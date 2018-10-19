package com.muffinsoft.alexa.skills.adventureisland.model;

public class StateItem {

    public static final int TIER_INDEX = 0;
    public static final int MISSION_INDEX = 1;
    public static final int LOCATION_INDEX = 2;
    public static final int SCENE_INDEX = 3;

    private String mission;
    private String location;
    private String scene;
    private State state;
    private int index;
    private String introId;
    private String outroId;
    // game state is array of 4 indices: 1) Tier; 2) Mission; 3) Location; 4) Scene
    private byte[] gameState;

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

    public byte[] getGameState() {
        return gameState;
    }

    public void setGameState(byte[] gameState) {
        this.gameState = gameState;
    }

    public String getIntroOutroId(State state) {
        if (state == State.INTRO) {
            return getIntroId();
        } else {
            return getOutroId();
        }
    }
}
