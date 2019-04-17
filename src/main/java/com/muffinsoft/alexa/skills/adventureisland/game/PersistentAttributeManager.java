package com.muffinsoft.alexa.skills.adventureisland.game;

import com.amazon.ask.attributes.AttributesManager;
import com.muffinsoft.alexa.skills.adventureisland.model.PurchaseState;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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

    public PurchaseState getPurchaseState(String key, PurchaseState defaultValue) {
        String stateStr = String.valueOf(persistentAttributes.getOrDefault(key, defaultValue.name()));
        if (stateStr != null && !Objects.equals(stateStr, "null")) {
            return PurchaseState.valueOf(stateStr);
        } else {
            return null;
        }
    }

    public ZonedDateTime getDateTime(String key) {
        String dateTimeStr = String.valueOf(persistentAttributes.get(key));
        if (dateTimeStr != null && !Objects.equals(dateTimeStr, "null")) {
            return ZonedDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_INSTANT);
        } else {
            return null;
        }
    }

    public void setDateTime(String key, ZonedDateTime dateTime) {
        persistentAttributes.put(key, dateTime.format(DateTimeFormatter.ISO_INSTANT));
    }

    public void updateObject(String key, Object object) {
        persistentAttributes.put(key, object);
    }
}
