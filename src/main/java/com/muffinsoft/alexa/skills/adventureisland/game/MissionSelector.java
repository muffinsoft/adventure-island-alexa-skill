package com.muffinsoft.alexa.skills.adventureisland.game;

import com.muffinsoft.alexa.skills.adventureisland.content.Constants;
import com.muffinsoft.alexa.skills.adventureisland.content.ImageManager;
import com.muffinsoft.alexa.skills.adventureisland.content.NumbersManager;
import com.muffinsoft.alexa.skills.adventureisland.model.DialogItem;
import com.muffinsoft.alexa.skills.adventureisland.model.Mission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;

import static com.muffinsoft.alexa.skills.adventureisland.content.Constants.*;
import static com.muffinsoft.alexa.skills.adventureisland.content.PhraseManager.getPhrase;
import static com.muffinsoft.alexa.skills.adventureisland.game.Utils.capitalizeFirstLetter;
import static com.muffinsoft.alexa.skills.adventureisland.game.Utils.combine;

public class MissionSelector {

    private static final Logger logger = LoggerFactory.getLogger(MissionSelector.class);

    public static DialogItem promptForMission(String slotName, List<List<BigDecimal>> completedMissions, boolean purchasable) {

        String responseText = "";

        if (completedMissions.size() == NumbersManager.TIERS) {
            List<BigDecimal> lastTier = completedMissions.get(NumbersManager.TIERS - 1);
            if (lastTier.size() == NumbersManager.MISSIONS) {
                responseText = getPhrase(ALL + capitalizeFirstLetter(FINISHED));
            }
        }

        String description = getPhrase(SELECT_MISSION);
        String missionNames = getMissionNames(completedMissions, purchasable);

        description = description.replace(Constants.MISSIONS_AVAILABLE, missionNames);

        responseText = combine(responseText, description);

        String imageUrl = ImageManager.getGeneralImageByKey(ROOT);

        return DialogItem.builder()
                .responseText(responseText)
                .slotName(slotName)
                .reprompt(description)
                .backgroundImage(imageUrl)
                .cardText(missionNames)
                .build();
    }

    public static String getMissionNames(List<List<BigDecimal>> completedMissions, boolean purchasable) {
        StringBuilder missionNames = new StringBuilder();
        List<Mission> missions = game.getMissions();
        for (int i = 0; i < missions.size(); i++) {
            int tier = purchasable ? getTier(i, completedMissions) : 0;
            missionNames.append(missions.get(i).getTierNames().get(tier));
            if (i < missions.size() - 1) {
                missionNames.append(", ");
            }
            if (i == missions.size() - 2) {
                missionNames.append("or ");
            }
        }
        return missionNames.toString();
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
                    if (result >= NumbersManager.TIERS) {
                        result = i;
                    }
                    logger.debug("Mission {} is at tier {}", missionIndex, result);
                    return result;
                }
            }
        }
        return result;
    }
}
