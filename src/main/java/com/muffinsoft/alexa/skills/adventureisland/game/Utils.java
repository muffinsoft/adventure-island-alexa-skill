package com.muffinsoft.alexa.skills.adventureisland.game;

public class Utils {

    private Utils() {}

    public static String wrap(String phrase) {
        return " " + phrase + " ";
    }

    public static String combineWithBreak(String responseText, String newText) {
        if (responseText != null) {
            return responseText + " <break time=\"3s\"/> " + newText;
        }
        return newText;
    }
}
