package com.muffinsoft.alexa.skills.adventureisland.model;

public enum State {

    WELCOME("WelcomeNew"), INTRO("Intro"), READY("Ready"),
    ACTION("Action"), OUTRO("Outro"), FAILED("Failed"),
    HELP("Help"), CANCEL("Cancel"), QUIT("Quit"),
    CHECKPOINT("Checkpoint"), RESET("Reset"), RESTART("Restart"),
    MORE("More"), PLAY_AGAIN("PlayAgain"), PLAY_NEW("PlayNew"),
    CONTINUE("Continue"), BUY("Buy"), DIFFERENT_OR_RESET("DifferentOrReset"), MAIN_MENU("MainMenu");

    private String key;

    State(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
