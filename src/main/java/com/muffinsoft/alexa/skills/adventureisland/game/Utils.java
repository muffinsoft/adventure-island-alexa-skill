package com.muffinsoft.alexa.skills.adventureisland.game;

import com.amazon.ask.model.Slot;
import com.amazon.ask.model.slu.entityresolution.Resolution;
import com.amazon.ask.model.slu.entityresolution.Resolutions;
import com.amazon.ask.model.slu.entityresolution.Value;
import com.amazon.ask.model.slu.entityresolution.ValueWrapper;
import com.muffinsoft.alexa.skills.adventureisland.content.Constants;
import com.muffinsoft.alexa.skills.adventureisland.model.State;
import com.muffinsoft.alexa.skills.adventureisland.model.StateItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.muffinsoft.alexa.skills.adventureisland.content.Constants.ROOT;
import static com.muffinsoft.alexa.skills.adventureisland.content.Constants.SILENT_SCENE;

public class Utils {

    private static final Logger logger = LoggerFactory.getLogger(Utils.class);

    private Utils() {
    }

    public static String wrap(String phrase) {
        return "<p>" + phrase + "</p>";
    }

    public static String combineWithBreak(String responseText, String newText) {
        if (responseText != null) {
            return responseText + " <break time=\"3s\"/> " + newText;
        }
        return newText;
    }

    static String extractSlotResolution(Slot slot, String defaultValue) {
        Resolutions resolutions = slot.getResolutions();
        if (resolutions != null) {
            List<Resolution> resolutionList = resolutions.getResolutionsPerAuthority();
            if (resolutionList != null && !resolutionList.isEmpty()) {
                Resolution resolution = resolutionList.get(0);
                if (resolution != null) {
                    List<ValueWrapper> valueWrappers = resolution.getValues();
                    if (valueWrappers != null && !valueWrappers.isEmpty()) {
                        ValueWrapper valueWrapper = valueWrappers.get(0);
                        if (valueWrapper != null) {
                            Value value = valueWrapper.getValue();
                            if (value != null) {
                                return value.getId();
                            }
                        }
                    }
                }
            }
        }
        return defaultValue;
    }

    static String getLocationStoreKey(StateItem stateItem) {
        return String.format("%d-%d-%d::", stateItem.getTierIndex(), stateItem.getMissionIndex(), stateItem.getLocationIndex());
    }

    static String capitalizeFirstLetter(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    static String getNameKey(StateItem stateItem, State state) {
        String prefix = "";
        if (Objects.equals(stateItem.getScene(), SILENT_SCENE)) {
            prefix = stateItem.getLocation();
        } else if (stateItem.getState() == State.OUTRO && Objects.equals(stateItem.getScene(), stateItem.getLocation())) {
            prefix = stateItem.getMission();
        }

        String introOutroId = stateItem.getIntroOutroId(state);

        if (!Objects.equals(stateItem.getMission(), ROOT) && stateItem.getState() == State.INTRO) {
            // for mission intro / outro, use tier-specific intro / outro
            if (Objects.equals(stateItem.getMission(), stateItem.getLocation())) {
                int tierIndex = stateItem.getTierIndex();
                introOutroId = tierIndex == 0 ? "" : "" + tierIndex;
            } else if (Objects.equals(stateItem.getLocation(), stateItem.getScene())) {
                int locationIndex = getNextLocationIndex(stateItem) % Constants.INTRO_VARIANTS;
                introOutroId = locationIndex == 0 ? "" : "" + locationIndex;
            }
        }

        String scene = stateItem.getScene();
        scene = "".equals(prefix) ? scene : capitalizeFirstLetter(scene);

        return prefix + scene + introOutroId + state.getKey() + stateItem.getIndex();
    }

    private static int getNextLocationIndex(StateItem stateItem) {
        String locationStoreKey = getLocationStoreKey(stateItem);
        logger.debug("Getting location intro for {}", locationStoreKey);
        int result = 0;
        List<String> locationIntros = stateItem.getLocationIntros().getOrDefault(stateItem.getLocation(), new ArrayList<>());
        if (!locationIntros.isEmpty()) {
            logger.debug("Location intros size is {}", locationIntros.size());
            for (String storedLocationInfo : locationIntros) {
                logger.debug("Stored location info: {}", storedLocationInfo);
                if (!storedLocationInfo.isEmpty() && storedLocationInfo.startsWith(locationStoreKey)) {
                    String lastIndex = storedLocationInfo.substring(storedLocationInfo.length() - 1);
                    return Integer.parseInt(lastIndex);
                }
            }
            String info = locationIntros.get(locationIntros.size() - 1);
            String lastIndex = info.substring(info.length() - 1);
            result = Integer.parseInt(lastIndex) + 1;
        }
        locationIntros.add(locationStoreKey + result);
        stateItem.getLocationIntros().put(stateItem.getLocation(), locationIntros);
        return result;
    }
}
