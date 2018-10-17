package com.muffinsoft.alexa.skills.adventureisland.content;

import com.fasterxml.jackson.core.type.TypeReference;
import com.muffinsoft.alexa.skills.adventureisland.model.Game;
import com.muffinsoft.alexa.skills.adventureisland.model.Mission;

import static com.muffinsoft.alexa.skills.adventureisland.content.Constants.contentLoader;

public class LayoutManager {

    private static final String LAYOUT_MISSION_1 = "setup/mission1-layout.json";
    private static final String LAYOUT_MISSION_2 = "setup/mission2-layout.json";
    private static final String LAYOUT_MISSION_3 = "setup/mission3-layout.json";

    public static Game loadGame() {
        Mission sample = new Mission();
        Mission mission1 = contentLoader.loadContent(sample, LAYOUT_MISSION_1, new TypeReference<Mission>(){});
        Mission mission2 = contentLoader.loadContent(sample, LAYOUT_MISSION_2, new TypeReference<Mission>(){});
        Mission mission3 = contentLoader.loadContent(sample, LAYOUT_MISSION_3, new TypeReference<Mission>(){});
        Game game = new Game();
        game.addMission(mission1);
        game.addMission(mission2);
        game.addMission(mission3);
        return game;
    }
}
