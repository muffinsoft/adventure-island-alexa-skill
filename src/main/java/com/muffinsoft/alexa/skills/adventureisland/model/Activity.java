package com.muffinsoft.alexa.skills.adventureisland.model;

import java.util.List;

public class Activity {

    private String name;
    private List<String> obstaclesTier1;
    private List<String> obstaclesTier2;
    private List<String> obstaclesTier3;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getObstaclesTier1() {
        return obstaclesTier1;
    }

    public void setObstaclesTier1(List<String> obstaclesTier1) {
        this.obstaclesTier1 = obstaclesTier1;
    }

    public List<String> getObstaclesTier2() {
        return obstaclesTier2;
    }

    public void setObstaclesTier2(List<String> obstaclesTier2) {
        this.obstaclesTier2 = obstaclesTier2;
    }

    public List<String> getObstaclesTier3() {
        return obstaclesTier3;
    }

    public void setObstaclesTier3(List<String> obstaclesTier3) {
        this.obstaclesTier3 = obstaclesTier3;
    }
}
