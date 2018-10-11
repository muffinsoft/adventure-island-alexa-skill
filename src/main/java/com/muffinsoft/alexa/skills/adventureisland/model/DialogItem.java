package com.muffinsoft.alexa.skills.adventureisland.model;

public class DialogItem {

    private String responseText;
    private boolean end;
    private String slotName;
    private boolean repromptRequired;

    public DialogItem() {}

    public DialogItem(String response, boolean shouldEnd) {
        this(response, shouldEnd, null, false);
    }

    public DialogItem(String response, boolean shouldEnd, String slotName) {
        this(response, shouldEnd, slotName, false);
    }

    public DialogItem(String response, boolean shouldEnd, String slotName, boolean repromptRequired) {
        this.responseText = response;
        this.end = shouldEnd;
        this.slotName = slotName;
        this.repromptRequired = repromptRequired;
    }

    public String getResponseText() {
        return responseText;
    }

    public void setResponseText(String responseText) {
        this.responseText = responseText;
    }

    public boolean isEnd() {
        return end;
    }

    public void setEnd(boolean end) {
        this.end = end;
    }

    public String getSlotName() {
        return slotName;
    }

    public void setSlotName(String slotName) {
        this.slotName = slotName;
    }

    public boolean isRepromptRequired() {
        return repromptRequired;
    }

    public void setRepromptRequired(boolean repromptRequired) {
        this.repromptRequired = repromptRequired;
    }
}
