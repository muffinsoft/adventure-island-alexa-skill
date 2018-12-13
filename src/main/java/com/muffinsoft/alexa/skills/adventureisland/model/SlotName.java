package com.muffinsoft.alexa.skills.adventureisland.model;

public enum SlotName {

    ACTION("action"), MISSION("mission"), WHATEVER("whatever");

    public final String text;

    SlotName(String text) {
        this.text = text;
    }

}
