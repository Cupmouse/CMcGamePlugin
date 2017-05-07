package net.cupmouse.minecraft.game.spleef;

import com.flowpowered.math.vector.Vector3d;
import net.cupmouse.minecraft.worlds.WorldTag;
import net.cupmouse.minecraft.worlds.WorldTagModule;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public abstract class SpleefArea {

    public final WorldTag worldTag;

    protected SpleefArea(WorldTag worldTag) {
        this.worldTag = worldTag;
    }

    public boolean isInArea(Location<World> location) {
        return WorldTagModule.isThis(worldTag, location.getExtent())
                && isInArea(location.getPosition());
    }

    /**
     * ワールド成分は無視してポジションだけでエリア内にいるか確認します。
     * @param position
     * @return
     */
    public abstract boolean isInArea(Vector3d position);
}
