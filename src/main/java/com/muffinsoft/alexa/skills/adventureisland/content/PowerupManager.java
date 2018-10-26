package com.muffinsoft.alexa.skills.adventureisland.content;

import com.fasterxml.jackson.core.type.TypeReference;
import com.muffinsoft.alexa.skills.adventureisland.model.Powerup;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

import static com.muffinsoft.alexa.skills.adventureisland.content.Constants.ANYTHING;

public class PowerupManager {

    private static final String PATH = "phrases/powerups.json";
    private static List<Powerup> powerups = new ArrayList<>();
    private static ThreadLocalRandom random = ThreadLocalRandom.current();

    static {
        powerups = Constants.contentLoader.loadContent(powerups, PATH, new TypeReference<ArrayList<Powerup>>() {});
    }

    public static Powerup getPowerup(String previous) {
        Powerup powerup;
        do {
            int next = random.nextInt(powerups.size());
            powerup = powerups.get(next);
        } while (Objects.equals(powerup.getName(), previous));
        return powerup;
    }

    public static Powerup getPowerupByName(String name) {
        for (Powerup powerup : powerups) {
            if (Objects.equals(powerup.getName(), name)) {
                return powerup;
            }
        }
        throw new NoSuchElementException("No such powerup: " + name);
    }

    public static Powerup findRelevant(List<String> names, String action, String obstacle) {
        for (Powerup powerup : powerups) {
            String powerupAction = powerup.getAction().toLowerCase();
            if (names.contains(powerup.getName()) && powerupAction.contains(action) &&
                    (powerupAction.contains(obstacle.toLowerCase()) || powerupAction.contains(ANYTHING))) {
                return powerup;
            }
        }
        return null;
    }
}
