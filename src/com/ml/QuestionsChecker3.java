package com.ml;


import java.util.List;

public class QuestionsChecker3 {



    public static void main(String args[]) {
        List<String> questions = QuestionsChecker2.fetchQuestions(false);
        for (String question : questions) {
            System.out.println("################################################################################");
            System.out.println(question);
        }
    }
}
