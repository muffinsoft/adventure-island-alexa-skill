package com.muffinsoft.alexa.skills.adventureisland.model;

import com.muffinsoft.alexa.skills.adventureisland.content.Constants;
import com.muffinsoft.alexa.skills.adventureisland.content.PhraseManager;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

public class Game {

    private List<Mission> missions = new ArrayList<>();

    public List<Mission> getMissions() {
        return missions;
    }

    public void setMissions(List<Mission> missions) {
        this.missions = missions;
    }

    public void addMission(Mission mission) {
        missions.add(mission);
    }

    public <T extends Named> int getIndexByName(List<T> entities, String name) {
        for (T t : entities) {
            String nameAsKey = PhraseManager.nameToKey(t.getName());
            if (Objects.equals(name, nameAsKey)) {
                return entities.indexOf(t);
            }
        }
        throw new NoSuchElementException("Element " + name + " was not found");
    }

    public <T extends Named> T getEntityByName(List<T> entities, String name) {
        for (T t : entities) {
            String nameAsKey = PhraseManager.nameToKey(t.getName());
            if (Objects.equals(name, nameAsKey)) {
                return t;
            }
        }
        throw new NoSuchElementException("Element " + name + " was not found");
    }

    public String nextObstacle(StateItem state) {
        Mission currentMission = getEntityByName(missions, state.getMission());
        Location currentLocation = getEntityByName(currentMission.getLocations(), state.getLocation());
        Activity currentActivity = getEntityByName(currentLocation.getActivities(), state.getScene());
        List<String> obstacles = currentActivity.getObstaclesTier1();
        int obstacleIndex = state.getIndex();
        if (obstacleIndex >= obstacles.size()) {
            obstacleIndex = obstacleIndex % obstacles.size();
        }
        return obstacles.get(obstacleIndex);
    }

    public StateItem nextActivity(StateItem state) {

        // root menu, no mission selected. Normally this shouldn't be called, as
        // we do not go to the first mission automatically
        if (Objects.equals(state.getMission(), Constants.ROOT)) {
            String nextMissionName = missions.get(0).getName();
            String nextMissionKey = PhraseManager.nameToKey(nextMissionName);
            state.setMission(nextMissionKey);
            state.setLocation(nextMissionKey);
            state.setScene(nextMissionKey);
            return state;
        }

        int missionIndex = getIndexByName(missions, state.getMission());
        Mission currentMission = missions.get(missionIndex);

        // mission intro played, need to go to the first location
        if (Objects.equals(state.getMission(), state.getLocation())) {
            String nextLocationName = currentMission.getLocations().get(0).getName();
            String nextLocationKey = PhraseManager.nameToKey(nextLocationName);
            state.setLocation(nextLocationKey);
            state.setScene(nextLocationKey);
            return state;
        }

        int locationIndex = getIndexByName(currentMission.getLocations(), state.getLocation());
        Location currentLocation = currentMission.getLocations().get(locationIndex);

        // location intro played, need to go to the first activity
        if (Objects.equals(state.getLocation(), state.getScene())) {
            String nextActivityName = currentLocation.getActivities().get(0).getName();
            String nextActivityKey = PhraseManager.nameToKey(nextActivityName);
            state.setScene(nextActivityKey);
            return state;
        }

        // activity intro played, go to action
        if (state.getState() == State.INTRO) {
            state.setState(State.ACTION);
            return state;
        }

        int activityIndex = getIndexByName(currentLocation.getActivities(), state.getScene());

        // next activity (scene)
        if (activityIndex < currentLocation.getActivities().size() - 1) {
            state.setScene(currentLocation.getActivities().get(activityIndex + 1).getName());
            return state;
        }

        // no more activities in the location, play outro for the scene
        if (state.getState() == State.ACTION) {
            state.setState(State.OUTRO);
            return state;
        }

        // proceed to the next location
        if (locationIndex < currentMission.getLocations().size() - 1) {
            Location nextLocation = currentMission.getLocations().get(locationIndex + 1);
            state.setLocation(nextLocation.getName());
            state.setScene(nextLocation.getActivities().get(0).getName());
            state.setState(State.INTRO);
            return state;
        }

        // we do not go to the next mission automatically, return to the 'main menu'
        state.setMission(Constants.ROOT);
        return state;
    }
}
