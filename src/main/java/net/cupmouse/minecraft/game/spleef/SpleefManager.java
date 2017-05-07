package net.cupmouse.minecraft.game.spleef;

import com.google.common.reflect.TypeToken;
import net.cupmouse.minecraft.game.manager.GameManager;
import net.cupmouse.minecraft.worlds.WorldTag;
import net.cupmouse.minecraft.worlds.WorldTagModule;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializerCollection;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class SpleefManager implements GameManager<SpleefRoom> {

    private Map<Integer, SpleefRoom> rooms = new HashMap<>();

    @Override
    public Collection<SpleefRoom> getRooms() {
        return rooms.values();
    }

    @Override
    public void onInitializationProxy() throws Exception {
        // シリアライザ－登録
        TypeSerializerCollection defaultSerializers = TypeSerializers.getDefaultSerializers();

        defaultSerializers
                .registerType(TypeToken.of(SpleefAreaSquare.class), new SpleefAreaSquare.Serializer());
        defaultSerializers.registerType(TypeToken.of(SpleefStageSettings.class), new SpleefStageSettings.Serializer());
    }
}
