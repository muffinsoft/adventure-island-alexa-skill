package com.muffinsoft.alexa.skills.adventureisland.model;

public enum State {

    INTRO("Intro"), ACTION("Action"), OUTRO("Outro"), FAILED("Failed"), HELP("Help");

    private String key;

    State(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
