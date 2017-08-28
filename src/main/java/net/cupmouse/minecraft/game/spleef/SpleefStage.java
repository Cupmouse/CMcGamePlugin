package net.cupmouse.minecraft.game.spleef;

import net.cupmouse.minecraft.game.manager.GameException;
import net.cupmouse.minecraft.worlds.WorldTagArea;
import net.cupmouse.minecraft.worlds.WorldTagLocation;
import net.cupmouse.minecraft.worlds.WorldTagRocation;
import org.spongepowered.api.text.Text;

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

    SpleefStage(SpleefStageTemplate template, WorldTagLocation relativeBaseLocation) {
        this.template = template;
        this.options = template.getDefaultOptions();

        this.relativeBaseLocation = relativeBaseLocation;

        loadFromTemplate();
    }

    private void loadFromTemplate() {
        WorldTagLocation templateBaseLoc = template.getRelativeBaseLocation();

        // ワールドタグはテンプレートの方に設定されるので変更する必要がある
        this.groundArea = template.getGroundArea()
                .relativeTo(templateBaseLoc)
                .relativeBase(relativeBaseLocation)
                .worldTag(relativeBaseLocation.worldTag);
        this.fightingArea = template.getFightingArea()
                .relativeTo(templateBaseLoc)
                .relativeBase(relativeBaseLocation)
                .worldTag(relativeBaseLocation.worldTag);

        this.spawnRocations = new ArrayList<>();

        for (WorldTagRocation rocation : template.getSpawnRocations()) {
            spawnRocations.add(rocation.relativeTo(templateBaseLoc)
                    .relativeBase(relativeBaseLocation)
                    .worldTag(relativeBaseLocation.worldTag));
        }

        SpleefStageOptions defaultOptions = template.getDefaultOptions();
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
}
