package client.src.main.java.org.t04.g13;

import static server.src.main.java.org.t04.g13.Utils.*;

import java.net.*;
import java.io.*;
import java.util.Objects;


/**
 * This program used a simple TCP/IP socket client.
 *
 * @author www.codejava.net
 */
public class Client {

    private static boolean gameStarted = false;
    private static boolean gameFinished = false;

    public static void main(String[] args) {
        if (args.length < 2) return;

        Client client = new Client();
        String hostname = args[0];
        int port = Integer.parseInt(args[1]);
        client.startConnection(hostname,port);
    }

    public void startConnection(String hostname, int port) {
        try {
            Socket socket = new Socket(hostname, port);
            //this.authentication(socket);
            this.gameLoop(socket);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void authentication(Socket socket) {
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader consoleIn = new BufferedReader(new InputStreamReader(System.in));

            //Authentication method selection
            String auth_text = readResponse(socket);
            int opt;

            //Input validation
            while(true){
                String option = consoleIn.readLine();
                System.out.println(auth_text);
                if(!option.matches("\\d+")){
                    System.out.println("Invalid option!\n");
                    continue;
                }
                opt = Integer.parseInt(option);
                if(opt == 0 || opt == 1 || opt == 2){
                    out.println(opt);
                    break;
                }
            }

            //String username = consoleIn.readLine();
            //out.println(username);

            //String password = consoleIn.readLine();
            //out.println(password);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void gameLoop(Socket socket) {
        try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader consoleIn = new BufferedReader(new InputStreamReader(System.in))) {

            while (true) {
                String message = readResponse(socket);
                if (message == null) {
                    continue;
                }
                else if(message.equals("IN_QUEUE")) {
                    System.out.println("Waiting for other players to join");
                }
                else if(message.equals("GAME_FINISHED")) {
                    System.out.println("Game Ended");
                    break;
                }
                else if(message.equals("GAME_START")) {
                    System.out.println("Game is about to start");
                }
                else if(message.equals("START_ROUND")) {
                    System.out.println("Round Started");
                    this.round(socket);
                }
                else if(message.equals("ANSWER_TIME")) {
                    System.out.println("Type your answer");

                    boolean answerSent = false;

                    // Wait for 5 seconds for the player's input
                    long startTime = System.currentTimeMillis();
                    long elapsedTime = 0;
                    while (elapsedTime < 5000) {
                        if (consoleIn.ready()) {
                            String answer = consoleIn.readLine();
                            out.println(answer);
                            answerSent = true;
                            break;
                        }
                        elapsedTime = System.currentTimeMillis() - startTime;
                    }

                    if (!answerSent) {
                        System.out.println("Time's up! Sending default answer...");
                        out.println("-1");
                    }
                }
                else if(message.equals("END_ROUND")) {
                    System.out.println(readResponse(socket));
                }
                else {
                    System.out.println(message);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void round(Socket socket) {
        String question = readResponse(socket);
        System.out.println(question);
        for(int i = 0; i < 4; i++) {
            System.out.println(readResponse(socket));
        }
    }
}
