package com.muffinsoft.alexa.skills.adventureisland.game;

import com.amazon.ask.attributes.AttributesManager;
import com.amazon.ask.attributes.persistence.PersistenceAdapter;
import com.amazon.ask.attributes.persistence.impl.DynamoDbPersistenceAdapter;
import com.amazon.ask.model.RequestEnvelope;
import com.amazon.ask.model.Session;
import com.amazon.ask.model.Slot;
import com.muffinsoft.alexa.skills.adventureisland.model.SlotName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SessionStateManagerTest {

    @Mock
    private PersistenceAdapter adapter;


    @Test
    void nextResponse() {
        Session session = Session.builder().build();
        RequestEnvelope requestEnvelope = RequestEnvelope.builder().withSession(session).build();

        AttributesManager attributesManager = AttributesManager.builder()
                .withPersistenceAdapter(adapter)
                .withRequestEnvelope(requestEnvelope)
                .build();
        Map<String, Slot> slots = new HashMap<>();
        Slot slot = Slot.builder().withName(SlotName.ACTION.text).build();
        slots.put(SlotName.ACTION.text, slot);

        SessionStateManager sessionStateManager = new SessionStateManager(slots, attributesManager);
    }
}