package net.cupmouse.minecraft.game.spleef;

import com.google.common.reflect.TypeToken;
import net.cupmouse.minecraft.worlds.WorldTagArea;
import net.cupmouse.minecraft.worlds.WorldTagRocation;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;

import java.util.*;

public class SpleefStage {

    public final String stageId;

    private List<String> builder;
    private WorldTagArea groundArea;
    private WorldTagArea fightingArea;
    private List<WorldTagRocation> spawnRocations;
    private int defaultGameTime;
    private int minimumPlayerCount;

    private SpleefStage(String stageId, WorldTagArea groundArea, WorldTagArea fightingArea,
                        List<WorldTagRocation> spawnRocations, int defaultGameTime, int minimumPlayerCount) {
        this.stageId = stageId;
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
    public static SpleefStage creator(String stageId) {
        // TODO
        // 変更可能なリスト
        SpleefStage spleefStage = new SpleefStage(
                stageId, null, null, new ArrayList<>(),
                30, 2);

        return spleefStage;
    }

    public WorldTagArea getGroundArea() {
        return groundArea;
    }

    public WorldTagArea getFightingArea() {
        return fightingArea;
    }

    public List<WorldTagRocation> getSpawnRocations() {
        return spawnRocations;
    }

    public int getDefaultGameTime() {
        return defaultGameTime;
    }

    public int getMinimumPlayerCount() {
        return minimumPlayerCount;
    }

    public void setGroundArea(WorldTagArea groundArea) {
        this.groundArea = groundArea;
    }

    public void setFightingArea(WorldTagArea fightingArea) {
        this.fightingArea = fightingArea;
    }

    public void setSpawnRocations(List<WorldTagRocation> spawnRocations) {
        this.spawnRocations = spawnRocations;
    }

    public void setDefaultGameTime(int defaultGameTime) {
        this.defaultGameTime = defaultGameTime;
    }

    public void setMinimumPlayerCount(int minimumPlayerCount) {
        this.minimumPlayerCount = minimumPlayerCount;
    }

    static class Serializer implements TypeSerializer<SpleefStage> {

        @Override
        public SpleefStage deserialize(TypeToken<?> type, ConfigurationNode value)
                throws ObjectMappingException {
            String stageId = value.getNode("id").getString();
            WorldTagArea groundArea = value.getNode("ground_area").getValue(TypeToken.of(WorldTagArea.class));
            WorldTagArea fightingArea = value.getNode("fighting_area").getValue(TypeToken.of(WorldTagArea.class));

            // 変更不可能にしておく
            List<WorldTagRocation> spawnRocations =
//                    Collections.unmodifiableList(
                            value.getNode("spawns").getList(TypeToken.of(WorldTagRocation.class))
                    ;
//                    );

            int defaultGameTime = value.getNode("default_game_time").getInt();
            int minimumPlayerCount = value.getNode("minimum_player_count").getInt();

            return new SpleefStage(stageId, groundArea, fightingArea,
                    spawnRocations, defaultGameTime, minimumPlayerCount);
        }

        @Override
        public void serialize(TypeToken<?> type, SpleefStage obj, ConfigurationNode value)
                throws ObjectMappingException {
            value.getNode("ground_area").setValue(TypeToken.of(WorldTagArea.class), obj.groundArea);
            value.getNode("fighting_area").setValue(TypeToken.of(WorldTagArea.class), obj.fightingArea);

            value.getNode("spawns").setValue(new TypeToken<List<WorldTagRocation>>() {}, obj.spawnRocations);

            value.getNode("default_game_time").setValue(obj.defaultGameTime);
            value.getNode("minimum_player_count").setValue(obj.minimumPlayerCount);
        }
    }
}
