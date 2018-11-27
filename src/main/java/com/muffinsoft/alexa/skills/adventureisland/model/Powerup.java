package com.muffinsoft.alexa.skills.adventureisland.model;

public class Powerup {

    private String name;
    private String explanation;
    private String got;
    private String used;
    private String action;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String getGot() {
        return got;
    }

    public void setGot(String got) {
        this.got = got;
    }

    public String getUsed() {
        return used;
    }

    public void setUsed(String used) {
        this.used = used;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
