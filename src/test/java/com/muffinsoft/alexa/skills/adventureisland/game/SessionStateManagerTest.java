package com.muffinsoft.alexa.skills.adventureisland.game;

import com.amazon.ask.attributes.AttributesManager;
import com.amazon.ask.attributes.persistence.PersistenceAdapter;
import com.amazon.ask.model.RequestEnvelope;
import com.amazon.ask.model.Session;
import com.amazon.ask.model.Slot;
import com.muffinsoft.alexa.skills.adventureisland.content.Constants;
import com.muffinsoft.alexa.skills.adventureisland.content.ObstacleManager;
import com.muffinsoft.alexa.skills.adventureisland.content.PhraseManager;
import com.muffinsoft.alexa.skills.adventureisland.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static com.muffinsoft.alexa.skills.adventureisland.content.Constants.*;
import static com.muffinsoft.alexa.skills.adventureisland.content.NumbersManager.getNumber;
import static com.muffinsoft.alexa.skills.adventureisland.content.PhraseManager.getPhrase;
import static com.muffinsoft.alexa.skills.adventureisland.content.PhraseManager.nameToKey;
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
        String expected = PhraseManager.getPhrase(Constants.SELECT_MISSION);
        for (Mission mission : Constants.game.getMissions()) {
            expected += mission.getName() + ". ";
        }
        assertEquals(expected, dialogItem.getResponseText());
    }

    @Test
    void nextResponseTransitionToFirstMission() {
        String userName = "Test user";
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(MISSION, ROOT);
        attributes.put(LOCATION, ROOT);
        attributes.put(SCENE, ROOT);
        attributes.put(STATE, State.INTRO);
        attributes.put(STATE_INDEX, 2);
        attributes.put(COINS, 0);
        attributes.put(TOTAL_COINS, 0);
        attributes.put(HEALTH, getNumber(HEALTH));
        attributes.put(USERNAME, userName);

        String missionName = Constants.game.getMissions().get(0).getName();

        SessionStateManager stateManager = getSessionStateManager(attributes, missionName);
        DialogItem dialogItem = stateManager.nextResponse();

        String key = nameToKey(missionName);

        String expected = getPhrase(key + State.INTRO.getKey() + 0);
        expected = expected.replace(USERNAME_PLACEHOLDER, userName);
        assertEquals(expected, dialogItem.getResponseText());
    }

    @Test
    void nextResponseTransitionToFirstAction() {
        String userName = "Test user";
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(MISSION, "royalRansom");
        attributes.put(LOCATION, "ancientTemple");
        attributes.put(SCENE, "templeHalls");
        attributes.put(STATE_INDEX, 0);
        attributes.put(COINS, 0);
        attributes.put(TOTAL_COINS, 0);
        attributes.put(HEALTH, getNumber(HEALTH));
        attributes.put(USERNAME, userName);
        attributes.put(OLD_OBSTACLES, new ArrayList<>());
        SessionStateManager stateManager = getSessionStateManager(attributes);
        DialogItem dialogItem = stateManager.nextResponse();
        System.out.println(dialogItem.getResponseText());
    }

    @Test
    void nextResponseAskPassword() {
        String userName = "Test user";
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(MISSION, "royalRansom");
        attributes.put(LOCATION, "ancientTemple");
        attributes.put(SCENE, "ancientTemple");
        attributes.put(STATE, State.INTRO);
        attributes.put(STATE_INDEX, 1);
        attributes.put(COINS, 0);
        attributes.put(TOTAL_COINS, 0);
        attributes.put(HEALTH, getNumber(HEALTH));
        attributes.put(USERNAME, userName);
        attributes.put(VISITED_LOCATIONS, new ArrayList<String>());
        attributes.put(OLD_OBSTACLES, new ArrayList<>());
        SessionStateManager stateManager = getSessionStateManager(attributes);
        DialogItem dialogItem = stateManager.nextResponse();

        String expected = getPhrase("ancientTemple" + State.INTRO.getKey() + 1 + NO);

        assertTrue(dialogItem.getResponseText().startsWith(expected));
    }

    @Test
    void nextResponseExplanation() {
        String userName = "Test user";
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(MISSION, "royalRansom");
        attributes.put(LOCATION, "ancientTemple");
        attributes.put(SCENE, "templeHalls");
        attributes.put(STATE, State.ACTION);
        attributes.put(STATE_INDEX, 0);
        attributes.put(COINS, 0);
        attributes.put(TOTAL_COINS, 0);
        attributes.put(HEALTH, getNumber(HEALTH));
        attributes.put(USERNAME, userName);
        attributes.put(OLD_OBSTACLES, new ArrayList<>());
        List<String> visitedLocations = new ArrayList<>();
        visitedLocations.add("ancientTemple");
        attributes.put(VISITED_LOCATIONS, visitedLocations);
        SessionStateManager stateManager = getSessionStateManager(attributes);
        DialogItem dialogItem = stateManager.nextResponse();

        StateItem stateItem = stateFromAttributes(attributes);
        stateItem.setIndex(0);
        String obstacle = game.nextObstacle(stateItem);
        String preObstacle = ObstacleManager.getPreObstacle(stateItem, obstacle);

        assertTrue(dialogItem.getResponseText().contains(preObstacle));
    }

    private StateItem stateFromAttributes(Map<String, Object> attributes) {
        StateItem result = new StateItem();
        result.setMission(attributes.get(MISSION).toString());
        result.setLocation(attributes.get(LOCATION).toString());
        result.setScene(attributes.get(SCENE).toString());
        result.setState((State) attributes.get(STATE));
        result.setIndex((int) attributes.get(STATE_INDEX));

        return result;
    }

    private String capitalizeFirstLetter(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    private SessionStateManager getSessionStateManager(Map<String, Object> sessionAttributes) {
        return getSessionStateManager(sessionAttributes, null);
    }

    private SessionStateManager getSessionStateManager(Map<String, Object> sessionAttributes, String inSlot) {
        Map<String, Slot> slots = new HashMap<>();
        Slot slot = Slot.builder().withName(SlotName.ACTION.text).withValue(inSlot).build();
        slots.put(SlotName.ACTION.text, slot);
        return getSessionStateManager(slots, sessionAttributes);
    }

    private SessionStateManager getSessionStateManager(Map<String, Slot> slots, Map<String, Object> sessionAttributes) {
        Session session = Session.builder()
                .withAttributes(sessionAttributes)
                .build();
        RequestEnvelope requestEnvelope = RequestEnvelope.builder().withSession(session).build();

        AttributesManager attributesManager = AttributesManager.builder()
                .withPersistenceAdapter(adapter)
                .withRequestEnvelope(requestEnvelope)
                .build();

        return new SessionStateManager(slots, attributesManager);
    }
}