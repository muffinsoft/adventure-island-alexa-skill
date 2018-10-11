package com.muffinsoft.alexa.skills.adventureisland.model;

public class ObstacleItem {

    private String name;
    private String response;
    private String preObstacle;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getPreObstacle() {
        return preObstacle;
    }

    public void setPreObstacle(String preObstacle) {
        this.preObstacle = preObstacle;
    }
}
