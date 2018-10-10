package com.muffinsoft.alexa.skills.adventureisland.game;

import com.amazon.ask.attributes.AttributesManager;
import com.amazon.ask.model.Slot;

import java.util.Map;

public class SessionStateManager {

    private Map<String, Slot> slots;
    private AttributesManager attributesManager;

    public SessionStateManager(Map<String, Slot> slots, AttributesManager attributesManager) {
        this.slots = slots;
        this.attributesManager = attributesManager;
    }
}
