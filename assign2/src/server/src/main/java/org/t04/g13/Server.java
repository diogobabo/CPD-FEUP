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
    private ExecutorService authPool;
    private ExecutorService gamePool;
    private ExecutorService matchmakingPool;

    public Server(int port) {
        this.PORT = port;
        games = new ArrayList<>();
        waitingClients = new ArrayDeque<>();
        authPool = Executors.newFixedThreadPool(25);
        gamePool = Executors.newFixedThreadPool(25);
        matchmakingPool = Executors.newSingleThreadExecutor();
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            int i = 1;
            int elo = 100;
            System.out.println("Server started on port " + PORT);
            MatchMaking matchmaking = new MatchMaking(this, 1000);
            matchmakingPool.execute(matchmaking);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("\n(#" + i + ") New client connected from IP: " + clientSocket.getInetAddress().getHostAddress());
                Player player = new Player(clientSocket, 100);
                elo += 100;
                addToQueue(player);
                sendMessage(clientSocket, ENQUEUE);
                // Runnable auth = new Auth(clientSocket, this, i++);

                // TODO catch exception -> client disconnects before or during his auth process
                // TODO stop auth process if it's taking too long
                // authPool.execute(auth);
            }
        } catch (Exception e) {
            System.err.println("Server exception: " + e.getMessage());
            authPool.shutdown();
            gamePool.shutdown();
            matchmakingPool.shutdown();
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

        for (List<Player> gameTeam : allTeams) {
            if (gameTeam.size() == 2) {
                Runnable game = new Game(gameTeam);
                gamePool.execute(game);
                for (Player play : gameTeam) {
                    waitingClients.remove(play);
                }
            }
        }
    }

    public void addToQueue(Player player) {
        waitingClients.offer(player);
        player.startQueueTimer();
    }

    public static void main(String[] args) {
        int port = 8000;
        if (args.length != 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.printf("First argument has to be an integer, %s not allowed!%n", args[0]);
                System.out.printf("Using default port: %d%n%n", port);
            }
        }

        Server server = new Server(port);
        server.start();

    }
}
