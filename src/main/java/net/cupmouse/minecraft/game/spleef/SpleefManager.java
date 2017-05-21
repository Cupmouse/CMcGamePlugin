package net.cupmouse.minecraft.game.spleef;

import com.google.common.reflect.TypeToken;
import net.cupmouse.minecraft.game.manager.GameManager;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializerCollection;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;

import java.util.*;

public final class SpleefManager implements GameManager<SpleefRoom> {

    private Map<String, SpleefRoom> rooms = new HashMap<>();

    @Override
    public Collection<SpleefRoom> getRooms() {
        return rooms.values();
    }

    @Override
    public void onInitializationProxy() throws Exception {
        // シリアライザ－登録
        TypeSerializerCollection defaultSerializers = TypeSerializers.getDefaultSerializers();

        defaultSerializers.registerType(TypeToken.of(SpleefStageSettings.class), new SpleefStageSettings.Serializer());
    }

    public Optional<SpleefRoom> getRoomOfStageId(String stageId) {
        return Optional.ofNullable(rooms.get(stageId));
    }

    public Set<String> getStageIds() {
        return rooms.keySet();
    }
}
