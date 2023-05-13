package client;

import server.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.nio.channels.SocketChannel;

public class Client {
    private SocketChannel socketChannel;

    public static void main(String[] args) {
        if (args.length < 2) return;
        String hostname = args[0];
        int port = Integer.parseInt(args[1]);
        Client client = new Client(hostname,port);
    }

    public Client(String serverAddress, int serverPort) {
        try {
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(true);
            socketChannel.connect(new InetSocketAddress(serverAddress, serverPort));
            System.out.println("Connected to the server at " + serverAddress + ":" + serverPort+'\n');
            gameState(socketChannel);
        }
        catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void gameState(SocketChannel socket) throws IOException, InterruptedException {
        while(true) {
            String msg = Utils.readFromSocket(socketChannel);
            if (msg == null) {
                continue;
            }
            else if(msg.equals("N_QUEUE")) {
                System.out.println("Waiting for other players to join...");
            }
            else if(msg.equals("START_ROUND")) {
                System.out.println("Round is about to start!");
                String roundInfo = Utils.readFromSocket(socketChannel);
                System.out.println(roundInfo+'\n');
                System.out.println("Write your answer!");
                String ans = readInput(6000) + "\n";
                Utils.writeToSocket(socketChannel,ans);
                Thread.sleep(1000);
                System.out.println(Utils.readFromSocket(socketChannel));
            }
            else if(msg.equals("GAME_FINISHED")) {
                System.out.println("Game Ended!!");
                return;
            }
            else {
                System.out.println(msg);
            }
        }
    }

    public static String readInput(int timeout) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        long startTime = System.currentTimeMillis();
        long elapsedTime = 0;
        int number = -1;
        while (elapsedTime < timeout) {
            if (reader.ready()) {
                String input = reader.readLine();
                try {
                    number = Integer.parseInt(input);
                } catch (NumberFormatException e) {
                }
            }

            elapsedTime = System.currentTimeMillis() - startTime;
        }
        if(number == -1) {
            System.out.println("Time's up. No answer was given.");
        }
        return Integer.toString(number);
    }


}
