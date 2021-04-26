package com.ml;

import com.ml.utils.CompressionUtil;
import com.ml.utils.DatabaseHelper;
import com.ml.utils.Logger;

import java.util.Map;

public class QuestionsChecker3 {

    public static void main(String args[]){
        Map<Long, String> questionsMap = DatabaseHelper.fetchQuestions();
        for (String compressedQuestion:questionsMap.values()){
            String text=null;
            try {
                text=CompressionUtil.decompressB64(compressedQuestion);
            }catch (Exception e){
                String msg="Error descomprimiendo pregunta";
                System.out.println(msg);
                Logger.log(msg);
                e.printStackTrace();
                Logger.log(e);
            }
            if (text!=null){
                System.out.println("################################################################################");
                System.out.println(text);
            }
        }
    }

}
