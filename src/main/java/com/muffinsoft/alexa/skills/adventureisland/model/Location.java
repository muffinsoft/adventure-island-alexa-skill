package com.muffinsoft.alexa.skills.adventureisland.model;

import java.util.List;

public class Location extends Named {

    private List<Activity> activities;

    public List<Activity> getActivities() {
        return activities;
    }

    public void setActivities(List<Activity> activities) {
        this.activities = activities;
    }
}
