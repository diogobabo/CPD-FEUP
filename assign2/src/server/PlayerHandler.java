package server;

import java.nio.channels.SocketChannel;
import java.util.List;

public class PlayerHandler implements Runnable {
    private final Player player;

    private final SocketChannel socket;

    private final List<Question> questions;

    private int rightAnswers;

    private final int averageElo;

    private final int type;

    public PlayerHandler(Player player, List<Question> questions, int averageElo, int type) {
        this.socket = player.getClientSocket();
        this.questions = questions;
        this.player = player;
        this.rightAnswers = 0;
        this.averageElo = averageElo;
        this.type = type;
    }

    @Override
    public void run() {
        for (Question question : questions) {
            Utils.writeToSocket(socket, Utils.START_ROUND);
            String ask = "Q: " + question.getQuestion() + '\n';
            List<String> all = question.getAnswers();
            StringBuilder messageBuilder = new StringBuilder(ask);

            for (int j = 0; j < 4; j++) {
                String opt = j + 1 + ") " + all.get(j) + '\n';
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
                this.rightAnswers += 1;
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
        if(type == 2) {
            updateElo();
        }
        Utils.writeToSocket(socket,"Your current elo is: " + player.getElo());
    }

    public void updateElo() {
        int elo = player.getElo();
        float factor = (float) averageElo / elo;
        int eloIncrease = (int) (rightAnswers * 10 * factor);
        int eloDecrease = (int) ((Utils.NUM_QUESTIONS - rightAnswers) * 10 / factor);
        int newElo = elo + eloIncrease - eloDecrease;
        player.setElo(newElo);
        Utils.updateUserElo(player.getUsername(),Integer.toString(newElo));
    }
}
