package com.muffinsoft.alexa.skills.adventureisland.model;

import com.amazon.ask.attributes.AttributesManager;
import com.muffinsoft.alexa.skills.adventureisland.game.SessionAttributeManager;

import java.util.List;

import static com.muffinsoft.alexa.skills.adventureisland.content.AttributeKeys.*;
import static com.muffinsoft.alexa.skills.adventureisland.content.Constants.HEADS_UP;
import static com.muffinsoft.alexa.skills.adventureisland.content.Constants.HEALTH;
import static com.muffinsoft.alexa.skills.adventureisland.content.NumbersManager.getNumber;
import static com.muffinsoft.alexa.skills.adventureisland.content.NumbersManager.getTurnsToNextExclamation;

public class GameProperties {

    private Integer health;
    private Integer coins;
    private String currentObstacle;
    private Integer toNextExclamation;
    private Integer toNextHeadsUp;
    private boolean skipReadyPrompt;
    private Boolean justFailed;
    private List<String> powerups;

    private final SessionAttributeManager sessionAttributeManager;

    public GameProperties(AttributesManager attributesManager) {
        this.sessionAttributeManager = new SessionAttributeManager(attributesManager);
    }

    public int getHealth() {
        if (health == null) {
            health = sessionAttributeManager.getInt(HEALTH, getNumber(HEALTH));
        }
        return health;
    }

    public void decrementHealth() {
        health = getHealth() - 1;
        sessionAttributeManager.updateObject(HEALTH, health);
    }

    public void resetHealth() {
        this.health = getNumber(HEALTH);
        sessionAttributeManager.updateObject(HEALTH, health);
    }

    public int getCoins() {
        if (coins == null) {
            coins = sessionAttributeManager.getInt(COINS, 0);
        }
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
        sessionAttributeManager.updateObject(COINS, coins);
    }

    public void addCoin() {
        coins = getCoins() + 1;
        sessionAttributeManager.updateObject(COINS, coins);
    }

    public String getCurrentObstacle() {
        if (currentObstacle == null) {
            currentObstacle = sessionAttributeManager.getString(OBSTACLE);
        }
        return currentObstacle;
    }

    public void setCurrentObstacle(String currentObstacle) {
        this.currentObstacle = currentObstacle;
        sessionAttributeManager.updateObject(OBSTACLE, currentObstacle);
    }

    public int decrementAndGetToNextExclamation() {
        if (toNextExclamation == null) {
            toNextExclamation = sessionAttributeManager.getInt(TURNS_TO_NEXT_EXCLAMATION, getTurnsToNextExclamation());
        }
        toNextExclamation--;
        sessionAttributeManager.updateObject(TURNS_TO_NEXT_EXCLAMATION, toNextExclamation);
        return toNextExclamation;
    }

    public void setToNextExclamation(int toNextExclamation) {
        this.toNextExclamation = toNextExclamation;
        sessionAttributeManager.updateObject(TURNS_TO_NEXT_EXCLAMATION, toNextExclamation);
    }

    public int decrementAndGetToNextHeadsUp() {
        if (toNextHeadsUp == null) {
            toNextHeadsUp = sessionAttributeManager.getInt(TURNS_TO_NEXT_HEADS_UP, getNumber(HEADS_UP));
        }
        toNextHeadsUp--;
        sessionAttributeManager.updateObject(TURNS_TO_NEXT_HEADS_UP, toNextHeadsUp);
        return toNextHeadsUp;
    }

    public void resetToNextHeadsUp() {
        toNextHeadsUp = getNumber(HEADS_UP);
        sessionAttributeManager.updateObject(TURNS_TO_NEXT_HEADS_UP, toNextHeadsUp);
    }

    public boolean isSkipReadyPrompt() {
        return skipReadyPrompt;
    }

    public void setSkipReadyPrompt(boolean skipReadyPrompt) {
        this.skipReadyPrompt = skipReadyPrompt;
    }

    public boolean isJustFailed() {
        if (justFailed == null) {
            justFailed = sessionAttributeManager.getBoolean(JUST_FAILED);
        }
        return justFailed;
    }

    public void setJustFailed(boolean justFailed) {
        this.justFailed = justFailed;
        sessionAttributeManager.updateObject(JUST_FAILED, justFailed ? "yes" : null);
    }

    public List<String> getPowerups() {
        if (powerups == null) {
            powerups = sessionAttributeManager.getStringList(POWERUPS);
        }
        return powerups;
    }

    public void setPowerups(List<String> powerups) {
        this.powerups = powerups;
        sessionAttributeManager.updateObject(POWERUPS, powerups);
    }

    public void resetPowerups() {
        powerups.clear();
        sessionAttributeManager.updateObject(POWERUPS, powerups);
    }
}
