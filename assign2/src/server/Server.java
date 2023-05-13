package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private int PORT;
    private ServerSocketChannel serverSocketChannel;

    private ExecutorService game_pool;

    private ArrayDeque<SocketChannel> userQueue;


    public Server(int port) {
        try {
            this.PORT = port;
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.socket().bind(new InetSocketAddress(port));
            serverSocketChannel.configureBlocking(false);
            userQueue = new ArrayDeque<>();
            game_pool = Executors.newFixedThreadPool(2);
            System.out.println("Server started and listening on port " + port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start(){
        try {
            while(true) {
                SocketChannel socketChannel = serverSocketChannel.accept();
                if (socketChannel != null) {
                    addToQueue(socketChannel);
                    System.out.println("New connection from: " + socketChannel.getRemoteAddress());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void  addToQueue(SocketChannel user) throws IOException {
        this.userQueue.offer(user);
        if(userQueue.size() == 2) {
            List<SocketChannel> users = new ArrayList<>();
            for(int i = 0; i < 2; i++) {
                SocketChannel client = userQueue.poll();
                users.add(client);
            }
            Runnable game = new Game(users);
            game_pool.execute(game);
        }
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
