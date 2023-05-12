package server.src.main.java.org.t04.g13;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Game extends Thread {
    private List<Player> players;
    private List<Question> questions;
    private ExecutorService playerPool;

    public Game(List<Player> players) {
        this.players = players;
        try {
            questions = parseQuestions();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Collections.shuffle(questions);
        playerPool = Executors.newFixedThreadPool(25);
    }

    public List<Question> parseQuestions() throws IOException {
        String filename = "src/server/src/main/resources/questions.csv";
        List<Question> all = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                String question = fields[0];
                String option1 = fields[1];
                String option2 = fields[2];
                String option3 = fields[3];
                String option4 = fields[4];
                String correctAnswer = fields[5];
                Question quest = new Question(question, option1, option2, option3, option4, correctAnswer);
                all.add(quest);
            }
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
        for (Player player : players) {
            Runnable playThread = new PlayerHandler(player, quests);
            playerPool.execute(playThread);
        }
        playerPool.shutdown();
    }


    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public List<Question> getRandomQuestions() {
        List<Question> res = new ArrayList<>();
        for (int i = 0; i < Utils.NUM_QUESTIONS; i++) {
            Random r = new Random();
            int idx = r.nextInt(questions.size() - 1);
            Question question = questions.get(idx);
            res.add(question);
        }
        return res;
    }
}
