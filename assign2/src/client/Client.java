package client;

import server.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.nio.channels.SocketChannel;
import java.util.Objects;

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
            authenticationState();
            gameState();
        }
        catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void authenticationState() throws IOException {
        int i = 0;
        int j = 0;
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while(true) {
            String msg = Utils.readFromSocket(socketChannel);
            if (msg == null || Objects.equals(msg,"nada")) {
                continue;
            }
            else if(msg.equals("AUTHENTICATION")) {
                System.out.println("1 - Register\n2 - Login\n3 - Disconnect");
                String input = waitInput(60000);
                Utils.writeToSocket(socketChannel,input);
            }
            else if(msg.equals("LOGIN")) {
                i++;
                if(i > 1) {
                    System.out.println("Wrong credentials. Try again!");
                }
                else {
                    System.out.println("Type your credentials!");
                }
                System.out.println("Username:");
                String userName = waitInput(60000);
                System.out.println("Password:");
                String password = waitInput(60000);
                Utils.writeToSocket(socketChannel,userName);
                Utils.writeToSocket(socketChannel,password);
            }
            else if(msg.equals("REGISTER")) {
                j++;
                if(j > 1) {
                    System.out.println("Username in Use. Try again!");
                }
                else {
                    System.out.println("Type your credentials!");
                }
                System.out.println("Type Username:");
                String userName = waitInput(60000);
                System.out.println("Type Password:");
                String password = waitInput(60000);
                Utils.writeToSocket(socketChannel,userName);
                Utils.writeToSocket(socketChannel,password);
            }
            else if(msg.equals("SUCCESSFUL")) {
                System.out.println("Successful authentication!");
                System.out.println("Waiting for other players to join...");

                break;
            }
            else {
                System.out.println(msg);
            }
        }
    }

    public void gameState() throws IOException, InterruptedException {
        while(true) {
            String msg = Utils.readFromSocket(socketChannel);
            if (msg == null || Objects.equals(msg,"nada")) {
                continue;
            }
            else if(msg.equals("IN_QUEUE")) {
                System.out.println("Waiting for other players to join...");
            }
            else if(msg.equals("START_ROUND")) {
                System.out.println("Round is about to start!");
                String roundInfo = Utils.readFromSocket(socketChannel);
                System.out.println(roundInfo);
                System.out.println("Write your answer!");
                String ans = readInput(6000);
                Utils.writeToSocket(socketChannel,ans);
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

    public static String waitInput(int timeout) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        long startTime = System.currentTimeMillis();
        long elapsedTime = 0;
        String ans = null;
        while (elapsedTime < timeout) {
            if (reader.ready()) {
                ans = reader.readLine();
                return ans;
            }
            elapsedTime = System.currentTimeMillis() - startTime;
        }
        System.out.println("Too long to answer...");
        System.exit(1);
        return ans;
    }


}
