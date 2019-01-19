package com.muffinsoft.alexa.skills.adventureisland.model;

import com.amazon.ask.attributes.AttributesManager;
import com.muffinsoft.alexa.skills.adventureisland.game.SessionAttributeManager;

import static com.muffinsoft.alexa.skills.adventureisland.content.AttributeKeys.*;
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

    private HelpState helpState;

    private String imageToInsert;
    private String image1ToInsert;

    private final SessionAttributeManager sessionAttributeManager;

    public StateItem(AttributesManager attributesManager) {
        this.sessionAttributeManager = new SessionAttributeManager(attributesManager);
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
        sessionAttributeManager.updateObject(SCENE, scene);
    }

    public State getState() {
        if (state == null) {
            state = sessionAttributeManager.getState(STATE, State.INTRO);
        }
        return state;
    }

    public void setState(State state) {
        this.state = state;
        sessionAttributeManager.updateObject(STATE, state);
    }

    public State getPendingState() {
        if (pendingState == null) {
            pendingState = sessionAttributeManager.getState(PENDING_STATE, null);
        }
        return pendingState;
    }

    public void setPendingState(State pendingState) {
        this.pendingState = pendingState;
        sessionAttributeManager.updateObject(PENDING_STATE, state);
    }

    public int getIndex() {
        if (index == null) {
            index = sessionAttributeManager.getInt(STATE_INDEX, 0);
        }
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
        sessionAttributeManager.updateObject(STATE_INDEX, index);
    }

    public int getPendingIndex() {
        if (pendingIndex == null) {
            pendingIndex = sessionAttributeManager.getInt(PENDING_INDEX, 0);
        }
        return pendingIndex;
    }

    public void setPendingIndex(int pendingIndex) {
        this.pendingIndex = pendingIndex;
        sessionAttributeManager.updateObject(PENDING_INDEX, pendingIndex);
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
        if (tierIndex == null) {
            tierIndex = sessionAttributeManager.getInt(TIER_INDEX, 0);
        }
        return tierIndex;
    }

    public String getTierIndexForKey() {
        int index = getTierIndex();
        return index > 0 ? "" + index : "";
    }

    public void setTierIndex(int tierIndex) {
        this.tierIndex = tierIndex;
        sessionAttributeManager.updateObject(TIER_INDEX, tierIndex);
    }

    public int getMissionIndex() {
        if (missionIndex == null) {
            missionIndex = sessionAttributeManager.getInt(MISSION_INDEX, 0);
        }
        return missionIndex;
    }

    public void setMissionIndex(int missionIndex) {
        this.missionIndex = missionIndex;
        sessionAttributeManager.updateObject(MISSION_INDEX, missionIndex);
    }

    public int getLocationIndex() {
        if (locationIndex == null) {
            locationIndex = sessionAttributeManager.getInt(LOCATION_INDEX, 0);
        }
        return locationIndex;
    }

    public void setLocationIndex(int locationIndex) {
        this.locationIndex = locationIndex;
        sessionAttributeManager.updateObject(LOCATION_INDEX, locationIndex);
    }

    public int getSceneIndex() {
        if (sceneIndex == null) {
            sceneIndex = sessionAttributeManager.getInt(SCENE_INDEX, 0);
        }
        return sceneIndex;
    }

    public void setSceneIndex(int sceneIndex) {
        this.sceneIndex = sceneIndex;
        sessionAttributeManager.updateObject(SCENE_INDEX, sceneIndex);
    }

    public String getIntroOutroId(State state) {
        if (state == State.INTRO) {
            return getIntroId();
        } else {
            return getOutroId();
        }
    }

    public HelpState getHelpState() {
        if (helpState == null) {
            helpState = sessionAttributeManager.getHelpState(HELP_STATE);
        }
        return helpState;
    }

    public void setHelpState(HelpState helpState) {
        this.helpState = helpState;
        sessionAttributeManager.updateObject(HELP_STATE, helpState);
    }

    public String getImageToInsert() {
        return imageToInsert;
    }

    public void setImageToInsert(String imageToInsert) {
        this.imageToInsert = imageToInsert;
    }


    public String getImage1ToInsert() {
        return image1ToInsert;
    }

    public void setImage1ToInsert(String image1ToInsert) {
        this.image1ToInsert = image1ToInsert;
    }
}
