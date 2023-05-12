package server.src.main.java.org.t04.g13;

import java.net.Socket;
import java.util.List;
import java.util.Objects;

public class PlayerHandler implements Runnable{

    Player player;

    Socket socket;

    List<Question> questions;

    public PlayerHandler(Player player, List<Question> questions) {
        this.player = player;
        this.socket = player.getClientSocket();
        this.questions = questions;
    }

    @Override
    public void run() {
        Utils.sendMessage(socket,Utils.GAME_START);
        playGame();
        Utils.sendMessage(socket,Utils.GAME_END);
    }

    public void playGame() {
        for(Question question: questions) {
            Utils.sendMessage(socket,Utils.START_ROUND);

            String ask = "Q: " + question.getQuestion();
            List<String> all = question.getAnswers();

            Utils.sendMessage(socket,ask);

            for (int j = 0; j < 4; j++) {
                String opt = j + 1 + ") " + all.get(j) + '\n';
                Utils.sendMessage(socket,opt);
            }

            Utils.sendMessage(socket,Utils.ANSWER_TIME);

            try {
                Thread.sleep(10000); // wait for 10 seconds
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            Utils.sendMessage(socket, Utils.END_ROUND);
            String answer = Utils.readResponse(socket);

            if (Objects.equals(answer, "-1")) {
                Utils.sendMessage(socket, "Wrong Answer!!");
                return;
            }
            int answerIdx = Integer.parseInt(answer);

            if (all.get(answerIdx).equals(question.getCorrectAnswer())) {
                Utils.sendMessage(socket, "Correct Answer!!");
            } else {
                Utils.sendMessage(socket, "Wrong Answer!!");
            }
        }
    }
}
