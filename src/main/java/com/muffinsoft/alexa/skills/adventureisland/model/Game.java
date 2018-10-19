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
            Mission nextMission = missions.get(0);
            String nextMissionName = nextMission.getName();
            String nextMissionKey = PhraseManager.nameToKey(nextMissionName);
            state.setMission(nextMissionKey);
            state.setLocation(nextMissionKey);
            state.setScene(nextMissionKey);
            state.setIndex(0);
            state.setIntroId(nextMission.getIntroId());
            state.setOutroId(nextMission.getOutroId());
            return state;
        }

        int missionIndex = getIndexByName(missions, state.getMission());
        Mission currentMission = missions.get(missionIndex);

        // mission intro played, need to go to the first location
        if (Objects.equals(state.getMission(), state.getLocation())) {
            Location nextLocation = currentMission.getLocations().get(0);
            String nextLocationName = nextLocation.getName();
            String nextLocationKey = PhraseManager.nameToKey(nextLocationName);
            state.setLocation(nextLocationKey);
            state.setScene(nextLocationKey);
            state.setIndex(0);
            state.setIntroId(nextLocation.getIntroId());
            state.setOutroId(nextLocation.getOutroId());
            return state;
        }

        int locationIndex = getIndexByName(currentMission.getLocations(), state.getLocation());
        Location currentLocation = currentMission.getLocations().get(locationIndex);

        // location intro played, need to go to the first activity
        if (Objects.equals(state.getLocation(), state.getScene())) {
            Activity nextActivity = currentLocation.getActivities().get(0);
            String nextActivityName = nextActivity.getName();
            String nextActivityKey = PhraseManager.nameToKey(nextActivityName);
            state.setScene(nextActivityKey);
            state.setIndex(0);
            state.setIntroId(nextActivity.getIntroId());
            state.setOutroId(nextActivity.getOutroId());
            return state;
        }

        // activity intro played, go to action
        if (state.getState() == State.INTRO) {
            state.setState(State.ACTION);
            state.setIndex(0);
            return state;
        }

        int activityIndex = getIndexByName(currentLocation.getActivities(), state.getScene());

        // next activity (scene)
        if (activityIndex < currentLocation.getActivities().size() - 1) {
            Activity nextActivity = currentLocation.getActivities().get(activityIndex + 1);
            state.setScene(nextActivity.getName());
            state.setState(State.INTRO);
            state.setIndex(0);
            state.setIntroId(nextActivity.getIntroId());
            state.setOutroId(nextActivity.getOutroId());
            return state;
        }

        // no more activities in the location, play outro for the location
        if (state.getState() == State.ACTION) {
            state.setScene(state.getLocation());
            state.setState(State.OUTRO);
            state.setIndex(0);
            return state;
        }

        // proceed to the next location
        if (locationIndex < currentMission.getLocations().size() - 1) {
            Location nextLocation = currentMission.getLocations().get(locationIndex + 1);
            state.setLocation(nextLocation.getName());
            state.setScene(nextLocation.getActivities().get(0).getName());
            state.setState(State.INTRO);
            state.setIndex(0);
            state.setIntroId(nextLocation.getIntroId());
            state.setOutroId(nextLocation.getOutroId());
            return state;
        }

        // we do not go to the next mission automatically, return to the 'main menu'
        state.setMission(Constants.ROOT);
        return state;
    }
}
