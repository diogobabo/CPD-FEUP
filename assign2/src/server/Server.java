package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.Iterator;

public class Server {
    private final int PORT;
    Selector selector;

    public Server(int port) {

        this.PORT = port;

    }

    public void start(){
        try(ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()){
            serverSocketChannel.socket().bind(new InetSocketAddress(PORT)); //Listen to port PORT
            serverSocketChannel.configureBlocking(false); //Set to non-blocking mode
            selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("Server started on port " + PORT);

            //Listen for connection attempts
            while(true){
                selector.select();

                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while(iterator.hasNext()){
                    SelectionKey key = iterator.next();
                    iterator.remove();

                        if(!key.isValid()){
                            continue;
                        }
                        //If client is trying to connect, accept connection
                        if(key.isAcceptable()){
                            SocketChannel client = ((ServerSocketChannel) key.channel()).accept();
                            client.configureBlocking(false);
                            client.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                        }
                        //If we can write to client, write
                        else if(key.isWritable()){
                            SocketChannel client = (SocketChannel) key.channel();
                        }
                        //If we can read from client, read
                        else if(key.isReadable()){
                            SocketChannel client = (SocketChannel) key.channel();

                        }
                }
            }

        }catch (IOException e){
            System.err.println("start(): Error opening ServerSocketChannel.");
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
