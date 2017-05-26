package net.cupmouse.minecraft.game.spleef;

import com.google.common.reflect.TypeToken;
import net.cupmouse.minecraft.game.CMcGamePlugin;
import net.cupmouse.minecraft.game.manager.GameManager;
import net.cupmouse.minecraft.game.manager.GameException;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializerCollection;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.spongepowered.api.Game;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.*;

public final class SpleefManager implements GameManager<SpleefRoom> {

    private BidiMap<Integer, SpleefRoom> rooms = new DualHashBidiMap<>();
    private BidiMap<String, SpleefRoom> stageIdVsRoom = new DualHashBidiMap<>();

    @Override
    public Collection<SpleefRoom> getRooms() {
        return Collections.unmodifiableCollection(rooms.values());
    }

    public Set<Map.Entry<SpleefRoom, Integer>> getRoomsAndItsNumber() {
        return Collections.unmodifiableSet(rooms.inverseBidiMap().entrySet());
    }

    @Override
    public Optional<SpleefRoom> getRoom(int roomNumber) {
        return Optional.ofNullable(rooms.get(roomNumber));
    }

    public void newRoom(int roomNumber, String stageId) throws GameException {
        SpleefRoom spleefRoom = new SpleefRoom(SpleefStage.creator(stageId));
        addRoom(roomNumber, spleefRoom);
    }

    public void removeRoom(int roomNumber) throws GameException {
        SpleefRoom removed = rooms.remove(roomNumber);

        if (removed == null) {
            throw new GameException(Text.of(TextColors.RED, "✗その部屋番号は存在しません。"));
        }

        // 部屋を閉じる
        removed.closeRoom();

        this.rooms.remove(roomNumber);
    }

    private void addRoom(int roomNumber, SpleefRoom spleefRoom) throws GameException {
        if (roomNumber < 0) {
            throw new GameException(
                    Text.of(TextColors.RED, "✗負の数を部屋番号に指定すると使いづらいのでやめてください。"));
        }
        if (rooms.containsKey(roomNumber)) {
            throw new GameException(Text.of(TextColors.RED, "✗部屋番号が重複しています。"));
        }
        if (stageIdVsRoom.containsKey(spleefRoom.stage.stageId)) {
            throw new GameException(Text.of(TextColors.RED, "✗ステージIDが重複しています。"));
        }

        this.rooms.put(roomNumber, spleefRoom);
        this.stageIdVsRoom.put(spleefRoom.stage.stageId, spleefRoom);
    }

    public Set<String> getStageIds() {
        return Collections.unmodifiableSet(stageIdVsRoom.keySet());
    }

    public Optional<SpleefStage> getStage(String stageId) {
        return Optional.ofNullable(stageIdVsRoom.get(stageId).stage);
    }

    public String getStageId(SpleefStage stage) {
        return stageIdVsRoom.getKey(stage);
    }

    @Override
    public void onInitializationProxy() throws Exception {
        // シリアライザ－登録
        TypeSerializerCollection defaultSerializers = TypeSerializers.getDefaultSerializers();

        defaultSerializers.registerType(TypeToken.of(SpleefStage.class), new SpleefStage.Serializer());

        // ルームとステージのロード
        CommentedConfigurationNode nodeRooms = CMcGamePlugin.getGameConfigNode().getNode("spleef", "rooms");

        Map<Object, ? extends CommentedConfigurationNode> childrenMap = nodeRooms.getChildrenMap();

        for (Map.Entry<Object, ? extends CommentedConfigurationNode> entry : childrenMap.entrySet()) {
            SpleefStage stage = entry.getValue().getNode("stage").getValue(TypeToken.of(SpleefStage.class));
            SpleefRoom spleefRoom = new SpleefRoom(stage);

            addRoom((int) entry.getKey(), spleefRoom);
        }
    }

    @Override
    public void onStoppingServerProxy() throws Exception {
        CommentedConfigurationNode nodeRooms = CMcGamePlugin.getGameConfigNode().getNode("spleef", "rooms");

        for (Map.Entry<Integer, SpleefRoom> entry : rooms.entrySet()) {
            CommentedConfigurationNode nodeRoom = nodeRooms.getNode(entry.getKey());

            nodeRoom.getNode("stage").setValue(TypeToken.of(SpleefStage.class), entry.getValue().stage);
        }
    }
}
