package server.src.main.java.org.t04.g13;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Game extends Thread {


    private List<Player> players;
    private List<Question> questions;

    private ExecutorService player_pool;

    public Game() {
        players = new ArrayList<>();
        try {
            questions = this.parseQuestions();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Collections.shuffle(questions);
        player_pool = Executors.newFixedThreadPool(20);
    }

    public List<Question> parseQuestions() throws IOException {
        String filename = "src/server/src/main/resources/questions.csv";
        List<Question> all = new ArrayList<>();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                String question = fields[0];
                String option1 = fields[1];
                String option2 = fields[2];
                String option3 = fields[3];
                String option4 = fields[4];
                String correctAnswer = fields[5];
                Question quest = new Question(question,option1,option2,option3,option4,correctAnswer);
                all.add(quest);
            }
            reader.close();
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
        return all;
    }

    public void addPlayer(Player player) {
        this.players.add(player);
    }


    @Override
    public void run() {
        List<Question> quests = getRandomQuestions();
        for(Player player : players) {
            PlayerHandler playThread = new PlayerHandler(player, quests);
            player_pool.execute(playThread);
        }
    }
    public void initGame() {
        this.messageEveryone(Utils.GAME_START);
    }

    public void gameRound() {
        messageEveryone(Utils.START_ROUND);
        Random r = new Random();
        int idx = r.nextInt(questions.size() - 1);
        Question question = questions.get(idx);
        String ask = "Q: " + question.getQuestion();
        List<String> all = question.getAnswers();
        messageEveryone(ask);

        for (int j = 0; j < 4; j++) {
            String opt = Integer.toString(j + 1) + ") " + all.get(j) + '\n';
            messageEveryone(opt);
        }

        messageEveryone(Utils.ANSWER_TIME);

        try {
            Thread.sleep(10000); // wait for 10 seconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        for (Player play : players) {
            Thread playerThread = new Thread(() -> {
                Socket playerSocket = play.getClientSocket();
                Utils.sendMessage(playerSocket, Utils.END_ROUND);
                String answer = Utils.readResponse(playerSocket);
                if (Objects.equals(answer, "-1")) {
                    Utils.sendMessage(playerSocket, "Wrong Answer!!");
                    return;
                }
                int answerIdx = Integer.parseInt(answer);

                if (all.get(answerIdx).equals(question.getCorrectAnswer())) {
                    Utils.sendMessage(playerSocket, "Correct Answer!!");
                } else {
                    Utils.sendMessage(playerSocket, "Wrong Answer!!");
                }
            });
            playerThread.start();

            try {
                playerThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    public void displayQuestions() {
        for(int i = 0; i < Utils.NUM_QUESTIONS; i++) {
            Question quest = questions.get(i);
            List<String> all = quest.getAnswers();
            String question = "Q: " + quest.getQuestion();
            this.messageEveryone(question);
            for(int j = 0; j < 4; j++) {
                String opt = Integer.toString(j+1) + ") " + all.get(j) +'\n';
                this.messageEveryone(opt);
            }
            this.messageEveryone("Type your answer:");
        }
    }

    public void messageEveryone(String message) {
        List<Thread> threads = new ArrayList<>();

        for (Player player : players) {
            Thread thread = new Thread(() -> {
                Socket socket = player.getClientSocket();
                Utils.sendMessage(socket, message);
            });

            thread.start();
            threads.add(thread);
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void setPlayers(List<Player> players){
        this.players = players;
    }

    public List<Question> getRandomQuestions() {
        List<Question> res = new ArrayList<>();
        for(int i = 0; i < Utils.NUM_QUESTIONS; i++) {
            Random r = new Random();
            int idx = r.nextInt(questions.size() - 1);
            Question question = questions.get(idx);
            res.add(question);
        }
        return res;
    }
}
