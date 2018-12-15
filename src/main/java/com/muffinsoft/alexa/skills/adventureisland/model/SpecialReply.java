package com.muffinsoft.alexa.skills.adventureisland.model;

public enum SpecialReply {

    YES("yes"), NO("no"), CONTINUE("continue");

    public final String text;

    SpecialReply(String text) {
        this.text = text;
    }
}
