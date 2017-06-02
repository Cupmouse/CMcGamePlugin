package net.cupmouse.minecraft.game.spleef;

import com.google.common.reflect.TypeToken;
import net.cupmouse.minecraft.game.manager.*;
import net.cupmouse.minecraft.worlds.WorldTagArea;
import net.cupmouse.minecraft.worlds.WorldTagRocation;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;

import java.util.*;

import static net.cupmouse.minecraft.game.manager.IntRange.range;

@ConfigSerializable
public class SpleefStage {

    public final String stageId;

    @OptionId({"builders"})
    @Setting("builders")
    private List<String> builders;

    @AreaId({"groundArea", "g"})
    @Setting("ground_area")
    private WorldTagArea groundArea;
    @AreaId({"fightingArea", "f"})
    @Setting("ground_area")
    private WorldTagArea fightingArea;

    @PositionId({"spawn"})
    @Setting("spawns")
    private List<WorldTagRocation> spawnRocations;

    @IntOptionId({"defaultGameTime", "dgt"})
    @Setting("default_game_time")
    private int defaultGameTime;
    @IntOptionId({"minimumPlayerCount", "mpc"})
    @Setting("minimum_player_count")
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
        return Collections.unmodifiableList(spawnRocations);
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

    public WorldTagRocation getSpawnRocation(int index) {
        return spawnRocations.get(index);
    }

    public void setSpawnRocation(int index, WorldTagRocation rocation) {
        spawnRocations.set(index, rocation);
    }

    public void setDefaultGameTime(int defaultGameTime) {
        range(2, null).checkInRange(defaultGameTime);

        this.defaultGameTime = defaultGameTime;
    }

    public void setMinimumPlayerCount(int minimumPlayerCount) {
        range(0, null).checkInRange(minimumPlayerCount);

        this.minimumPlayerCount = minimumPlayerCount;
    }
}
