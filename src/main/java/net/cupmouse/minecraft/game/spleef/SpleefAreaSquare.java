package net.cupmouse.minecraft.game.spleef;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.reflect.TypeToken;
import net.cupmouse.minecraft.worlds.WorldTag;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;

public class SpleefAreaSquare extends SpleefArea {

    public final int minX;
    public final int maxX;
    public final int minY;
    public final int maxY;
    public final int minZ;
    public final int maxZ;

    public SpleefAreaSquare(WorldTag worldTag, int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
        super(worldTag);
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
        this.minZ = minZ;
        this.maxZ = maxZ;
    }

    @Override
    public boolean isInArea(Vector3d position) {
        return (position.getX() >= minX && position.getX() <= maxX)
                && (position.getY() >= minY && position.getX() <= maxY)
                && (position.getZ() >= minX && position.getZ() <= maxZ);
    }

    static class Serializer implements TypeSerializer<SpleefAreaSquare> {

        @Override
        public SpleefAreaSquare deserialize(TypeToken<?> type, ConfigurationNode value) throws ObjectMappingException {
            WorldTag worldTag = value.getValue(TypeToken.of(WorldTag.class));
            int minX = value.getNode("min_x").getInt();
            int minY = value.getNode("min_y").getInt();
            int minZ = value.getNode("min_z").getInt();
            int maxX = value.getNode("max_x").getInt();
            int maxY = value.getNode("max_y").getInt();
            int maxZ = value.getNode("max_z").getInt();

            SpleefAreaSquare areaSquare = new SpleefAreaSquare(worldTag, minX, maxX, minY, maxY, minZ, maxZ);

            return areaSquare;
        }

        @Override
        public void serialize(TypeToken<?> type, SpleefAreaSquare obj, ConfigurationNode value) throws ObjectMappingException {

        }
    }
}
