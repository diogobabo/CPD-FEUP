package server;

import jdk.jshell.execution.Util;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static server.Utils.MAX_THREADS;

public class Server {
    private int PORT;
    private ServerSocketChannel serverSocketChannel;

    private ExecutorService game_pool;
    private ExecutorService auth_pool;

    private ExecutorService matchmaking_pool;

    private ArrayDeque<Player> normalQueue;
    private ArrayDeque<Player> rankedQueue;



    public Server(int port) {
        try {
            this.PORT = port;
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.socket().bind(new InetSocketAddress("localhost",port));
            serverSocketChannel.configureBlocking(false);
            normalQueue = new ArrayDeque<>();
            rankedQueue = new ArrayDeque<>();
            game_pool = Executors.newFixedThreadPool(MAX_THREADS/2);
            auth_pool = Executors.newFixedThreadPool((MAX_THREADS/2)-1);
            matchmaking_pool = Executors.newSingleThreadExecutor();
            System.out.println("Server started and listening on port " + port);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void start() {
        try {
            MatchMaking matchmaking = new MatchMaking(this, 1000);
            matchmaking_pool.execute(matchmaking);
            while (true) {
                SocketChannel socketChannel = serverSocketChannel.accept();
                if (socketChannel != null) {
                    try {
                        Runnable auth = new Auth(socketChannel, this);
                        auth_pool.execute(auth);
                        System.out.println("New connection from: " + socketChannel.getRemoteAddress());
                    } catch (IOException e) {
                        // Handle socket channel closure
                        System.out.println("Connection closed by client: " + socketChannel.getRemoteAddress());
                        socketChannel.close();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addToQueueSave(Player user) throws InterruptedException {
        this.normalQueue.offer(user);
        if(normalQueue.size() >= 2) {
            List<Player> users = new ArrayList<>();
            for(int i = 0; i < 2; i++) {
                Player client = normalQueue.poll();
                users.add(client);
            }
            Runnable game = new Game(users);
            game_pool.execute(game);
        }
    }

    public void addToQueue(Player user) throws InterruptedException {
        if(!userInQueue(user,rankedQueue)) {
            this.rankedQueue.offer(user);
            user.startQueueTimer();
        }
    }

    public boolean userInQueue(Player user, ArrayDeque<Player> queue) {
        for(Player play: queue) {
            if(play.getUsername().equals(user.getUsername())) {
                play.setClientSocket(user.getClientSocket());
                return true;
            }
        }
        return false;
    }

    public void matchMaking() {
        List<List<Player>> allTeams = new ArrayList<>();
        for(Player player : rankedQueue) {
            System.out.println(player.getUsername() + '\n');
        }
        for (Player player : rankedQueue) {
            if(!Utils.isClientConnected(player.getClientSocket())) {
                continue;
            }
            int maxDiff = player.getQueueTime() * 5;
            boolean added = false;

            for (List<Player> team : allTeams) {
                int teamSize = team.size();
                int totalElo = team.stream().mapToInt(Player::getElo).sum();
                int averageElo = totalElo / teamSize;
                System.out.println("Team ELO:" + averageElo);
                System.out.println("Player ELO - " + (player.getElo() - maxDiff) + " & " + (player.getElo() + maxDiff));

                if (Math.abs(averageElo - player.getElo()) <= maxDiff && teamSize < Utils.MAX_PLAYERS) {
                    team.add(player);
                    added = true;
                    break;
                }
            }

            if (!added) {
                List<Player> newTeam = new ArrayList<>();
                newTeam.add(player);
                allTeams.add(newTeam);
            }
        }

        for (List<Player> gameTeam : allTeams) {
            if (gameTeam.size() == Utils.MAX_PLAYERS) {
                Runnable game = new Game(gameTeam);
                game_pool.execute(game);
                for (Player play : gameTeam) {
                    rankedQueue.remove(play);
                }
            }
        }
    }


    public static void main(String[] args) {
        int port = 8002;
        if(args.length != 0){
            try{
                port = Integer.parseInt(args[0]);
            }
            catch (NumberFormatException e){
                System.out.printf("First argument has to be an integer, %s not allowed!%n", args[0]);
                System.out.printf("Using default port: %d%n%n", port);
            }
        }
        Server server = new Server(port);
        server.start();
    }
}
