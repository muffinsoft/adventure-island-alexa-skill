package com.muffinsoft.alexa.skills.adventureisland.game;

import com.muffinsoft.alexa.skills.adventureisland.content.AudioManager;
import com.muffinsoft.alexa.skills.adventureisland.model.State;
import com.muffinsoft.alexa.skills.adventureisland.model.StateItem;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class IntroSoundManager {

    private static final Map<String, String> missionIntros = new HashMap<>();
    static {
        missionIntros.put("royalRansom", "kingdomChase");
        missionIntros.put("pirateBusiness", "pirateBay");
        missionIntros.put("tikiTrouble", "ancientTemple");
    }

    public static String getIntroSound(StateItem state) {
        if (state.getState() == State.INTRO && state.getIndex() == 0) {
            if (Objects.equals(state.getMission(), state.getLocation())) {
                return AudioManager.getLocationIntro(missionIntros.get(state.getMission()));
            } else {
                if (Objects.equals(state.getLocation(), state.getScene())) {
                    return AudioManager.getLocationIntro(state.getLocation());
                } else {
                    return AudioManager.getSceneTransition(state.getLocation());
                }
            }
        }
        return null;
    }
}
