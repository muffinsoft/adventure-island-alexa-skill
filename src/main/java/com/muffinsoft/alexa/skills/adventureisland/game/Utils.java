package com.muffinsoft.alexa.skills.adventureisland.game;

import com.amazon.ask.model.Slot;
import com.amazon.ask.model.slu.entityresolution.Resolution;
import com.amazon.ask.model.slu.entityresolution.Resolutions;
import com.amazon.ask.model.slu.entityresolution.Value;
import com.amazon.ask.model.slu.entityresolution.ValueWrapper;
import com.muffinsoft.alexa.skills.adventureisland.model.State;
import com.muffinsoft.alexa.skills.adventureisland.model.StateItem;

import java.util.List;
import java.util.Objects;

import static com.muffinsoft.alexa.skills.adventureisland.content.Constants.SILENT_SCENE;

public class Utils {

    private Utils() {}

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

        String scene = stateItem.getScene();
        scene = "".equals(prefix) ? scene : capitalizeFirstLetter(scene);

        return prefix + scene + stateItem.getIntroOutroId(state) + state.getKey() + stateItem.getIndex();
    }
}
