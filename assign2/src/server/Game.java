package server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Game implements Runnable {

    private final List<Player> users;

    private final ExecutorService user_pool;

    private final List<Question> questions;

    public Game(List<Player> users) {
        this.users = users;
        this.user_pool = Executors.newFixedThreadPool(2);
        this.questions = parseQuestions();
    }

    public List<Question> parseQuestions() {
        String filename = "src/server/questions.csv";
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

    @Override
    public void run() {
        System.out.println(users.size());
        List<Question> quests = getRandomQuestions();
        for(Player player: users) {
            Runnable user = new PlayerHandler(player, quests);
            user_pool.execute(user);
        }
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
