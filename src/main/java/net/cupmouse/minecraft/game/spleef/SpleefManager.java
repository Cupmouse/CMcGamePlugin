package net.cupmouse.minecraft.game.spleef;

import com.google.common.reflect.TypeToken;
import net.cupmouse.minecraft.game.manager.GameManager;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializerCollection;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;

import java.util.*;

public final class SpleefManager implements GameManager<SpleefRoom> {

    private Map<Integer, SpleefRoom> rooms = new HashMap<>();
    private Map<String, SpleefRoom> roomsStageId = new HashMap<>();

    @Override
    public Collection<SpleefRoom> getRooms() {
        return rooms.values();
    }

    @Override
    public Optional<SpleefRoom> getRoom(int roomNumber) {
        return Optional.ofNullable(rooms.get(roomNumber));
    }

    @Override
    public void onInitializationProxy() throws Exception {
        // シリアライザ－登録
        TypeSerializerCollection defaultSerializers = TypeSerializers.getDefaultSerializers();

        defaultSerializers.registerType(TypeToken.of(SpleefStageSettings.class), new SpleefStageSettings.Serializer());

        // TODO ルームのロード
    }

    public Optional<SpleefRoom> getRoomOfStageId(String stageId) {
        return Optional.ofNullable(rooms.get(stageId));
    }

    public Set<String> getStageIds() {
        return roomsStageId.keySet();
    }

    public void addRoom(String stageId, int roomNumber, SpleefRoom spleefRoom) {
        this.rooms.put(roomNumber, spleefRoom);
        this.roomsStageId.put(stageId, spleefRoom);
    }

    public void removeRoom(String stageId) {
        SpleefRoom remove = this.roomsStageId.remove(stageId);

        if (remove == null) {
            return;
        }

        this.rooms.remove(remove.roomNumber);
    }
}
