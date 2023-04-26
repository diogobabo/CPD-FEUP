package org.t04.g13;

import java.net.*;
import java.io.*;

/**
 * This program used a simple TCP/IP socket client.
 *
 * @author www.codejava.net
 */
public class Client {

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
            this.authentication(socket);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void authentication(Socket socket) {
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            BufferedReader consoleIn = new BufferedReader(new InputStreamReader(System.in));

            System.out.println("Enter Username");
            String username = consoleIn.readLine();
            out.println(username);

            System.out.println("Enter Password");
            String password = consoleIn.readLine();
            out.println(password);

            System.out.println(this.readResponse(socket));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String readResponse(Socket socket) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            return in.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}