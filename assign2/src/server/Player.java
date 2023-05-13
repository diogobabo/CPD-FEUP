package server;


import java.net.Socket;
import java.util.*;

public class Player {

    private String username;
    private String password;

    private final int eloRange = 100;
    private int queueTime = 0;
    private int elo = 0;

    private Socket clientSocket;

    public Player(Socket socket) {
        this.clientSocket = socket;
    }

    public Player(Socket socket, int elo) {
        this.clientSocket = socket;
        this.elo = elo;
    }

    public void setUser(String username, String password, int elo) {
        this.username = username;
        this.password = password;
        this.elo = elo;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public int getElo() {
        return elo;
    }

    public Socket getClientSocket() {
        return this.clientSocket;
    }

    public void setElo(int elo) {
        this.elo = elo;
    }

    public int getQueueTime() {
        return queueTime;
    }

    // Method to start the timer and update the queueTime
    public void startQueueTimer() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                queueTime++; // Increment queueTime every second
            }
        }, 0, 1000); // Run the task every second (1000 milliseconds)
    }

}
