package com.muffinsoft.alexa.skills.adventureisland.model;

public enum MissionAchievement {

    PERFECT_SCENE(0), PERFECT_LOCATION(1), PERFECT_MISSION(2), PERFECT_ALL(3);

    public int index;

    MissionAchievement(int index) {
        this.index = index;
    }

}
