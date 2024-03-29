package com.muffinsoft.alexa.skills.adventureisland.content;

import com.fasterxml.jackson.core.type.TypeReference;
import com.muffinsoft.alexa.skills.adventureisland.model.GameProperties;
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

    public static Powerup useFirstRelevant(GameProperties props, String... actions) {
        List<String> names = props.getPowerups();
        for (Powerup powerup : powerups) {
            String powerupAction = powerup.getAction().toLowerCase();
            if (names.contains(powerup.getName()) &&
                    (powerupAction.contains(props.getCurrentObstacle().toLowerCase()) || powerupAction.contains(ANYTHING))) {
                for (String action : actions) {
                    if (powerupAction.contains(action)) {
                        names.remove(powerup.getName());
                        props.setPowerups(names);
                        return powerup;
                    }
                }
            }
        }
        return null;
    }
}
