package com.muffinsoft.alexa.skills.adventureisland.model;

import java.util.Arrays;
import java.util.List;

public enum State {

    WELCOME("WelcomeNew"), INTRO("Intro"), READY("Ready"),
    ACTION("Action"), OUTRO("Outro"), FAILED("Failed"),
    HELP("Help"), CANCEL("Cancel"), QUIT("Quit"),
    CHECKPOINT("Checkpoint"), RESET("Reset"), RESTART("Restart"),
    MORE("More"), PLAY_AGAIN("PlayAgain"), PLAY_NEW("PlayNew"),
    CONTINUE("Continue"), BUY("Buy"), UPSELL("UpSell"), DIFFERENT_OR_RESET("DifferentOrReset"), MAIN_MENU("MainMenu"), MAIN_OR_CONTINUE("MainOrContinue");

    private String key;

    State(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public static final State[] CONTINUED_STATES_ARRAY = {INTRO, ACTION, OUTRO};
    public static final List<State> CONTINUED_STATES = Arrays.asList(CONTINUED_STATES_ARRAY);
}
