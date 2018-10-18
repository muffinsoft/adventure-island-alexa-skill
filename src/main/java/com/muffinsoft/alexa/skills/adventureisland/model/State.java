package com.muffinsoft.alexa.skills.adventureisland.model;

public enum State {

    INTRO("Intro"), ACTION("Action"), OUTRO("Outro");

    private String key;

    State(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
