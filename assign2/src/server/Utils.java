package server;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Utils {
    private static final String EOF_MARKER = "<EOF>";
    static final int MAX_THREADS = ManagementFactory.getThreadMXBean().getThreadCount();
    static final int MAX_PLAYERS = 2;
    static final int NUM_QUESTIONS = 4;
    static final int QUESTION_TIME = 80000;
    static final String ENQUEUE = "IN_QUEUE";
    static final String START_ROUND = "START_ROUND";
    static final String END_ROUND = "END_ROUND";
    static final String ANSWER_TIME = "ANSWER_TIME";
    static final String GAME_START = "GAME_START";
    private static final Object lock = new Object();
    static final String GAME_END = "GAME_FINISHED";

    private static final int BUFFER_SIZE = 1024;

    //private static final Map<SocketChannel, Lock> channelLocks = new ConcurrentHashMap<>();

    public static void writeToSocket(SocketChannel socketChannel, String message) {
        try {
            message = message + EOF_MARKER; // Add the delimiter "&&" before the newline character
            String messageWithLength = message.length() + ":" + message; // Prepend the length of the message
            ByteBuffer buffer = ByteBuffer.wrap(messageWithLength.getBytes());
            socketChannel.write(buffer);
            buffer.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String readFromSocket(SocketChannel socketChannel) {
        try {
            ByteBuffer readBuffer = ByteBuffer.allocate(4096);
            StringBuilder messageBuilder = new StringBuilder();

            int bytesRead = socketChannel.read(readBuffer);
            if (bytesRead == -1) {
                return null;  // Return empty string to indicate no data was received
            }

            while (bytesRead > 0) {
                readBuffer.flip();
                byte[] bytes = new byte[bytesRead];
                readBuffer.get(bytes);
                String receivedData = new String(bytes, StandardCharsets.UTF_8);
                messageBuilder.append(receivedData);

                if (receivedData.contains(EOF_MARKER)) {
                    break;
                }

                readBuffer.clear();
                bytesRead = socketChannel.read(readBuffer);
            }

            String msg = messageBuilder.toString().trim();
            if (msg.equals("1:")) {
                return null;
            }
            int colonIndex = msg.indexOf(':');
            if (colonIndex != -1) {
                msg = msg.substring(colonIndex + 1);
                int delimiterIndex = msg.indexOf(EOF_MARKER);
                if (delimiterIndex != -1) {
                    msg = msg.substring(0, delimiterIndex);
                }
                return msg;
            }

            return msg;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }


    public static boolean isClientConnected(SocketChannel socketChannel) {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(1);
            int bytesWritten = socketChannel.write(buffer);
            return bytesWritten != -1; // Check if any bytes were written
        } catch (IOException e) {
            // Exception occurred, indicating the client is not connected
            return false;
        }
    }

    public static int isCredentialsValid(String name, String password) {
        try (BufferedReader reader = new BufferedReader(new FileReader("src/server/users.csv"))) {
            String line;
            int lastNumber = -1;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields.length >= 3 && fields[0].equals(name) && fields[1].equals(password)) {
                    lastNumber = Integer.parseInt(fields[2]);
                }
            }
            return lastNumber;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static boolean doesUsernameExist(String name) {
        try (BufferedReader reader = new BufferedReader(new FileReader("src/server/users.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields.length >= 1 && fields[0].equals(name)) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void addUser(String name, String password) {
        synchronized (lock) {
            try (PrintWriter writer = new PrintWriter(new FileWriter("src/server/users.csv", true))) {
                String line = name + "," + password + ",500";
                writer.println(line);
                writer.flush(); // Flush the PrintWriter to ensure the data is written immediately
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static void updateUserElo(String name, String elo){
        synchronized (lock){
            try (BufferedReader reader = new BufferedReader(new FileReader("src/server/users.csv"))) {
                File tempFile = new File("src/server/temp.csv");
                PrintWriter writer = new PrintWriter(new FileWriter(tempFile));

                String line;
                boolean updated = false;

                while ((line = reader.readLine()) != null) {
                    String[] fields = line.split(",");
                    if (fields.length >= 3 && fields[0].equals(name)) {
                        line = fields[0] + "," + fields[1] + "," + elo;
                        updated = true;
                    }
                    writer.println(line);
                }
                reader.close();
                writer.flush();
                writer.close();
                if (updated) {
                    File file = new File("src/server/users.csv");
                    file.delete();
                    tempFile.renameTo(new File("src/server/users.csv"));
                } else {
                    tempFile.delete();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
