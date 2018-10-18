package com.muffinsoft.alexa.skills.adventureisland.model;

import java.util.List;

public class ObstacleItem {

    private String name;
    private List<String> responses;
    private String preObstacle;
    private String headsUp;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getResponses() {
        return responses;
    }

    public void setResponses(List<String> responses) {
        this.responses = responses;
    }

    public String getPreObstacle() {
        return preObstacle;
    }

    public void setPreObstacle(String preObstacle) {
        this.preObstacle = preObstacle;
    }

    public String getHeadsUp() {
        return headsUp;
    }

    public void setHeadsUp(String headsUp) {
        this.headsUp = headsUp;
    }
}
