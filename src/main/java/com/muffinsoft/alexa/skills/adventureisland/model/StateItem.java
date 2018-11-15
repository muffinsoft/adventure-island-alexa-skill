package com.muffinsoft.alexa.skills.adventureisland.model;

import com.muffinsoft.alexa.skills.adventureisland.content.AttributeKeys;
import com.muffinsoft.alexa.skills.adventureisland.content.Constants;
import com.muffinsoft.alexa.skills.adventureisland.game.SessionAttributeManager;

import java.util.List;
import java.util.Map;

import static com.muffinsoft.alexa.skills.adventureisland.content.AttributeKeys.LOCATION;
import static com.muffinsoft.alexa.skills.adventureisland.content.AttributeKeys.MISSION;
import static com.muffinsoft.alexa.skills.adventureisland.content.AttributeKeys.SCENE;
import static com.muffinsoft.alexa.skills.adventureisland.content.Constants.ROOT;

public class StateItem {

    private String mission;
    private String location;
    private String scene;
    private State state;
    private State pendingState;
    private Integer index;
    private Integer pendingIndex;
    private String introId;
    private String outroId;
    private Integer tierIndex;
    private Integer missionIndex;
    private Integer locationIndex;
    private Integer sceneIndex;
    private Map<String, List<String>> locationIntros;
    private Map<String, List<String>> sceneIntros;

    private HelpState helpState;

    private SessionAttributeManager sessionAttributeManager;

    public StateItem(SessionAttributeManager sessionAttributeManager) {
        this.sessionAttributeManager = sessionAttributeManager;
    }

    public String getMission() {
        if (mission == null) {
            mission = sessionAttributeManager.getString(MISSION, ROOT);
        }
        return mission;
    }

    public void setMission(String mission) {
        this.mission = mission;
        sessionAttributeManager.updateObject(MISSION, mission);
    }

    public String getLocation() {
        if (location == null) {
            location = sessionAttributeManager.getString(LOCATION, ROOT);
        }
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
        sessionAttributeManager.updateObject(LOCATION, location);
    }

    public String getScene() {
        if (scene == null) {
            scene = sessionAttributeManager.getString(SCENE, ROOT);
        }
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

    public State getPendingState() {
        return pendingState;
    }

    public void setPendingState(State pendingState) {
        this.pendingState = pendingState;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getPendingIndex() {
        return pendingIndex;
    }

    public void setPendingIndex(int pendingIndex) {
        this.pendingIndex = pendingIndex;
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

    public int getTierIndex() {
        return tierIndex;
    }

    public void setTierIndex(int tierIndex) {
        this.tierIndex = tierIndex;
    }

    public int getMissionIndex() {
        return missionIndex;
    }

    public void setMissionIndex(int missionIndex) {
        this.missionIndex = missionIndex;
    }

    public int getLocationIndex() {
        return locationIndex;
    }

    public void setLocationIndex(int locationIndex) {
        this.locationIndex = locationIndex;
    }

    public int getSceneIndex() {
        return sceneIndex;
    }

    public void setSceneIndex(int sceneIndex) {
        this.sceneIndex = sceneIndex;
    }

    public String getIntroOutroId(State state) {
        if (state == State.INTRO) {
            return getIntroId();
        } else {
            return getOutroId();
        }
    }

    public HelpState getHelpState() {
        return helpState;
    }

    public void setHelpState(HelpState helpState) {
        this.helpState = helpState;
    }

    public Map<String, List<String>> getLocationIntros() {
        return locationIntros;
    }

    public void setLocationIntros(Map<String, List<String>> locationIntros) {
        this.locationIntros = locationIntros;
    }

    public Map<String, List<String>> getSceneIntros() {
        return sceneIntros;
    }

    public void setSceneIntros(Map<String, List<String>> sceneIntros) {
        this.sceneIntros = sceneIntros;
    }
}
