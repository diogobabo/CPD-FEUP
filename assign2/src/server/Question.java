package server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Question {
    private String question;

    private List<String> choices;
    private String answer;

    public Question(String question, String answer1, String answer2, String answer3, String answer4, String answer) {
        this.question = question;
        this.choices = Arrays.asList(answer1, answer2, answer3, answer4);
        this.answer = answer;
    }

    public String getQuestion() {
        return question;
    }

    public List<String> getAnswers() {
        return choices;
    }

    public String getCorrectAnswer() {
        return answer;
    }
}
