package com.muffinsoft.alexa.skills.adventureisland.game;

import com.amazon.ask.model.Slot;
import com.amazon.ask.model.slu.entityresolution.Resolution;
import com.amazon.ask.model.slu.entityresolution.Resolutions;
import com.amazon.ask.model.slu.entityresolution.Value;
import com.amazon.ask.model.slu.entityresolution.ValueWrapper;

import java.util.List;

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
}
