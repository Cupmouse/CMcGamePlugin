package net.cupmouse.minecraft.game.spleef;

import com.google.common.reflect.TypeToken;
import net.cupmouse.minecraft.worlds.WorldTagArea;
import net.cupmouse.minecraft.worlds.WorldTagLocation;
import net.cupmouse.minecraft.worlds.WorldTagRocation;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public final class SpleefStageSettings {

    public final WorldTagArea groundArea;
    public final WorldTagArea fightingArea;
    public final List<WorldTagRocation> spawnRocations;
    public final int defaultGameTime;
    public final int minimumPlayerCount;

    private SpleefStageSettings(WorldTagArea groundArea, WorldTagArea fightingArea,
                                List<WorldTagRocation> spawnRocations, int defaultGameTime, int minimumPlayerCount) {
        this.groundArea = groundArea;
        this.fightingArea = fightingArea;
        this.spawnRocations = spawnRocations;
        this.defaultGameTime = defaultGameTime;
        this.minimumPlayerCount = minimumPlayerCount;
    }

    /**
     * 新しいステージを作るときに使うものです。
     * @return
     */
    public static SpleefStageSettings creator() {
        // TODO
        // 変更可能なリスト
        SpleefStageSettings settings = new SpleefStageSettings(null, null, new ArrayList<>(),
                30, 1);

        return settings;
    }

    static class Serializer implements TypeSerializer<SpleefStageSettings> {

        @Override
        public SpleefStageSettings deserialize(TypeToken<?> type, ConfigurationNode value)
                throws ObjectMappingException {
            WorldTagArea groundArea = value.getNode("ground_area").getValue(TypeToken.of(WorldTagArea.class));
            WorldTagArea fightingArea = value.getNode("fighting_area").getValue(TypeToken.of(WorldTagArea.class));

            // 変更不可能にしておく
            List<WorldTagRocation> spawnRocations =
                    Collections.unmodifiableList(
                            value.getNode("spawns").getList(TypeToken.of(WorldTagRocation.class)));

            int defaultGameTime = value.getNode("default_game_time").getInt();
            int minimumPlayerCount = value.getNode("minimumPlayerCount").getInt();

            return new SpleefStageSettings(groundArea, fightingArea,
                    spawnRocations, defaultGameTime, minimumPlayerCount);
        }

        @Override
        public void serialize(TypeToken<?> type, SpleefStageSettings obj, ConfigurationNode value)
                throws ObjectMappingException {
            value.getNode("ground_area").setValue(TypeToken.of(WorldTagArea.class), obj.groundArea);
            value.getNode("fighting_area").setValue(TypeToken.of(WorldTagArea.class), obj.fightingArea);

            value.getNode("spawns").setValue(new TypeToken<List<WorldTagRocation>>() {}, obj.spawnRocations);

            value.getNode("default_game_time").setValue(obj.defaultGameTime);
            value.getNode("minimumPlayerCount").setValue(obj.minimumPlayerCount);
        }
    }
}
