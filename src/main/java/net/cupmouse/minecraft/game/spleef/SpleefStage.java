package net.cupmouse.minecraft.game.spleef;

import com.flowpowered.math.vector.Vector3i;
import net.cupmouse.minecraft.worlds.WorldTagArea;
import net.cupmouse.minecraft.worlds.WorldTagRocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SpleefStage {

    private final SpleefStageTemplate template;
    private SpleefStageOptions options;

    private final Vector3i relativeBasePoint;
    private WorldTagArea groundArea;
    private WorldTagArea fightingArea;
    private ArrayList<WorldTagRocation> spawnRocations;
    private int minimumPlayerCount;

    SpleefStage(SpleefStageTemplate template, Vector3i relativeBasePoint) {
        this.template = template;
        this.options = template.getDefaultOptions();

        this.relativeBasePoint = relativeBasePoint;

        loadFromTemplate();
    }

    private void loadFromTemplate() {
        this.groundArea = template.getRelativeGroundArea().relativeBasePoint(relativeBasePoint);
        this.fightingArea = template.getRelativeFightingArea().relativeBasePoint(relativeBasePoint);

        this.spawnRocations = new ArrayList<>();

        for (WorldTagRocation rocation : template.getRelativeSpawnRocations()) {
            spawnRocations.add(rocation.relativeBasePoint(relativeBasePoint));
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

    public Vector3i getRelativeBasePoint() {
        return relativeBasePoint;
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
