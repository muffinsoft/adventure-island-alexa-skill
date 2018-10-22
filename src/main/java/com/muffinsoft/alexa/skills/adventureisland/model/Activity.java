package com.muffinsoft.alexa.skills.adventureisland.model;

import java.util.List;

public class Activity extends Named {

    private List<List<String>> obstacles;

    public List<List<String>> getObstacles() {
        return obstacles;
    }

    public void setObstacles(List<List<String>> obstacles) {
        this.obstacles = obstacles;
    }
}
