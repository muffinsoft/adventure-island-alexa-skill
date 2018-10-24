package com.muffinsoft.alexa.skills.adventureisland.model;

import java.util.List;

public class Mission extends Named {

    private List<String> tierNames;
    private List<Location> locations;

    public List<String> getTierNames() {
        return tierNames;
    }

    public void setTierNames(List<String> tierNames) {
        this.tierNames = tierNames;
    }

    public List<Location> getLocations() {
        return locations;
    }

    public void setLocations(List<Location> locations) {
        this.locations = locations;
    }
}
