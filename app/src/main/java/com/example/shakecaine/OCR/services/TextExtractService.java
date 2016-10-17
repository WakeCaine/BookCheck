package com.example.shakecaine.OCR.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.Getter;

/**
 * Created by Shakecaine on 2016-07-26.
 */
public class TextExtractService {
    @Getter List<String> mainStringList = new ArrayList<>();
    @Getter List<String> subStringList = new ArrayList<>();

    public void extractWords(String searchString){
        mainStringList = Arrays.asList(searchString.split("\\s* \\s*"));
    }

    public void extractSubWords(String searchString){
        List<String> tempArray = Arrays.asList(searchString.split("\\s* \\s*"));
        for(String word : tempArray){
            word.replace(" ", "");
            subStringList.add(word);
        }
    }

    public String getMainWords(boolean cutLast){
        String mainString = "";
        if(cutLast && mainStringList.size() > 0){
            mainStringList.remove(mainStringList.size() - 1);
        }
        for(String word : mainStringList){
            mainString += word + " ";
        }
        return mainString.length() > 1 ? mainString.substring(0,mainString.length() - 2) : "";
    }

    public String getSubWords(boolean cutLast){
        String mainString = "";
        if(cutLast){
            subStringList.remove(subStringList.size() - 1);
        }
        for(String word : subStringList){
            mainString += word + " ";
        }
        return mainString.length() > 1 ? mainString.substring(0,mainString.length() - 2) : "";
    }
}
