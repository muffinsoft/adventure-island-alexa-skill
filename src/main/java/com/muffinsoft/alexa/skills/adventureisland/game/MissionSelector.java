package com.muffinsoft.alexa.skills.adventureisland.game;

import com.muffinsoft.alexa.skills.adventureisland.model.DialogItem;
import com.muffinsoft.alexa.skills.adventureisland.model.Mission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;

import static com.muffinsoft.alexa.skills.adventureisland.content.Constants.SELECT_MISSION;
import static com.muffinsoft.alexa.skills.adventureisland.content.Constants.TIERS;
import static com.muffinsoft.alexa.skills.adventureisland.content.Constants.game;
import static com.muffinsoft.alexa.skills.adventureisland.content.PhraseManager.getPhrase;
import static com.muffinsoft.alexa.skills.adventureisland.game.Utils.wrap;

public class MissionSelector {

    private static final Logger logger = LoggerFactory.getLogger(MissionSelector.class);

    public static DialogItem promptForMission(String slotName, List<List<BigDecimal>> completedMissions) {
        StringBuilder responseText = new StringBuilder(getPhrase(SELECT_MISSION));
        responseText.append(" ");
        List<Mission> missions = game.getMissions();
        for (int i = 0; i < missions.size(); i++) {
            int tier = getTier(i, completedMissions);
            responseText.append(missions.get(i).getTierNames().get(tier));
            responseText.append(". ");
        }
        return new DialogItem(wrap(responseText.toString()), false, slotName, true);
    }

    public static int getTier(int missionIndex, List<List<BigDecimal>> completedMissions) {
        int result = 0;
        logger.debug("Detecting tier, completed missions size: {}", completedMissions.size());
        for (int i = (completedMissions.size() - 1); i >= 0; i--) {
            logger.debug("{} contains {}?", completedMissions.get(i).toArray(), missionIndex);
            for (BigDecimal savedMissionIndex : completedMissions.get(i)) {
                if (savedMissionIndex.intValue() == missionIndex) {
                    result = i + 1;
                    // do not go over the max tier available
                    if (result >= TIERS) {
                        result = i;
                    }
                    logger.debug("Mission {} is at tier {}", missionIndex, result);
                    break;
                }
            }
        }
        return result;
    }
}
