package com.muffinsoft.alexa.skills.adventureisland.model;

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

    public StateItem nextActivity(StateItem state) {

        if (state.getState() == State.INTRO) {
            state.setState(State.ACTION);
            return state;
        }

        int missionIndex = getIndexByName(missions, state.getMission());
        Mission currentMission = missions.get(missionIndex);
        int locationIndex = getIndexByName(currentMission.getLocations(), state.getLocation());
        Location currentLocation = currentMission.getLocations().get(locationIndex);
        int activityIndex = getIndexByName(currentLocation.getActivities(), state.getActivity());

        if (activityIndex < currentLocation.getActivities().size() - 1) {
            state.setActivity(currentLocation.getActivities().get(activityIndex + 1).getName());
            return state;
        }

        if (state.getState() == State.ACTION) {
            state.setState(State.OUTRO);
            return state;
        }

        if (locationIndex < currentMission.getLocations().size() - 1) {
            Location nextLocation = currentMission.getLocations().get(locationIndex + 1);
            state.setLocation(nextLocation.getName());
            state.setActivity(nextLocation.getActivities().get(0).getName());
            state.setState(State.INTRO);
        }

        return state;
    }
}
