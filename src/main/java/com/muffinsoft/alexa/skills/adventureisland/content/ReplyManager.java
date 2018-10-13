package com.muffinsoft.alexa.skills.adventureisland.content;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.HashMap;
import java.util.Map;

import static com.muffinsoft.alexa.skills.adventureisland.content.Constants.contentLoader;

public class ReplyManager {
    private static final String PATH = "phrases/promptReplies.json";
    private static Map<String, String> replies = new HashMap<>();
    static {
        replies = contentLoader.loadContent(replies, PATH, new TypeReference<HashMap<String, String>>(){});
    }

    public static String getReply(String key) {
        return replies.get(key);
    }
}
