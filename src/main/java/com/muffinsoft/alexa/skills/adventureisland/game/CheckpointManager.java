package com.muffinsoft.alexa.skills.adventureisland.game;

import com.muffinsoft.alexa.skills.adventureisland.model.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.muffinsoft.alexa.skills.adventureisland.content.Constants.CONTINUE;
import static com.muffinsoft.alexa.skills.adventureisland.content.Constants.game;
import static com.muffinsoft.alexa.skills.adventureisland.content.PhraseManager.nameToKey;

class CheckpointManager {

    private CheckpointManager() {}

    static void setCheckpoint(StateItem stateItem, PersistentState persistentState) {
        int locationIndex = stateItem.getLocationIndex();

        List<Location> locations = game.getMissions().get(stateItem.getMissionIndex()).getLocations();
        List<Activity> scenes = locations.get(locationIndex).getActivities();

        int sceneIndex = stateItem.getSceneIndex() + 1;
        // location finished, proceed to next location
        if (sceneIndex >= scenes.size()) {
            sceneIndex = 0;
            locationIndex = stateItem.getLocationIndex() + 1;
            // location finished, nothing to save
            if (locationIndex >= locations.size()) {
                return;
            }
        }
        List<BigDecimal> checkpoint = new ArrayList<>();
        checkpoint.add(BigDecimal.valueOf(stateItem.getTierIndex()));
        checkpoint.add(BigDecimal.valueOf(stateItem.getMissionIndex()));
        checkpoint.add(BigDecimal.valueOf(locationIndex));
        checkpoint.add(BigDecimal.valueOf(sceneIndex));

        persistentState.setCheckpoint(checkpoint);
    }

    static void restoreFromCheckpoint(StateItem stateItem, PersistentState persistentState, String userReply) {
        List<BigDecimal> checkpoint = persistentState.getCheckpoint();
        if (checkpoint != null && Objects.equals(CONTINUE, userReply)) {
            stateItem.setTierIndex(checkpoint.get(0).intValue());
            stateItem.setMissionIndex(checkpoint.get(1).intValue());
            stateItem.setLocationIndex(checkpoint.get(2).intValue());
            stateItem.setSceneIndex(checkpoint.get(3).intValue());
            Mission currentMission = game.getMissions().get(stateItem.getMissionIndex());
            stateItem.setMission(nameToKey(currentMission.getName()));
            Location currentLocation = currentMission.getLocations().get(stateItem.getLocationIndex());
            stateItem.setLocation(nameToKey(currentLocation.getName()));
            stateItem.setScene(nameToKey(currentLocation.getActivities().get(stateItem.getSceneIndex()).getName()));
        }
        stateItem.setState(State.INTRO);
    }

}
