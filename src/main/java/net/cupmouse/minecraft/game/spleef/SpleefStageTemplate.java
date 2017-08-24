package net.cupmouse.minecraft.game.spleef;

import net.cupmouse.minecraft.worlds.WorldTagArea;
import net.cupmouse.minecraft.worlds.WorldTagLocation;
import net.cupmouse.minecraft.worlds.WorldTagRocation;

import java.util.List;

public class SpleefStageTemplate {

    private SpleefStageTemplateInfo info;
    private SpleefStageOptionsMutable defaultOptions;

    // これらはすべてステージテンプレート作成時の絶対位置（ワールドも含め）なので、相対位置ではない
    // そのために相対位置の基準点を決め、それを使って新しいステージを作成する
    private WorldTagLocation relativeBaseLocation;
    private WorldTagArea groundArea;
    private WorldTagArea fightingArea;
    private List<WorldTagRocation> spawnRocations;

    private SpleefStageTemplate() {
    }

    public SpleefStageOptionsMutable getDefaultOptions() {
        return defaultOptions;
    }

    public WorldTagArea getGroundArea() {
        return groundArea;
    }

    public WorldTagArea getFightingArea() {
        return fightingArea;
    }

    /**
     * なんと外部から変更可能
     * @return
     */
    public List<WorldTagRocation> getSpawnRocations() {
        return spawnRocations;
    }

    /*
    ここからSetter
     */

    public void setGroundArea(WorldTagArea groundArea) {
        this.groundArea = groundArea;
    }

    public void setFightingArea(WorldTagArea fightingArea) {
        this.fightingArea = fightingArea;
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
