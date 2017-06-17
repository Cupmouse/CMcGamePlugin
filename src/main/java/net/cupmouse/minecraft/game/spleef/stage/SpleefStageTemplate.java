package net.cupmouse.minecraft.game.spleef.stage;

import net.cupmouse.minecraft.game.manager.AreaId;
import net.cupmouse.minecraft.game.manager.IntOptionId;
import net.cupmouse.minecraft.game.manager.PositionId;
import net.cupmouse.minecraft.worlds.WorldTagArea;
import net.cupmouse.minecraft.worlds.WorldTagRocation;
import ninja.leaping.configurate.objectmapping.Setting;

import java.util.Collections;
import java.util.List;

public abstract class SpleefStageTemplate {

    @Setting("template_info")
    private SpleefStageTemplateInfo info;
    @Setting("default_options")
    private SpleefStageOptions defaultOptions;

    @IntOptionId({"minimumPlayerCount", "mpc"})
    @Setting("minimum_player_count")
    private int minimumPlayerCount;

    @AreaId({"relativeGroundArea", "g"})
    @Setting("relative_ground_area")
    private WorldTagArea relativeGroundArea;
    @AreaId({"relativeFightingArea", "f"})
    @Setting("relative_ground_area")
    private WorldTagArea relativeFightingArea;

    @PositionId({"spawn"})
    @Setting("spawns")
    private List<WorldTagRocation> relativeSpawnRocations;

    public SpleefStageOptions getDefaultOptions() {
        return defaultOptions;
    }

    public WorldTagArea getRelativeGroundArea() {
        return relativeGroundArea;
    }

    public WorldTagArea getRelativeFightingArea() {
        return relativeFightingArea;
    }

    public WorldTagRocation getRelativeSpawnRocation(int index) {
        return relativeSpawnRocations.get(index);
    }

    public List<WorldTagRocation> getRelativeSpawnRocations() {
        return Collections.unmodifiableList(relativeSpawnRocations);
    }

    public int getMinimumPlayerCount() {
        return minimumPlayerCount;
    }

    /*
    ここからSetter
     */

    public void setRelativeGroundArea(WorldTagArea groundArea) {
        this.relativeGroundArea = groundArea;
    }

    public void setRelativeFightingArea(WorldTagArea fightingArea) {
        this.relativeFightingArea = fightingArea;
    }

    public void setRelativeSpawnRocation(int index, WorldTagRocation rocation) {
        relativeSpawnRocations.set(index, rocation);
    }

    public void setMinimumPlayerCount(int minimumPlayerCount) {
        this.minimumPlayerCount = minimumPlayerCount;
    }
}
