package com.muffinsoft.alexa.skills.adventureisland.model;

public class StateItem {

    private String mission;
    private String location;
    private String scene;
    private State state;
    private int index;
    private String introId;
    private String outroId;

    public String getMission() {
        return mission;
    }

    public void setMission(String mission) {
        this.mission = mission;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getScene() {
        return scene;
    }

    public void setScene(String scene) {
        this.scene = scene;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getIntroId() {
        return introId != null ? introId : "";
    }

    public void setIntroId(String introId) {
        this.introId = introId;
    }

    public String getOutroId() {
        return outroId != null ? outroId : "";
    }

    public void setOutroId(String outroId) {
        this.outroId = outroId;
    }

    public String getIntroOutroId(State state) {
        if (state == State.INTRO) {
            return getIntroId();
        } else {
            return getOutroId();
        }
    }
}
