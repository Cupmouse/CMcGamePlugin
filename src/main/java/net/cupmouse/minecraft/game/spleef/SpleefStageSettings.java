package net.cupmouse.minecraft.game.spleef;

import com.flowpowered.math.vector.Vector3d;
import net.cupmouse.minecraft.Utilities;
import net.cupmouse.minecraft.worlds.WorldTag;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.*;
import java.util.stream.Collectors;

public class SpleefStageSettings {

    private List<LocationTaggedWorld> spawnLocations;

    public SpleefStageSettings() {
        this.spawnLocations = new ArrayList<>();
    }

    private SpleefStageSettings(List<LocationTaggedWorld> spawnLocations) {
        this.spawnLocations = spawnLocations;
    }

    public List<LocationTaggedWorld> getSpawnLocations() {
        return spawnLocations;
    }

    public static SpleefStageSettings loadFromConfig(ConfigurationNode node) {
        ConfigurationNode nodeSpawns = node.getNode("spawns");

        ArrayList<LocationTaggedWorld> spawnLocations = nodeSpawns.getChildrenList().stream()
                .map(LocationTaggedWorld::loadFromConfig)
                .collect(Collectors.toCollection(ArrayList::new));

        // ロケーションに回転も必要１
        return new SpleefStageSettings(spawnLocations);
    }
}
