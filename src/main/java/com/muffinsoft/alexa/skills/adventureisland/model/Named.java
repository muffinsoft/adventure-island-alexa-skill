package com.muffinsoft.alexa.skills.adventureisland.model;

public class Named {

    private String name;
    private String introId = "";
    private String outroId = "";

    public String getIntroId() {
        return introId;
    }

    public void setIntroId(String introId) {
        this.introId = introId;
    }

    public String getOutroId() {
        return outroId;
    }

    public void setOutroId(String outroId) {
        this.outroId = outroId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
