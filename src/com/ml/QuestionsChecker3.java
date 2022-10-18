package com.ml;

import com.ml.utils.CompressionUtil;
import com.ml.utils.DatabaseHelper;
import com.ml.utils.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QuestionsChecker3 {

    public static List<String> fetchQuestions(boolean web) {
        List<String> result = new ArrayList<String>();
        Map<Long, String> questionsMap = DatabaseHelper.fetchQuestions();
        for (String compressedQuestion : questionsMap.values()) {
            String text = null;
            try {
                text = CompressionUtil.decompressB64(compressedQuestion);
            } catch (Exception e) {
                String msg = "Error descomprimiendo pregunta";
                System.out.println(msg);
                Logger.log(msg);
                e.printStackTrace();
                Logger.log(e);
            }
            if (text != null) {
                if (web) {
                    text=text.replaceAll("\n", "<br/>");
                }
                result.add(text);
            }
        }
        return result;
    }


    public static void main(String args[]) {
        List<String> questions = fetchQuestions(false);
        for (String question : questions) {
            System.out.println("################################################################################");
            System.out.println(question);
        }
    }
}
