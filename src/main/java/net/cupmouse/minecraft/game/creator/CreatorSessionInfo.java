package net.cupmouse.minecraft.game.creator;

import net.cupmouse.minecraft.worlds.*;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;

public final class CreatorSessionInfo {

    public WorldTagArea loadedArea;

    public WorldTagLocation worldTagLoc;

    public Location<World> firstLoc;

    public Location<World> secondLoc;

    public Optional<WorldTagAreaSquare> createAreaSquare() {
        if (firstLoc.getExtent() != secondLoc.getExtent()) {
            return Optional.empty();
        }

        Optional<WorldTag> worldTagOptional = WorldTagModule.whatIsThisWorld(firstLoc.getExtent());

        return worldTagOptional.map(worldTag ->
                new WorldTagAreaSquare(worldTag, firstLoc.getBlockPosition(), secondLoc.getBlockPosition()));
    }
}
