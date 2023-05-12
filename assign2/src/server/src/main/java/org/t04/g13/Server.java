package server.src.main.java.org.t04.g13;


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static server.src.main.java.org.t04.g13.Utils.*;


public class Server {
    private final int PORT;
    private final List<Game> games;
    private final Queue<Player> waitingClients;
    private ExecutorService auth_pool;
    private ExecutorService game_pool;

    public Server(int port) {

        this.PORT = port;

        games = new ArrayList<>();
        waitingClients = new ArrayDeque<>();

        //New thread pool with at most MAX_TRHEADS authentication attempts
        auth_pool = Executors.newFixedThreadPool(MAX_TRHEADS);
        game_pool = Executors.newFixedThreadPool(10);
    }

    public void start() {

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            int i = 1;
            int elo = 100;
            System.out.println("Server started on port " + PORT);
            MatchMaking matchmaking = new MatchMaking(this,1000);
            matchmaking.start();
            while(true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("\n(#" + i + ") New client connected from IP: " + clientSocket.getInetAddress().getHostAddress());
                Player player = new Player(clientSocket,100);
                elo += 100;
                addToQueue(player);
                sendMessage(clientSocket,ENQUEUE);
                //Runnable auth = new Auth(clientSocket, this, i++);

                //TODO catch exception -> client disconnects before or during his auth process
                //TODO stop auth process if it's taking too long
                //auth_pool.execute(auth);
            }

        } catch (Exception e) {
            System.err.println("Server exception: " + e.getMessage());
            auth_pool.shutdown();
        }
    }

    public void matchMaking() {
        List<List<Player>> allTeams = new ArrayList<>();
        for (Player player : waitingClients) {
            int maxDiff = player.getQueueTime() * 5;
            boolean added = false;

            for (List<Player> team : allTeams) {
                int teamSize = team.size();
                int totalElo = team.stream().mapToInt(Player::getElo).sum();
                int averageElo = totalElo / teamSize;
                System.out.println("Team ELO:" + averageElo);
                System.out.println("Player ELO - " + (player.getElo() - maxDiff) + " & " + (player.getElo() + maxDiff));

                if (Math.abs(averageElo - player.getElo()) <= maxDiff && teamSize < MAX_PLAYERS) {
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

        for(List<Player> gameTeam: allTeams) {
            if(gameTeam.size() == 2) {
                Game game = new Game();
                game.setPlayers(gameTeam);
                game_pool.execute(game);
                for(Player play: gameTeam) {
                    waitingClients.remove(play);
                }
            }
        }
    }

    public void addToQueue(Player player) {
        waitingClients.offer(player);
        player.startQueueTimer();
        /*
        if (waitingClients.size() >= MAX_PLAYERS) {
            Game game = new Game();
            for (int i = 0; i < MAX_PLAYERS; i++) {
                Player currPlayer = waitingClients.poll();
                game.addPlayer(currPlayer);
            }
            game.start();
            games.add(game);
        }*/
    }


    public static void main(String[] args) {

        int port = 8000;
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