package com.muffinsoft.alexa.skills.adventureisland.game;

import com.muffinsoft.alexa.skills.adventureisland.content.AchievementManager;
import com.muffinsoft.alexa.skills.adventureisland.model.Achievement;
import com.muffinsoft.alexa.skills.adventureisland.model.MissionAchievement;
import com.muffinsoft.alexa.skills.adventureisland.model.StateItem;

import java.util.List;

import static com.muffinsoft.alexa.skills.adventureisland.content.Constants.SCENES_PER_LOCATION;
import static com.muffinsoft.alexa.skills.adventureisland.content.Constants.SCENES_PER_MISSION;

public class AchievementChecker {

    public static String getPerfectScene(List<String> achievements, String mission, int hits) {
        if (hits == 0) {
            Achievement achievement = AchievementManager.getAchievementForMission(mission, MissionAchievement.PERFECT_SCENE);
            if (!achievements.contains(achievement.getName())) {
                achievements.add(achievement.getName());
                return achievement.getExplanation();
            }
        }
        return null;
    }

    public static String getPerfectLocation(List<String> achievements, StateItem state, List<String> hitsHistory) {
        int firstIndex = state.getTierIndex() * SCENES_PER_MISSION + state.getLocationIndex() * SCENES_PER_LOCATION;
        List<String> locationHits = hitsHistory.subList(firstIndex, firstIndex + SCENES_PER_LOCATION);
        if (locationHits.stream().allMatch(o -> o.equals("0"))) {
            Achievement achievement = AchievementManager.getAchievementForMission(state.getMission(), MissionAchievement.PERFECT_LOCATION);
            if (!achievements.contains(achievement.getName())) {
                achievements.add(achievement.getName());
                return achievement.getExplanation();
            }
        }
        return null;
    }

    public static String getPerfectMission(List<String> achievements, StateItem state, List<String> hitsHistory) {
        int firstIndex = state.getTierIndex() * SCENES_PER_MISSION;
        List<String> missionHits = hitsHistory.subList(firstIndex, firstIndex + SCENES_PER_MISSION);
        if (missionHits.stream().allMatch(o -> o.equals("0"))) {
            Achievement achievement = AchievementManager.getAchievementForMission(state.getMission(), MissionAchievement.PERFECT_MISSION);
            if (!achievements.contains(achievement.getName())) {
                achievements.add(achievement.getName());
                return achievement.getExplanation();
            }
        }
        return null;
    }

}
