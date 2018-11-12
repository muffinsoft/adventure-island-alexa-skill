package com.muffinsoft.alexa.skills.adventureisland.content;

import com.fasterxml.jackson.core.type.TypeReference;
import com.muffinsoft.alexa.skills.adventureisland.model.Achievement;
import com.muffinsoft.alexa.skills.adventureisland.model.MissionAchievement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AchievementManager {

    private static final String PATH = "phrases/achievements.json";
    private static Map<String, List<Achievement>> achievements = new HashMap<>();

    static {
        achievements = Constants.contentLoader.loadContent(achievements, PATH, new TypeReference<Map<String, List<Achievement>>>(){});
    }

    public static Achievement getAchievementForMission(String mission, MissionAchievement achievement) {
        return achievements.get(mission).get(achievement.index);
    }

}
