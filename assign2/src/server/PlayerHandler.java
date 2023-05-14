package server;

import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Objects;

public class PlayerHandler implements Runnable {
    private final Player player;

    private final SocketChannel socket;

    private final List<Question> questions;

    public PlayerHandler(Player player, List<Question> questions) {
        this.socket = player.getClientSocket();
        this.questions = questions;
        this.player = player;
    }

    @Override
    public void run() {
        for (Question question : questions) {
            Utils.writeToSocket(socket, Utils.START_ROUND);
            String ask = "Q: " + question.getQuestion() + '\n';
            List<String> all = question.getAnswers();
            StringBuilder messageBuilder = new StringBuilder(ask);

            for (int j = 0; j < 4; j++) {
                String opt = Integer.toString(j + 1) + ") " + all.get(j) + '\n';
                messageBuilder.append(opt);
            }
            String roundInfo = messageBuilder.toString();
            Utils.writeToSocket(socket, roundInfo);

            try {
                Thread.sleep(6200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            String answer = Utils.readFromSocket(socket);
            System.out.println(answer);


            int answerIdx = Integer.parseInt(answer) - 1;

            if(answer.equals("-1")) {
                Utils.writeToSocket(socket, "Wrong Answer!");
            }
            else if (all.get(answerIdx).equals(question.getCorrectAnswer())) {
                Utils.writeToSocket(socket, "Correct Answer!");
            } else {
                Utils.writeToSocket(socket, "Wrong Answer!");
            }

            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Utils.writeToSocket(socket, Utils.GAME_END);
    }
}
