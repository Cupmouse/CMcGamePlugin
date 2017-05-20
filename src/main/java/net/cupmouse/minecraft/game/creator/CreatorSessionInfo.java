package net.cupmouse.minecraft.game.creator;

import net.cupmouse.minecraft.worlds.WorldTagArea;
import net.cupmouse.minecraft.worlds.WorldTagLocation;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public final class CreatorSessionInfo {

    public WorldTagArea worldTagArea;

    public WorldTagLocation worldTagLoc;

    public Location<World> firstLoc;

    public Location<World> secondLoc;
}
