package server;


import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.*;

public class Player {

    private String username;
    private String password;

    private final int eloRange = 100;
    private int queueTime = 0;

    private int elo = 500;

    private SocketChannel clientSocket;

    public Player(SocketChannel socket) {
        this.clientSocket = socket;
    }

    public Player(SocketChannel socket, String name) {
        this.clientSocket = socket;
        this.username = name;
        this.elo = 500;
    }
    public Player(SocketChannel socket, String name, int elo) {
        this.clientSocket = socket;
        this.username = name;
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

    public SocketChannel getClientSocket() {
        return this.clientSocket;
    }

    public void setElo(int elo) {
        this.elo = elo;
    }

    public int getQueueTime() {
        return queueTime;
    }

    public void startQueueTimer() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                queueTime++;
            }
        }, 0, 1000);
    }

    public void setClientSocket(SocketChannel clientSocket) {
        this.clientSocket = clientSocket;
    }
}
