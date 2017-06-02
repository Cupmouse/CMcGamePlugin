package net.cupmouse.minecraft.game.spleef;

import com.google.common.reflect.TypeToken;
import net.cupmouse.minecraft.CMcCore;
import net.cupmouse.minecraft.game.CMcGamePlugin;
import net.cupmouse.minecraft.game.manager.GameManager;
import net.cupmouse.minecraft.game.manager.GameException;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializerCollection;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.json.JSONObject;
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
                    Text.of(TextColors.RED, "✗負の数を部屋番号に指定しないでください。"));
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
        SpleefRoom spleefRoom = stageIdVsRoom.get(stageId);

        if (spleefRoom == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(spleefRoom.stage);
    }

    public String getStageId(SpleefStage stage) {
        return stageIdVsRoom.getKey(stage);
    }

    @Override
    public void onInitializationProxy() throws Exception {
        // シリアライザ－登録
        TypeSerializerCollection defaultSerializers = TypeSerializers.getDefaultSerializers();

//        defaultSerializers.registerType(TypeToken.of(SpleefStage.class), new SpleefStage.Serializer());

        load();
    }

    public void load() {
        // TODO
        this.rooms.clear();
        this.stageIdVsRoom.clear();

        // ルームとステージのロード
        CommentedConfigurationNode nodeRooms = CMcGamePlugin.getGameConfigNode().getNode("spleef", "rooms");

        Map<Object, ? extends CommentedConfigurationNode> childrenMap = nodeRooms.getChildrenMap();

        for (Map.Entry<Object, ? extends CommentedConfigurationNode> entry : childrenMap.entrySet()) {
            SpleefStage stage = null;
            try {
                stage = entry.getValue().getNode("stage").getValue(TypeToken.of(SpleefStage.class));
            } catch (ObjectMappingException e) {
                e.printStackTrace();
            }
            SpleefRoom spleefRoom = new SpleefRoom(stage);

            try {
                addRoom(Integer.parseInt((String) entry.getKey()), spleefRoom);
            } catch (GameException e) {
                e.printStackTrace();
            }
            CMcCore.getLogger().info("SPLEEFルーム[stage=" + stage.stageId + "]を読み込みました");
        }
    }

    public void save() {
        CommentedConfigurationNode nodeRooms = CMcGamePlugin.getGameConfigNode().getNode("spleef", "rooms");

        for (Map.Entry<Integer, SpleefRoom> entry : rooms.entrySet()) {
            CommentedConfigurationNode nodeRoom = nodeRooms.getNode(Integer.toString(entry.getKey()));

            System.out.println(nodeRoom.getNode("stage").toString());
            try {
                nodeRoom.getNode("stage").setValue(TypeToken.of(SpleefStage.class), entry.getValue().stage);
            } catch (ObjectMappingException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onStoppingServerProxy() throws Exception {
        save();
    }
}
