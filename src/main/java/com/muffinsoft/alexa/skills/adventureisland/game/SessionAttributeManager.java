package com.muffinsoft.alexa.skills.adventureisland.game;

import com.amazon.ask.attributes.AttributesManager;
import com.muffinsoft.alexa.skills.adventureisland.model.HelpState;
import com.muffinsoft.alexa.skills.adventureisland.model.State;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.muffinsoft.alexa.skills.adventureisland.game.Utils.verifyMap;

@SuppressWarnings("unchecked")
public class SessionAttributeManager {

    private final Map<String, Object> sessionAttributes;

    public SessionAttributeManager(AttributesManager attributesManager) {
        this.sessionAttributes = verifyMap(attributesManager.getSessionAttributes());
        attributesManager.setSessionAttributes(sessionAttributes);
    }

    public List<String> getStringList(String key) {
        return (List<String>) sessionAttributes.getOrDefault(key, new ArrayList<>());
    }

    public String getString(String key, String defaultValue) {
        return String.valueOf(sessionAttributes.getOrDefault(key, defaultValue));
    }

    public String getString(String key) {
        return (String) sessionAttributes.get(key);
    }

    public State getState(String key, State defaultValue) {
        String stateStr = String.valueOf(sessionAttributes.getOrDefault(key, defaultValue));
        if (stateStr != null && !Objects.equals(stateStr, "null")) {
            return State.valueOf(stateStr);
        } else {
            return null;
        }
    }

    public HelpState getHelpState(String key) {
        String stateStr = String.valueOf(sessionAttributes.get(key));
        if (stateStr != null && !Objects.equals(stateStr, "null")) {
            return HelpState.valueOf(stateStr);
        } else {
            return null;
        }
    }

    public int getInt(String key, int defaultValue) {
        return (int) sessionAttributes.getOrDefault(key, defaultValue);
    }

    public boolean getBoolean(String key) {
        return sessionAttributes.get(key) != null;
    }

    public void updateObject(String key, Object object) {
        sessionAttributes.put(key, object);
    }

}
