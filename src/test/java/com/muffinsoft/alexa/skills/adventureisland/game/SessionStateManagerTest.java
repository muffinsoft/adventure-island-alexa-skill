package com.muffinsoft.alexa.skills.adventureisland.game;

import com.amazon.ask.attributes.AttributesManager;
import com.amazon.ask.attributes.persistence.PersistenceAdapter;
import com.amazon.ask.attributes.persistence.impl.DynamoDbPersistenceAdapter;
import com.amazon.ask.model.RequestEnvelope;
import com.amazon.ask.model.Session;
import com.amazon.ask.model.Slot;
import com.muffinsoft.alexa.skills.adventureisland.content.Constants;
import com.muffinsoft.alexa.skills.adventureisland.content.NumbersManager;
import com.muffinsoft.alexa.skills.adventureisland.content.PhraseManager;
import com.muffinsoft.alexa.skills.adventureisland.model.DialogItem;
import com.muffinsoft.alexa.skills.adventureisland.model.SlotName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.muffinsoft.alexa.skills.adventureisland.content.Constants.*;
import static com.muffinsoft.alexa.skills.adventureisland.content.NumbersManager.getNumber;
import static com.muffinsoft.alexa.skills.adventureisland.content.PhraseManager.getPhrase;
import static com.muffinsoft.alexa.skills.adventureisland.game.SessionStateManager.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SessionStateManagerTest {

    @Mock
    private PersistenceAdapter adapter;


    @Test
    void nextResponseRootIntro() {
        SessionStateManager stateManager = getSessionStateManager(Collections.emptyMap());
        DialogItem dialogItem = stateManager.nextResponse();
        String key = ROOT + capitalizeFirstLetter(Constants.INTRO) + 0;
        assertEquals(PhraseManager.getPhrase(key), dialogItem.getResponseText());
    }

    @Test
    void nextResponseTransitionToFirstMission() {
        String userName = "Test user";
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(MISSION, ROOT);
        attributes.put(LOCATION, ROOT);
        attributes.put(SCENE, ROOT);
        int sceneState = Integer.parseInt(getPhrase(ROOT + capitalizeFirstLetter(INTRO) + COUNT));
        attributes.put(SCENE_STATE, sceneState);
        attributes.put(COINS, 0);
        attributes.put(TURNS_TO_NEXT_COIN, 0);
        attributes.put(HEALTH, getNumber(HEALTH));
        attributes.put(USERNAME, userName);
        SessionStateManager stateManager = getSessionStateManager(attributes);
        DialogItem dialogItem = stateManager.nextResponse();
        String expected = getPhrase("royalRansom" + capitalizeFirstLetter(INTRO) + 0);
        expected = expected.replace(USERNAME_PLACEHOLDER, userName);
        assertEquals(expected, dialogItem.getResponseText());
    }

    private String capitalizeFirstLetter(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    private SessionStateManager getSessionStateManager(Map<String, Object> sessionAttributes) {
        Session session = Session.builder()
                .withAttributes(sessionAttributes)
                .build();
        RequestEnvelope requestEnvelope = RequestEnvelope.builder().withSession(session).build();

        AttributesManager attributesManager = AttributesManager.builder()
                .withPersistenceAdapter(adapter)
                .withRequestEnvelope(requestEnvelope)
                .build();
        Map<String, Slot> slots = new HashMap<>();
        Slot slot = Slot.builder().withName(SlotName.ACTION.text).build();
        slots.put(SlotName.ACTION.text, slot);

        return new SessionStateManager(slots, attributesManager);
    }
}