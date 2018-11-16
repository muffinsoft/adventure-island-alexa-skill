package com.muffinsoft.alexa.skills.adventureisland.model;

import com.amazon.ask.attributes.AttributesManager;
import com.muffinsoft.alexa.skills.adventureisland.game.PersistentAttributeManager;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.muffinsoft.alexa.skills.adventureisland.content.AttributeKeys.*;

public class PersistentState {

    private String userName = "my friend";
    private Integer totalCoins;
    private List<String> visitedLocations;
    private List<String> oldObstacles;
    /**
     * Index of the external list is tier, it contains a list of indices of completed missions
     */
    private List<List<BigDecimal>> completedMissions;
    /**
     * Checkpoint contains 4 integers: tier, mission, location, and last successful scene
     */
    private List<BigDecimal> checkpoint;

    /**
     * Earned nicknames are mapped to mission name keys (missionName).
     */
    private Map<String, List<String>> nicknames;
    private Map<String, List<String>> achievements;
    /**
     * How many times the user was hit in the mission, contains 36 digits as String (4*3 per mission * 3 tiers)
     */
    private Map<String, List<String>> hitsHistory;

    private Map<String, List<String>> locationIntros;
    private Map<String, List<String>> sceneIntros;

    private final PersistentAttributeManager persistentAttributeManager;

    public PersistentState(AttributesManager attributesManager) {
        this.persistentAttributeManager = new PersistentAttributeManager(attributesManager);
    }


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getTotalCoins() {
        if (totalCoins == null) {
            totalCoins = persistentAttributeManager.getInt(TOTAL_COINS);
        }
        return totalCoins;
    }

    public void addCoins(int coins) {
        setTotalCoins(getTotalCoins() + coins);
    }

    public void setTotalCoins(int totalCoins) {
        this.totalCoins = totalCoins;
        persistentAttributeManager.updateObject(TOTAL_COINS, totalCoins);
    }

    public List<String> getVisitedLocations() {
        if (visitedLocations == null) {
            visitedLocations = persistentAttributeManager.getStringList(VISITED_LOCATIONS);
        }
        return visitedLocations;
    }

    public void addVisitedLocation(String location) {
        getVisitedLocations().add(location);
        persistentAttributeManager.updateObject(VISITED_LOCATIONS, visitedLocations);
    }

    public List<String> getOldObstacles() {
        if (oldObstacles == null) {
            oldObstacles = persistentAttributeManager.getStringList(OLD_OBSTACLES);
        }
        return oldObstacles;
    }

    public void addOldObstacle(String obstacle) {
        getOldObstacles().add(obstacle);
        persistentAttributeManager.updateObject(OLD_OBSTACLES, oldObstacles);
    }

    public List<List<BigDecimal>> getCompletedMissions() {
        if (completedMissions == null) {
            completedMissions = persistentAttributeManager.getBDListOfLists(COMPLETED_MISSIONS);
        }
        return completedMissions;
    }

    public void setCompletedMissions(List<List<BigDecimal>> completedMissions) {
        this.completedMissions = completedMissions;
        persistentAttributeManager.updateObject(COMPLETED_MISSIONS, completedMissions);
    }

    public List<BigDecimal> getCheckpoint() {
        if (checkpoint == null) {
            checkpoint = persistentAttributeManager.getBigDecimalList(CHECKPOINT);
        }
        return checkpoint;
    }

    public void setCheckpoint(List<BigDecimal> checkpoint) {
        this.checkpoint = checkpoint;
        persistentAttributeManager.updateObject(CHECKPOINT, checkpoint);
    }

    public Map<String, List<String>> getNicknames() {
        if (nicknames == null) {
            nicknames = persistentAttributeManager.getMapWithList(NICKNAMES);
        }
        return nicknames;
    }

    public void addNickname(String key, List<String> nickname) {
        getNicknames().put(key, nickname);
        persistentAttributeManager.updateObject(NICKNAMES, nicknames);
    }

    public Map<String, List<String>> getAchievements() {
        if (achievements == null) {
            achievements = persistentAttributeManager.getMapWithList(NICKNAMES);
        }
        return achievements;
    }

    public void setAchievements(Map<String, List<String>> achievements) {
        this.achievements = achievements;
        persistentAttributeManager.updateObject(ACHIEVEMENTS, achievements);
    }

    public Map<String, List<String>> getHitsHistory() {
        if (hitsHistory == null) {
            hitsHistory = persistentAttributeManager.getMapWithList(HITS_HISTORY);
        }
        return hitsHistory;
    }

    public void setHitsHistory(Map<String, List<String>> hitsHistory) {
        this.hitsHistory = hitsHistory;
        persistentAttributeManager.updateObject(HITS_HISTORY, hitsHistory);
    }

    public Map<String, List<String>> getLocationIntros() {
        if (locationIntros == null) {
            locationIntros = persistentAttributeManager.getMapWithList(LOCATION_INTROS);
        }
        return locationIntros;
    }

    public void addLocationIntro(String key, List<String> locationIntros) {
        getLocationIntros().put(key, locationIntros);
        persistentAttributeManager.updateObject(LOCATION_INTROS, locationIntros);
    }

    public Map<String, List<String>> getSceneIntros() {
        if (sceneIntros == null) {
            sceneIntros = persistentAttributeManager.getMapWithList(SCENE_INTROS);
        }
        return sceneIntros;
    }

    public void addSceneIntro(String key, List<String> sceneIntros) {
        getSceneIntros().put(key, sceneIntros);
        persistentAttributeManager.updateObject(SCENE_INTROS, sceneIntros);
    }
}
