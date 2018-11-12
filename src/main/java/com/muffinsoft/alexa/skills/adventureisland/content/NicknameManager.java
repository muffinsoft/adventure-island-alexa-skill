package com.muffinsoft.alexa.skills.adventureisland.content;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NicknameManager {
    private static final String PATH = "phrases/nicknames.json";
    private static Map<String, List<String>> nicknames = new HashMap<>();

    static {
        nicknames = Constants.contentLoader.loadContent(nicknames, PATH, new TypeReference<Map<String, List<String>>>(){});
    }

    public static String getNickname(String mission, int tier) {
        return nicknames.get(mission).get(tier);
    }
}
