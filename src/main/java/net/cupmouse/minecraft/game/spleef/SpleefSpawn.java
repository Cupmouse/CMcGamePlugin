package net.cupmouse.minecraft.game.spleef;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3f;
import net.cupmouse.minecraft.Utilities;
import net.cupmouse.minecraft.util.WorldNotFoundException;
import net.cupmouse.minecraft.worlds.WorldTag;
import net.cupmouse.minecraft.worlds.WorldTagModule;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;

public final class SpleefSpawn {

    public final WorldTag worldTag;
    public final Vector3d position;
    public final Vector3d rotation;

    public SpleefSpawn(WorldTag worldTag, Vector3d position, Vector3d rotation) {
        this.worldTag = worldTag;
        this.position = position;
        this.rotation = rotation;
    }

    public Location<World> convertLocation() {
        Optional<World> taggedWorld = WorldTagModule.getTaggedWorld(worldTag);

        if (!taggedWorld.isPresent()) {
            throw new WorldNotFoundException();
        }

        return new Location<World>(taggedWorld.get(), position);
    }


    public static SpleefSpawn loadFromConfig(ConfigurationNode configurationNode) {
        Vector3d position = Utilities.loadVector3dFromConfig(configurationNode.getNode("position"));
        Vector3d rotation = Utilities.loadVector3dFromConfig(configurationNode.getNode("rotation"));
        WorldTag worldTag = WorldTag.byName(configurationNode.getString("tag"));

        return new SpleefSpawn(worldTag, position, rotation);
    }
}
