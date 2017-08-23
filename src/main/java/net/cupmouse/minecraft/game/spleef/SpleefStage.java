package net.cupmouse.minecraft.game.spleef;

import net.cupmouse.minecraft.worlds.WorldTagArea;
import net.cupmouse.minecraft.worlds.WorldTagLocation;
import net.cupmouse.minecraft.worlds.WorldTagRocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SpleefStage {

    private final SpleefStageTemplate template;
    private SpleefStageOptions options;

    private final WorldTagLocation relativeBaseLocation;
    private WorldTagArea groundArea;
    private WorldTagArea fightingArea;
    private ArrayList<WorldTagRocation> spawnRocations;
    private int minimumPlayerCount;

    SpleefStage(SpleefStageTemplate template, WorldTagLocation relativeBaseLocation) {
        this.template = template;
        this.options = template.getDefaultOptions();

        this.relativeBaseLocation = relativeBaseLocation;

        loadFromTemplate();
    }

    private void loadFromTemplate() {
        template.getRelativeBaseLocation();
        this.groundArea = template.getRelativeGroundArea().relativeBasePoint(relativeBaseLocation);
        this.fightingArea = template.getRelativeFightingArea().relativeBasePoint(relativeBaseLocation);

        this.spawnRocations = new ArrayList<>();

        for (WorldTagRocation rocation : template.getRelativeSpawnRocations()) {
            spawnRocations.add(rocation.relativeBasePoint(relativeBaseLocation));
        }

        SpleefStageOptions defaultOptions = template.getDefaultOptions();
        this.minimumPlayerCount = defaultOptions.getMinimumPlayerCount();
        // TODO
    }

    public SpleefStageTemplate getTemplate() {
        return template;
    }

    public SpleefStageOptions getOptions() {
        return options;
    }

    public WorldTagLocation getRelativeBaseLocation() {
        return relativeBaseLocation;
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

    public int getMinimumPlayerCount() {
        return minimumPlayerCount;
    }
}
