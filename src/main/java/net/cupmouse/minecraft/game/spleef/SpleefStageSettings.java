package net.cupmouse.minecraft.game.spleef;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;

import java.util.*;
import java.util.stream.Collectors;

public final class SpleefStageSettings {

    public final String stageId;
    public final SpleefArea groundArea;
    public final SpleefArea fightingArea;
    public final List<SpleefSpawn> spawnLocations;
    public final int defaultGameTime;
    public final int minimumPlayerCount;

    private SpleefStageSettings(String stageId, SpleefArea groundArea, SpleefArea fightingArea, List<SpleefSpawn> spawnLocations,
                                int defaultGameTime, int minimumPlayerCount) {
        this.stageId = stageId;
        this.groundArea = groundArea;
        this.fightingArea = fightingArea;
        this.spawnLocations = spawnLocations;
        this.defaultGameTime = defaultGameTime;
        this.minimumPlayerCount = minimumPlayerCount;
    }

    /**
     * 新しいステージを作るときに使うものです。
     * @return
     */
    public static SpleefStageSettings creator(String stageId) {
        // TODO
        // 変更可能なリスト
        SpleefStageSettings settings = new SpleefStageSettings(
                stageId, null, null, new ArrayList<>(),
                30, 1);

        return settings;
    }

    static class Serializer implements TypeSerializer<SpleefStageSettings> {

        @Override
        public SpleefStageSettings deserialize(TypeToken<?> type, ConfigurationNode value) throws ObjectMappingException {
            String stageId = value.getNode("stage_id").getString();

            SpleefArea groundArea = value.getNode("ground_area").getValue(TypeToken.of(SpleefArea.class));
            SpleefArea fightingArea = value.getNode("fighting_area").getValue(TypeToken.of(SpleefArea.class));

            ConfigurationNode nodeSpawns = value.getNode("spawns");

            // 変更不可のリスト
            List<SpleefSpawn> spawnLocations = Collections.unmodifiableList(
                    nodeSpawns.getChildrenList().stream()
                            .map(SpleefSpawn::loadFromConfig)
                            .collect(Collectors.toCollection(ArrayList::new))
            );

            int defaultGameTime = value.getNode("default_game_time").getInt();
            int minimumPlayerCount = value.getNode("minimumPlayerCount").getInt();

            return new SpleefStageSettings(stageId, groundArea, fightingArea, spawnLocations, defaultGameTime, minimumPlayerCount);
        }

        @Override
        public void serialize(TypeToken<?> type, SpleefStageSettings obj, ConfigurationNode value) throws ObjectMappingException {

        }
    }
}
