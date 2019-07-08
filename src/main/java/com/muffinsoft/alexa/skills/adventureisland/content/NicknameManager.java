package com.muffinsoft.alexa.skills.adventureisland.content;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NicknameManager {
    private static final String PATH = "phrases/nicknames.json";
    private static Map<String, List<String>> nicknames = new HashMap<>();
    private static final String sep = ", ";

    static {
        nicknames = Constants.contentLoader.loadContent(nicknames, PATH, new TypeReference<Map<String, List<String>>>() {
        });
    }

    public static String getNickname(String mission, int tier) {
        return nicknames.get(mission).get(tier);
    }

    public static String getNicknamesGreeting(Map<String, List<String>> earnedNicknames) {
        String result = PhraseManager.getPhrase(Constants.WELCOME_BACK_ROYAL + Constants.NICKNAMES);

        StringBuilder nicknames = new StringBuilder();
        for (String mission : earnedNicknames.keySet()) {
            List<String> missionNicknames = earnedNicknames.get(mission);
            for (String nickname : missionNicknames) {
                nicknames.append(nickname);
                nicknames.append(sep);
            }
        }
        String nicks = nicknames.substring(0, nicknames.lastIndexOf(sep));
        int k = nicks.lastIndexOf(sep);
        if (k > 0) {
            nicks = nicks.substring(0, k + sep.length()) + "and " + nicks.substring(k + sep.length());
        }
        result = result.replace(Constants.NICKNAME_PLACEHOLDER, nicks);
        return result;
    }
}
