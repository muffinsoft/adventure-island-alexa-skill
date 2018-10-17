package com.muffinsoft.alexa.skills.adventureisland.model;

import java.util.List;

public class Mission extends Named {

    private List<Location> locations;

    public List<Location> getLocations() {
        return locations;
    }

    public void setLocations(List<Location> locations) {
        this.locations = locations;
    }
}
