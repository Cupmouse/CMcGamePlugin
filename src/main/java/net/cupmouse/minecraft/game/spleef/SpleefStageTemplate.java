package net.cupmouse.minecraft.game.spleef;

import com.flowpowered.math.vector.Vector3i;
import net.cupmouse.minecraft.worlds.WorldTagArea;
import net.cupmouse.minecraft.worlds.WorldTagLocation;
import net.cupmouse.minecraft.worlds.WorldTagRocation;

import java.util.List;

public class SpleefStageTemplate {

    private SpleefStageTemplateInfo info;
    private SpleefStageOptionsMutable defaultOptions;

    private WorldTagLocation relativeBaseLocation;
    private WorldTagArea relativeGroundArea;
    private WorldTagArea relativeFightingArea;

    private List<WorldTagRocation> relativeSpawnRocations;

    private SpleefStageTemplate() {
    }

    public SpleefStageOptionsMutable getDefaultOptions() {
        return defaultOptions;
    }

    public WorldTagArea getRelativeGroundArea() {
        return relativeGroundArea;
    }

    public WorldTagArea getRelativeFightingArea() {
        return relativeFightingArea;
    }

    /**
     * なんと外部から変更可能
     * @return
     */
    public List<WorldTagRocation> getRelativeSpawnRocations() {
        return relativeSpawnRocations;
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

    /*
    ロードとセーブ
     */
    public static SpleefStageTemplate createNew() {
        SpleefStageTemplate spleefStageTemplate = new SpleefStageTemplate();
        spleefStageTemplate.defaultOptions = new SpleefStageOptionsMutable();
        spleefStageTemplate.info = new SpleefStageTemplateInfo();

        return spleefStageTemplate;
    }

    public WorldTagLocation getRelativeBaseLocation() {
        return relativeBaseLocation;
    }
}
