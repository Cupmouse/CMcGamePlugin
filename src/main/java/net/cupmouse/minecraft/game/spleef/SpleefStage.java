package net.cupmouse.minecraft.game.spleef;

import net.cupmouse.minecraft.game.manager.GameException;
import net.cupmouse.minecraft.worlds.WorldTagArea;
import net.cupmouse.minecraft.worlds.WorldTagLocation;
import net.cupmouse.minecraft.worlds.WorldTagRocation;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.text.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SpleefStage {

    private final SpleefStageTemplate template;
    private SpleefStageOptions options;

    private final WorldTagLocation relativeBaseLocation;
    private ArrayList<WorldTagRocation> spawnRocations;
    private WorldTagRocation waitingSpawnRocation;

    private WorldTagArea groundArea;
    private WorldTagArea fightingArea;
    private WorldTagArea spectetorArea;

    private BlockState groundSample;

    SpleefStage(SpleefStageTemplate template, WorldTagLocation relativeBaseLocation) {
        this.template = template;
        this.relativeBaseLocation = relativeBaseLocation;

        loadFromTemplate();
    }

    private void loadFromTemplate() {
        WorldTagLocation templateBaseLoc = template.getRelativeBaseLocation();

        // テンプレートの絶対値指定からテンプレートの相対位置基準点を用いて、このステージの実際の位置を計算する
        // ワールドタグはテンプレートの方に設定されるので変更する必要がある
        this.groundArea = template.getGroundArea()
                .relativeTo(templateBaseLoc)
                .relativeBase(relativeBaseLocation)
                .worldTag(relativeBaseLocation.worldTag);
        this.fightingArea = template.getFightingArea()
                .relativeTo(templateBaseLoc)
                .relativeBase(relativeBaseLocation)
                .worldTag(relativeBaseLocation.worldTag);
        this.spectetorArea = template.getSpectatorArea()
                .relativeTo(templateBaseLoc)
                .relativeBase(relativeBaseLocation)
                .worldTag(relativeBaseLocation.worldTag);

        this.spawnRocations = new ArrayList<>();

        for (WorldTagRocation rocation : template.getSpawnRocations()) {
            spawnRocations.add(rocation.relativeTo(templateBaseLoc)
                    .relativeBase(relativeBaseLocation)
                    .worldTag(relativeBaseLocation.worldTag));
        }

        this.waitingSpawnRocation = template.getWaitingSpawnRocation()
                .relativeTo(templateBaseLoc)
                .relativeBase(relativeBaseLocation)
                .worldTag(relativeBaseLocation.worldTag);

        this.groundSample = template.getGroundSample();

        // ステージの設定をテンプレートからコピーして設定する
        SpleefStageOptions defaultOptions = template.getDefaultOptions();
        this.options = defaultOptions.copy();
    }

    public SpleefStageTemplate getTemplate() {
        return template;
    }

    public SpleefStageOptions getOptions() {
        return options;
    }

    public WorldTagArea getGroundArea() {
        return groundArea;
    }

    public WorldTagArea getFightingArea() {
        return fightingArea;
    }

    public WorldTagArea getSpectetorArea() {
        return spectetorArea;
    }

    public WorldTagLocation getRelativeBaseLocation() {
        return relativeBaseLocation;
    }

    public List<WorldTagRocation> getSpawnRocations() {
        return Collections.unmodifiableList(spawnRocations);
    }

    public void shuffleSpawnRocations() {
        Collections.shuffle(spawnRocations);
    }

    public WorldTagRocation getWaitingSpawnRocation() {
        return waitingSpawnRocation;
    }

    public BlockState getGroundSample() {
        return groundSample;
    }
}
