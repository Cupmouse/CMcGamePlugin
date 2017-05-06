package net.cupmouse.minecraft.game.spleef;

import com.flowpowered.math.vector.Vector3d;
import net.cupmouse.minecraft.Utilities;
import net.cupmouse.minecraft.util.WorldNotFoundException;
import net.cupmouse.minecraft.worlds.WorldTag;
import net.cupmouse.minecraft.worlds.WorldTagModule;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;

public class LocationTaggedWorld {

    public final WorldTag worldTag;
    public final Vector3d vector3d;

    public LocationTaggedWorld(WorldTag worldTag, Vector3d vector3d) {
        this.worldTag = worldTag;
        this.vector3d = vector3d;
    }

    public Location<World> convertLocation() {
        Optional<World> taggedWorld = WorldTagModule.getTaggedWorld(worldTag);

        if (!taggedWorld.isPresent()) {
            throw new WorldNotFoundException();
        }

        return new Location<World>(taggedWorld.get(), vector3d);
    }


    public static LocationTaggedWorld loadFromConfig(ConfigurationNode configurationNode) {
        Vector3d vector3d = Utilities.loadVector3dFromConfig(configurationNode);
        WorldTag worldTag = WorldTag.byName(configurationNode.getString("tag"));

        return new LocationTaggedWorld(worldTag, vector3d);
    }
}
