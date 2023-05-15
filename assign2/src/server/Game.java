package server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.Math.round;

public class Game implements Runnable {

    private final List<Player> users;
    private final Map<Player,Integer> scores = new HashMap<>();

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
        for(Player player : users) {
            scores.put(player,0);
        }
        for (Question question : quests) {
            messageAll(Utils.START_ROUND);
            String ask = "Q: " + question.getQuestion() + '\n';
            List<String> all = question.getAnswers();
            StringBuilder messageBuilder = new StringBuilder(ask);

            for (int j = 0; j < 4; j++) {
                String opt = Integer.toString(j + 1) + ") " + all.get(j) + '\n';
                messageBuilder.append(opt);
            }
            String roundInfo = messageBuilder.toString();
            messageAll(roundInfo);

            try {
                Thread.sleep(6200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Map<Player,String> results =  getResults();

            for (Player player : users) {
                String answer = results.get(player);
                int answerIdx = Integer.parseInt(answer) - 1;

                if(answer.equals("-1")) {
                    Utils.writeToSocket (player.getClientSocket(),"Wrong Answer!");
                    scores.put(player,scores.get(player) - 1);
                }
                else if (all.get(answerIdx).equals(question.getCorrectAnswer())) {
                    Utils.writeToSocket (player.getClientSocket(),"Correct Answer!");
                    scores.put(player,scores.get(player) + 1);
                } else {
                    Utils.writeToSocket (player.getClientSocket(),"Wrong Answer!");
                    scores.put(player,scores.get(player) - 1);
                }
            }

            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        assignScores();
        messageAll(Utils.GAME_END);

    }

    public void messageAll(String message) {
        for (Player player : users) {
            Utils.writeToSocket(player.getClientSocket(), message);
        }
    }
    public void assignScores() {
        int numQuestions = Utils.NUM_QUESTIONS;
        int averageRating = 0;
        for(Player player : users) {
            averageRating += player.getElo();
        }
        averageRating = averageRating / users.size();
        for (Player player : users) {
            int score = scores.get(player)-(numQuestions/2);
            int elo = player.getElo();
            int newElo = elo + (score * 10) + (elo > averageRating + 100? 0: round((averageRating / elo) * 3));
            player.setElo(newElo);
            Utils.writeToSocket(player.getClientSocket(), "Your new elo is: " + newElo);
        }
        updateScoreFile();
    }
    public Map<Player,String> getResults() {
        Map<Player,String> results = new HashMap<>();
        for (Player player : users) {
            String result = Utils.readFromSocket(player.getClientSocket());
            results.put(player, Objects.requireNonNullElse(result, "-1"));
        }
        return results;
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
    public void updateScoreFile() {
        for (Player player : users) {
            Utils.updateUserElo(player.getUsername(),Integer.toString(player.getElo()));
        }
    }
}
