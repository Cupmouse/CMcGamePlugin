package net.cupmouse.minecraft.game.spleef;

import net.cupmouse.minecraft.worlds.WorldTagArea;
import net.cupmouse.minecraft.worlds.WorldTagRocation;

import java.util.Collections;
import java.util.List;

public class SpleefStageTemplate {

    private SpleefStageTemplateInfo info;
    private SpleefStageOptionsMutable defaultOptions;

    private WorldTagArea relativeGroundArea;
    private WorldTagArea relativeFightingArea;

    private List<WorldTagRocation> relativeSpawnRocations;

    private SpleefStageTemplate() {
    }

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

    /*
    ロードとセーブ
     */
    public static SpleefStageTemplate createNew() {
        SpleefStageTemplate spleefStageTemplate = new SpleefStageTemplate();
        spleefStageTemplate.defaultOptions = new SpleefStageOptionsMutable();
        spleefStageTemplate.info = new SpleefStageTemplateInfo();

        return spleefStageTemplate;
    }

}
