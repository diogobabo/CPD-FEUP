package client;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class Utils {
    private static final String EOF_MARKER = "<EOF>";
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
}
