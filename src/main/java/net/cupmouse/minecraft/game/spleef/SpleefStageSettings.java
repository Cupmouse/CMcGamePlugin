package net.cupmouse.minecraft.game.spleef;

import ninja.leaping.configurate.ConfigurationNode;

import java.util.*;
import java.util.stream.Collectors;

public class SpleefStageSettings {

    public final List<SpleefSpawn> spawnLocations;
    public final int defaultGameTime;
    public final int minimumPlayerCount;

    private SpleefStageSettings(List<SpleefSpawn> spawnLocations, int defaultGameTime) {
        this.spawnLocations = spawnLocations;
        this.defaultGameTime = defaultGameTime;
    }

    public static SpleefStageSettings loadFromConfig(ConfigurationNode node) {
        ConfigurationNode nodeSpawns = node.getNode("spawns");

        // 変更不可のリスト
        List<SpleefSpawn> spawnLocations = Collections.unmodifiableList(
                nodeSpawns.getChildrenList().stream()
                .map(SpleefSpawn::loadFromConfig)
                .collect(Collectors.toCollection(ArrayList::new))
        );

        int defaultGameTime = node.getNode("default_game_time").getInt();

        return new SpleefStageSettings(spawnLocations, defaultGameTime);
    }

    public static SpleefStageSettings createWorking() {
        // 変更可能なリスト
        SpleefStageSettings settings = new SpleefStageSettings(
                new ArrayList<>(),
                0);

        return settings;
    }
}
