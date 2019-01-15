package com.muffinsoft.alexa.skills.adventureisland.model;

import com.muffinsoft.alexa.skills.adventureisland.content.Constants;

public class DialogItem {

    private String responseText;
    private boolean end;
    private String slotName;
    private String reprompt;
    private String cardText;
    private String backgroundImage;
    private String backgroundImageExt = Constants.DEFAULT_IMAGE_EXTENSION;
    private String lastPhrase;

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
        private String bBackgroundImage;
        private String bBackgroundImageExt = Constants.DEFAULT_IMAGE_EXTENSION;
        private String bLastPhrase;

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

        public Builder backgroundImage(String url) {
            this.bBackgroundImage = url;
            return this;
        }

        public Builder backgroundImageExt(String extension) {
            this.bBackgroundImageExt = extension;
            return this;
        }

        public Builder lastPhrase(String text) {
            this.bLastPhrase = text;
            return this;
        }

        public DialogItem build() {
            DialogItem dialogItem = new DialogItem();
            dialogItem.cardText = bCardText;
            dialogItem.reprompt = bRePrompt;
            dialogItem.slotName = bSlotName;
            dialogItem.end = bEnd;
            dialogItem.responseText = bResponseText;
            dialogItem.backgroundImage = bBackgroundImage;
            dialogItem.backgroundImageExt = bBackgroundImageExt;
            dialogItem.lastPhrase = bLastPhrase;
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
        return cardText != null ? cardText : "";
    }

    public void setCardText(String cardText) {
        this.cardText = cardText;
    }

    public String getBackgroundImage() {
        return backgroundImage;
    }

    public void setBackgroundImage(String backgroundImage) {
        this.backgroundImage = backgroundImage;
    }

    public String getBackgroundImageExt() {
        return backgroundImageExt;
    }

    public void setBackgroundImageExt(String backgroundImageExt) {
        this.backgroundImageExt = backgroundImageExt;
    }

    public String getLastPhrase() {
        return lastPhrase;
    }

    public void setLastPhrase(String lastPhrase) {
        this.lastPhrase = lastPhrase;
    }
}
