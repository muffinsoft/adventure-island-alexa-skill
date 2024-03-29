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

    public static String getNicknamesGreeting(Map<String, List<String>> earnedNicknames) {
        String result = PhraseManager.getPhrase(Constants.WELCOME_BACK_ROYAL + Constants.NICKNAMES);

        StringBuilder nicknames = new StringBuilder();
        for (String mission : earnedNicknames.keySet()) {
            List<String> missionNicknames = earnedNicknames.get(mission);
            for (int i = 0; i < missionNicknames.size(); i++) {
                nicknames.append(missionNicknames.get(i));
                if (i < missionNicknames.size() - 1) {
                    nicknames.append(", ");
                }
                if (i == missionNicknames.size() - 2) {
                    nicknames.append("and ");
                }
            }
        }
        result = result.replace(Constants.NICKNAME_PLACEHOLDER, nicknames);
        return result;
    }
}
