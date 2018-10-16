package com.muffinsoft.alexa.skills.adventureisland.model;

import java.util.List;

public class Mission {

    private String name;
    private List<Location> locations;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Location> getLocations() {
        return locations;
    }

    public void setLocations(List<Location> locations) {
        this.locations = locations;
    }
}
