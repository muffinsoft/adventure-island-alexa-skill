package com.muffinsoft.alexa.skills.adventureisland.model;

import com.muffinsoft.alexa.skills.adventureisland.content.Constants;
import com.muffinsoft.alexa.skills.adventureisland.content.PhraseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

public class Game {

    private static final Logger logger = LoggerFactory.getLogger(Game.class);

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

    public String nextObstacle(StateItem state) {
        Activity currentActivity = missions.get(state.getMissionIndex()).getLocations().get(state.getLocationIndex()).getActivities().get(state.getSceneIndex());
        List<String> obstacles = currentActivity.getObstacles().get(state.getTierIndex());
        int obstacleIndex = state.getIndex();
        if (obstacleIndex >= obstacles.size()) {
            obstacleIndex = obstacleIndex % obstacles.size();
        }
        return obstacles.get(obstacleIndex);
    }

    public StateItem nextActivity(StateItem state) {

        if (state.getState() == State.WELCOME) {
            logger.debug("Changing state from Welcome to Intro");
            state.setState(State.INTRO);
            return state;
        }

        // root menu, no mission selected. Normally this shouldn't be called, as
        // we do not go to the first mission automatically
        if (Objects.equals(state.getMission(), Constants.ROOT)) {
            logger.debug("Going to ROOT");
            Mission nextMission = missions.get(0);
            String nextMissionName = nextMission.getName();
            String nextMissionKey = PhraseManager.nameToKey(nextMissionName);
            state.setMission(nextMissionKey);
            state.setLocation(nextMissionKey);
            state.setScene(nextMissionKey);
            state.setIndex(0);
            state.setMissionIndex(0);
            state.setIntroId(nextMission.getIntroId());
            state.setOutroId(nextMission.getOutroId());
            return state;
        }

        int missionIndex = state.getMissionIndex();
        Mission currentMission = missions.get(missionIndex);

        // mission intro played, need to go to the first location
        if (Objects.equals(state.getMission(), state.getLocation())) {
            logger.debug("Going to a new location");
            Location nextLocation = currentMission.getLocations().get(0);
            String nextLocationName = nextLocation.getName();
            String nextLocationKey = PhraseManager.nameToKey(nextLocationName);
            state.setLocation(nextLocationKey);
            state.setScene(nextLocationKey);
            state.setIndex(0);
            state.setMissionIndex(missionIndex);
            state.setLocationIndex(0);
            state.setIntroId(nextLocation.getIntroId());
            state.setOutroId(nextLocation.getOutroId());
            return state;
        }

        int locationIndex = state.getLocationIndex();
        Location currentLocation = currentMission.getLocations().get(locationIndex);

        // location intro played, need to go to the first activity
        if (Objects.equals(state.getLocation(), state.getScene()) && state.getState() != State.OUTRO) {
            logger.debug("Going to first scene");
            Activity nextActivity = currentLocation.getActivities().get(0);
            String nextActivityName = nextActivity.getName();
            String nextActivityKey = PhraseManager.nameToKey(nextActivityName);
            state.setScene(nextActivityKey);
            state.setIndex(0);
            state.setMissionIndex(missionIndex);
            state.setLocationIndex(locationIndex);
            state.setSceneIndex(0);
            state.setIntroId(nextActivity.getIntroId());
            state.setOutroId(nextActivity.getOutroId());
            return state;
        }

        // activity intro played, go to action
        if (state.getState() == State.INTRO) {
            logger.debug("Going to action");
            state.setState(State.ACTION);
            state.setIndex(0);
            return state;
        }

        int activityIndex = state.getSceneIndex();

        // next activity (scene)
        if (state.getState() != State.OUTRO && activityIndex < currentLocation.getActivities().size() - 1) {
            logger.debug("Going to the next scene");
            Activity nextActivity = currentLocation.getActivities().get(activityIndex + 1);
            state.setScene(PhraseManager.nameToKey(nextActivity.getName()));
            state.setState(State.INTRO);
            state.setIndex(0);
            state.setMissionIndex(missionIndex);
            state.setLocationIndex(locationIndex);
            state.setSceneIndex(activityIndex + 1);
            state.setIntroId(nextActivity.getIntroId());
            state.setOutroId(nextActivity.getOutroId());
            return state;
        }

        // no more activities in the location, play outro for the location
        if (state.getState() == State.ACTION) {
            logger.debug("Quitting action, starting outro");
            state.setScene(state.getLocation());
            state.setState(State.OUTRO);
            state.setIndex(0);
            return state;
        }

        // proceed to the next location
        if (locationIndex < currentMission.getLocations().size() - 1) {
            logger.debug("Going to the next location");
            Location nextLocation = currentMission.getLocations().get(locationIndex + 1);
            state.setLocation(PhraseManager.nameToKey(nextLocation.getName()));
            state.setScene(PhraseManager.nameToKey(nextLocation.getName()));
            state.setState(State.INTRO);
            state.setIndex(0);
            state.setMissionIndex(missionIndex);
            state.setLocationIndex(locationIndex + 1);
            state.setSceneIndex(0);
            state.setIntroId(nextLocation.getIntroId());
            state.setOutroId(nextLocation.getOutroId());
            return state;
        }

        // quit to mission outro
        if (state.getState() == State.OUTRO && !Objects.equals(state.getLocation(), state.getMission())) {
            state.setLocation(state.getMission());
            state.setScene(state.getMission());
            state.setIndex(0);
            return state;
        }

        // we do not go to the next mission automatically, return to the 'main menu'
        logger.debug("Quitting to root");
        state.setMission(Constants.ROOT);
        return state;
    }
}
