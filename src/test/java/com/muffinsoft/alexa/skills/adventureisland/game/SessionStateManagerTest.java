package com.muffinsoft.alexa.skills.adventureisland.game;

import com.amazon.ask.attributes.AttributesManager;
import com.amazon.ask.attributes.persistence.PersistenceAdapter;
import com.amazon.ask.model.RequestEnvelope;
import com.amazon.ask.model.Session;
import com.amazon.ask.model.Slot;
import com.muffinsoft.alexa.skills.adventureisland.content.Constants;
import com.muffinsoft.alexa.skills.adventureisland.content.ObstacleManager;
import com.muffinsoft.alexa.skills.adventureisland.content.PhraseManager;
import com.muffinsoft.alexa.skills.adventureisland.content.ReplyManager;
import com.muffinsoft.alexa.skills.adventureisland.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static com.muffinsoft.alexa.skills.adventureisland.content.AttributeKeys.*;
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
        expected = TagProcessor.insertTags(expected);
        assertTrue(dialogItem.getResponseText().startsWith(expected));
    }

    @Test
    void nextResponseTransitionToFirstAction() {
        String userName = "Test user";
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(MISSION, "royalRansom");
        attributes.put(LOCATION, "ancientTemple");
        attributes.put(SCENE, "templeHalls");
        attributes.put(USERNAME, userName);
        SessionStateManager stateManager = getSessionStateManager(attributes);
        DialogItem dialogItem = stateManager.nextResponse();
        System.out.println(dialogItem.getResponseText());
    }

    @Test
    void nextResponseTransitionToNextAction() {
        String userName = "Test user";
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(TIER_INDEX, 0);
        attributes.put(MISSION, "royalRansom");
        attributes.put(LOCATION, "ancientTemple");
        attributes.put(SCENE, "templeHalls");
        attributes.put(USERNAME, userName);
        attributes.put(OBSTACLE, "coins");
        attributes.put(STATE, State.ACTION);
        attributes.put(COINS, 4);
        SessionStateManager stateManager = getSessionStateManager(attributes, "mine");
        DialogItem dialogItem = stateManager.nextResponse();
        System.out.println(dialogItem.getResponseText());
    }

    @Test
    void nextResponseMissionsPrompt() {
        String userName = "Test user";
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(MISSION, ROOT);
        attributes.put(LOCATION, ROOT);
        attributes.put(SCENE, ROOT);
        attributes.put(USERNAME, userName);
        attributes.put(STATE, State.INTRO);
        attributes.put(STATE_INDEX, 2);
        SessionStateManager stateManager = getSessionStateManager(attributes, "hey");
        DialogItem dialogItem = stateManager.nextResponse();
        System.out.println(dialogItem.getResponseText());
    }

    @Test
    void nextResponseTransitionToMission() {
        String userName = "Test user";
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(MISSION, ROOT);
        attributes.put(LOCATION, ROOT);
        attributes.put(SCENE, ROOT);
        attributes.put(USERNAME, userName);
        attributes.put(STATE, State.INTRO);
        attributes.put(STATE_INDEX, 3);
        SessionStateManager stateManager = getSessionStateManager(attributes, "ransom");
        DialogItem dialogItem = stateManager.nextResponse();
        System.out.println(dialogItem.getResponseText());
    }

    @Test
    void nextResponseMissionIntro() {
        String userName = "Test user";
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(MISSION, "royalRansom");
        attributes.put(LOCATION, "royalRansom");
        attributes.put(SCENE, "royalRansom");
        attributes.put(USERNAME, userName);
        attributes.put(STATE, State.INTRO);
        attributes.put(STATE_INDEX, 0);
        SessionStateManager stateManager = getSessionStateManager(attributes, "ransom");
        DialogItem dialogItem = stateManager.nextResponse();
        System.out.println(dialogItem.getResponseText());
    }

    @Test
    void nextResponseQuitLocation() {
        String userName = "Test user";
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(MISSION, "royalRansom");
        attributes.put(LOCATION, "ancientTemple");
        attributes.put(SCENE, "ancientTemple");
        attributes.put(SCENE_INDEX, 4);
        attributes.put(USERNAME, userName);
        attributes.put(STATE, State.OUTRO);
        attributes.put(STATE_INDEX, 1);
        SessionStateManager stateManager = getSessionStateManager(attributes, "mine");
        DialogItem dialogItem = stateManager.nextResponse();
        System.out.println(dialogItem.getResponseText());
    }


    @Test
    void nextResponsePreObstacle() {
        String userName = "Test user";
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(MISSION, "royalRansom");
        attributes.put(LOCATION, "ancientTemple");
        attributes.put(SCENE, "templeHalls");
        attributes.put(STATE, State.ACTION);
        attributes.put(USERNAME, userName);
        List<String> visitedLocations = new ArrayList<>();
        visitedLocations.add("ancientTemple");
        attributes.put(VISITED_LOCATIONS, visitedLocations);
        SessionStateManager stateManager = getSessionStateManager(attributes, "ready");
        DialogItem dialogItem = stateManager.nextResponse();

        StateItem stateItem = stateFromAttributes(attributes);
        stateItem.setIndex(0);
        String obstacle = game.nextObstacle(stateItem);
        String preObstacle = ObstacleManager.getPreObstacle(stateItem, obstacle);

        assertTrue(dialogItem.getResponseText().contains(preObstacle));
    }

    @Test
    void sceneFail() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(MISSION, "royalRansom");
        attributes.put(LOCATION, "ancientTemple");
        attributes.put(SCENE, "templeHalls");
        attributes.put(STATE, State.ACTION);
        attributes.put(STATE_INDEX, 1);
        attributes.put(HEALTH, 1);
        attributes.put(OBSTACLE, "snakes");
        SessionStateManager stateManager = getSessionStateManager(attributes, "whatever");
        DialogItem dialogItem = stateManager.nextResponse();

        String expected = getPhrase(SCENE_FAIL);
        expected = TagProcessor.insertTags(expected);
        assertEquals(expected, dialogItem.getResponseText());

    }

    @Test
    void afterFailureRestart() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(MISSION, "royalRansom");
        attributes.put(LOCATION, "ancientTemple");
        attributes.put(SCENE, "templeHalls");
        attributes.put(STATE, State.FAILED);

        String reply = ReplyManager.getReply(State.FAILED.getKey().toLowerCase() + 1);

        SessionStateManager stateManager = getSessionStateManager(attributes, reply);
        DialogItem dialogItem = stateManager.nextResponse();

        StateItem state = stateFromAttributes(attributes);
        state.setIndex(0);
        String obstacle = Constants.game.nextObstacle(state);

        assertTrue(dialogItem.getResponseText().toLowerCase().contains(obstacle));

    }

    @Test
    void afterFailureNewMission() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(MISSION, "royalRansom");
        attributes.put(LOCATION, "ancientTemple");
        attributes.put(SCENE, "templeHalls");
        attributes.put(STATE, State.FAILED);

        String reply = ReplyManager.getReply(State.FAILED.getKey().toLowerCase() + 2);

        SessionStateManager stateManager = getSessionStateManager(attributes, reply);
        DialogItem dialogItem = stateManager.nextResponse();

        String expected = getPhrase(SELECT_MISSION);
        expected = TagProcessor.insertTags(expected);

        assertTrue(dialogItem.getResponseText().startsWith(expected));

    }

    private StateItem stateFromAttributes(Map<String, Object> attributes) {
        StateItem result = new StateItem(getAttributesManager(attributes));
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
        AttributesManager attributesManager = getAttributesManager(sessionAttributes);

        return new SessionStateManager(slots, attributesManager);
    }

    private AttributesManager getAttributesManager(Map<String, Object> sessionAttributes) {
        Session session = Session.builder()
                .withAttributes(sessionAttributes)
                .build();
        RequestEnvelope requestEnvelope = RequestEnvelope.builder().withSession(session).build();

        AttributesManager attributesManager = AttributesManager.builder()
                .withPersistenceAdapter(adapter)
                .withRequestEnvelope(requestEnvelope)
                .build();
        attributesManager.setPersistentAttributes(new HashMap<>());
        return attributesManager;
    }
}