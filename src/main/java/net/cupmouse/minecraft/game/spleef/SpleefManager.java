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
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.*;

public final class SpleefManager implements GameManager<SpleefRoom> {

    private BidiMap<String, SpleefStage> stages = new DualHashBidiMap<>();

    private BidiMap<Integer, SpleefRoom> rooms = new DualHashBidiMap<>();
    private Map<SpleefStage, SpleefRoom> roomStageMap = new HashMap<>();

    @Override
    public Collection<SpleefRoom> getRooms() {
        return Collections.unmodifiableCollection(rooms.values());
    }

    @Override
    public Set<Map.Entry<SpleefRoom, Integer>> getRoomAndItsNumber() {
        return Collections.unmodifiableSet(rooms.inverseBidiMap().entrySet());
    }

    @Override
    public Optional<SpleefRoom> getRoom(int roomNumber) {
        return Optional.ofNullable(rooms.get(roomNumber));
    }

    public void addRoom(int roomNumber, SpleefRoom spleefRoom) throws GameException {
        String stageId = stages.getKey(spleefRoom.stage);

        if (stageId == null) {
            throw new GameException(Text.of(TextColors.RED, "✗登録されていないステージです。"));
        }

        this.rooms.put(roomNumber, spleefRoom);
        this.roomStageMap.put(spleefRoom.stage, spleefRoom);
    }

    public void removeRoom(int roomNumber) {
        SpleefRoom removed = rooms.remove(roomNumber);

        if (removed == null) {
            return;
        }

        // 部屋を閉じる
        removed.closeRoom();

        this.rooms.remove(roomNumber);
        this.roomStageMap.remove(removed.stage);
    }

    public Set<String> getStageIds() {
        return Collections.unmodifiableSet(stages.keySet());
    }

    public Optional<SpleefStage> getStage(String stageId) {
        return Optional.ofNullable(stages.get(stageId));
    }

    public String getStageId(SpleefStage stage) {
        return stages.getKey(stage);
    }

    public void addStage(String stageId, SpleefStage stage) throws GameException {
        if (stages.containsKey(stageId)) {
            throw new GameException(Text.of(TextColors.RED, "✗ステージIDが重複しています。"));
        }

        this.stages.put(stageId, stage);
    }

    public void removeStage(String stageId) throws GameException {
        SpleefStage spleefStage = stages.get(stageId);

        if (spleefStage == null) {
            throw new GameException(Text.of(TextColors.RED, "✗そのステージIDのステージが存在しません。"));
        }

        SpleefRoom spleefRoom = roomStageMap.get(spleefStage);

        if (spleefRoom != null) {
            throw new GameException(
                    Text.of(TextColors.RED,
                            "✗そのステージは部屋[", rooms.getKey(spleefRoom), "]に割り当てられています。"));
        }
    }

    @Override
    public void onInitializationProxy() throws Exception {
        // シリアライザ－登録
        TypeSerializerCollection defaultSerializers = TypeSerializers.getDefaultSerializers();

        defaultSerializers.registerType(TypeToken.of(SpleefStage.class), new SpleefStage.Serializer());

        // ステージのロード
        CommentedConfigurationNode nodeStages = CMcGamePlugin.getGameConfigNode().getNode("spleef", "stages");

        Map<Object, ? extends CommentedConfigurationNode> childrenMap = nodeStages.getChildrenMap();

        for (Map.Entry<Object, ? extends CommentedConfigurationNode> entry : childrenMap.entrySet()) {
            SpleefStage stageSettings = entry.getValue().getValue(TypeToken.of(SpleefStage.class));

            addStage((String) entry.getKey(), stageSettings);
        }

        // ロードしたステージに対応するルームを作成する
    }

    @Override
    public void onStoppingServerProxy() throws Exception {
        CommentedConfigurationNode nodeStages = CMcGamePlugin.getGameConfigNode().getNode("spleef", "stages");

        for (Map.Entry<String, SpleefStage> entry : stages.entrySet()) {
            nodeStages.getNode(entry.getKey()).setValue(TypeToken.of(SpleefStage.class), entry.getValue());
        }
    }
}
