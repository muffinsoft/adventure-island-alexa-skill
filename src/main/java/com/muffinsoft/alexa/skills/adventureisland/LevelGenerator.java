package com.muffinsoft.alexa.skills.adventureisland;

import com.muffinsoft.alexa.skills.adventureisland.content.ObstacleManager;
import com.muffinsoft.alexa.skills.adventureisland.model.ObstacleSetupItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class LevelGenerator {

    private static ThreadLocalRandom random = ThreadLocalRandom.current();
    private static final String LOCATION = "pirateBay";
    private static final String SCENE = "shipwreckBay";
    private static final int MAX = 50;

    public static void main(String[] args) {
        ObstacleSetupItem obstacleSetup = ObstacleManager.getObstacleSetup(LOCATION, SCENE, 2);
        List<Integer> level = obstacleSetup.getObstacleIndices();
        List<String> result = new ArrayList<>();
        int lastTreasure = 0;
        for (int i = 0; i <= MAX; i++) {
            String nextObstacleQuoted;
            String nextObstacle;
            do {
                int next = random.nextInt(level.size() + 1);
                next = next >= level.size() ? -1 : next;
                nextObstacle = ObstacleManager.getObstacleByIndex(LOCATION, next);
                if (ObstacleManager.isTreasure(nextObstacle)) {
                    lastTreasure = 0;
                } else {
                    lastTreasure++;
                }
                int nextTreasure = random.nextInt(3) + 1;
                if (lastTreasure > nextTreasure) {
                    nextObstacle = ObstacleManager.getTreasureName();
                    lastTreasure = 0;
                }
                nextObstacleQuoted = "\"" + nextObstacle + "\"";
            } while (!result.isEmpty() && nextObstacleQuoted.equals(result.get(result.size() - 1)) &&
                    !ObstacleManager.isTreasure(nextObstacle) && random.nextDouble() > 0.3);
            result.add(nextObstacleQuoted);
        }
        System.out.println(Arrays.toString(result.toArray()));
    }
}
