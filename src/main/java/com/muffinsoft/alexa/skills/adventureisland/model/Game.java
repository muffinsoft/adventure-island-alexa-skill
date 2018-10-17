package com.muffinsoft.alexa.skills.adventureisland.model;

import java.util.List;

public class Game {

    private List<Mission> missions;

    public List<Mission> getMissions() {
        return missions;
    }

    public void setMissions(List<Mission> missions) {
        this.missions = missions;
    }
}
