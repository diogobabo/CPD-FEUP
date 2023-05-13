package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class Utils {

    static final int MAX_TRHEADS = 2;
    static final int MAX_PLAYERS = 2;
    static final int NUM_QUESTIONS = 3;
    static final int QUESTION_TIME = 80000;
    static final String ENQUEUE = "IN_QUEUE";
    static final String START_ROUND = "START_ROUND";
    static final String END_ROUND = "END_ROUND";
    static final String ANSWER_TIME = "ANSWER_TIME";
    static final String GAME_START = "GAME_START";
    static final String GAME_END = "GAME_FINISHED";

    private static final int BUFFER_SIZE = 1024;

    public static void writeToSocket(SocketChannel socketChannel, String message) {
        try {
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
            ByteBuffer readBuffer = ByteBuffer.allocate(1024);
            StringBuilder messageBuilder = new StringBuilder();

            int bytesRead;
            while ((bytesRead = socketChannel.read(readBuffer)) > 0) {
                readBuffer.flip();
                byte[] bytes = new byte[bytesRead];
                readBuffer.get(bytes);
                String receivedData = new String(bytes, StandardCharsets.UTF_8);
                messageBuilder.append(receivedData);

                if (receivedData.contains("\n")) {
                    break;
                }

                readBuffer.clear();
            }

            if (bytesRead == -1) {
                return null;
            }

            String msg = messageBuilder.toString().trim();
            int colonIndex = msg.indexOf(':');
            if (colonIndex != -1) {
                msg = msg.substring(colonIndex + 1);
                return msg;
            }
            // Clear the socket buffer
            readBuffer.clear();

            return msg;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
