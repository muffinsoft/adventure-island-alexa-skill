package com.muffinsoft.alexa.skills.adventureisland.model;

import java.util.List;
import java.util.Map;

public class CoinItem {
    private String name;
    private List<String> responses;
    private String preObstacle;
    private Map<String, String> headsUp;

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

    public Map<String, String> getHeadsUp() {
        return headsUp;
    }

    public void setHeadsUp(Map<String, String> headsUp) {
        this.headsUp = headsUp;
    }
}
