package com.muffinsoft.alexa.skills.adventureisland.model;

import java.util.List;

public class Activity {

    private String name;
    private List<ObstacleItem> obstacles;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ObstacleItem> getObstacles() {
        return obstacles;
    }

    public void setObstacles(List<ObstacleItem> obstacles) {
        this.obstacles = obstacles;
    }
}
