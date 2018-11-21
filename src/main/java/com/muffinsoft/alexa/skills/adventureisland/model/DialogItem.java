package com.muffinsoft.alexa.skills.adventureisland.model;

public class DialogItem {

    private String responseText;
    private boolean end;
    private String slotName;
    private String reprompt;
    private String cardText;

    public DialogItem() {}

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String bResponseText;
        private boolean bEnd;
        private String bSlotName;
        private String bRePrompt;
        private String bCardText;

        private Builder() {}

        public Builder responseText(String text) {
            this.bResponseText = text;
            return this;
        }

        public Builder end(boolean end) {
            this.bEnd = end;
            return this;
        }

        public Builder slotName(String name) {
            this.bSlotName = name;
            return this;
        }

        public Builder reprompt(String text) {
            this.bRePrompt = text;
            return this;
        }

        public Builder cardText(String text) {
            this.bCardText = text;
            return this;
        }

        public DialogItem build() {
            DialogItem dialogItem = new DialogItem();
            dialogItem.cardText = bCardText;
            dialogItem.reprompt = bRePrompt;
            dialogItem.slotName = bSlotName;
            dialogItem.end = bEnd;
            dialogItem.responseText = bResponseText;
            return dialogItem;
        }
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

    public String getReprompt() {
        return reprompt;
    }

    public void setReprompt(String reprompt) {
        this.reprompt = reprompt;
    }

    public String getCardText() {
        return cardText;
    }

    public void setCardText(String cardText) {
        this.cardText = cardText;
    }
}
