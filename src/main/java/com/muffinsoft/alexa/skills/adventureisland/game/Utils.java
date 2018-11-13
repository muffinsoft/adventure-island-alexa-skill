package com.muffinsoft.alexa.skills.adventureisland.game;

public class Utils {

    private Utils() {}

    public static String wrap(String phrase) {
        return "<speak>" + phrase + "</speak>";
    }

    public static String combineWithBreak(String responseText, String newText) {
        if (responseText != null) {
            return responseText + " <break time=\"3s\"/> " + newText;
        }
        return newText;
    }
}
