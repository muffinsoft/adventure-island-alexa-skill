package com.muffinsoft.alexa.skills.adventureisland.game;

import com.amazon.ask.attributes.AttributesManager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.muffinsoft.alexa.skills.adventureisland.game.Utils.verifyMap;

@SuppressWarnings("unchecked")
public class PersistentAttributeManager {

    private final Map<String, Object> persistentAttributes;

    public PersistentAttributeManager(AttributesManager attributesManager) {
        this.persistentAttributes = attributesManager.getPersistentAttributes();
    }

    public int getInt(String key) {
        BigDecimal value = (BigDecimal) persistentAttributes.getOrDefault(key, BigDecimal.ZERO);
        return value.intValue();
    }

    public List<String> getStringList(String key) {
        return (List<String>) persistentAttributes.getOrDefault(key, new ArrayList<>());
    }

    public Map<String, List<String>> getMapWithList(String key) {
        return (Map<String, List<String>>) persistentAttributes.getOrDefault(key, new HashMap<>());
    }

    public List<BigDecimal> getBigDecimalList(String key) {
        return (List<BigDecimal>) persistentAttributes.get(key);
    }

    public List<List<BigDecimal>> getBDListOfLists(String key) {
        return (List<List<BigDecimal>>) persistentAttributes.getOrDefault(key, new ArrayList<>());
    }

    public void updateObject(String key, Object object) {
        persistentAttributes.put(key, object);
    }
}
