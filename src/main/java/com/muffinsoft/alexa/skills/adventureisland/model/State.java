package com.muffinsoft.alexa.skills.adventureisland.model;

public enum State {

    WELCOME("WelcomeNew"), INTRO("Intro"), ACTION("Action"), OUTRO("Outro"), FAILED("Failed"), HELP("Help"), CANCEL("Cancel"), QUIT("Quit");

    private String key;

    State(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
