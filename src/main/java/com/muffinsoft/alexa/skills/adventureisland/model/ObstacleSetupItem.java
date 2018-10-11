package com.muffinsoft.alexa.skills.adventureisland.model;

import java.util.List;

public class ObstacleSetupItem {

    private String explanation;
    private List<Integer> obstacleIndices;

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public List<Integer> getObstacleIndices() {
        return obstacleIndices;
    }

    public void setObstacleIndices(List<Integer> obstacleIndices) {
        this.obstacleIndices = obstacleIndices;
    }
}
