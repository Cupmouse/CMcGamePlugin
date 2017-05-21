package net.cupmouse.minecraft.game.spleef;

import com.google.common.reflect.TypeToken;
import net.cupmouse.minecraft.game.CMcGamePlugin;
import net.cupmouse.minecraft.game.manager.GameManager;
import net.cupmouse.minecraft.game.manager.GameException;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializerCollection;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.*;

public final class SpleefManager implements GameManager<SpleefRoom> {

    private Map<String, SpleefStage> stages = new HashMap<>();
    private Map<SpleefStage, String> stageIds = new HashMap<>();

    private Map<Integer, SpleefRoom> rooms = new HashMap<>();
    private Map<SpleefStage, SpleefRoom> roomStageMap = new HashMap<>();

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

        defaultSerializers.registerType(TypeToken.of(SpleefStage.class), new SpleefStage.Serializer());

        // ステージのロード
        CommentedConfigurationNode nodeStages = CMcGamePlugin.getGameConfigNode().getNode("spleef", "stages");

        Map<Object, ? extends CommentedConfigurationNode> childrenMap = nodeStages.getChildrenMap();

        for (Map.Entry<Object, ? extends CommentedConfigurationNode> entry : childrenMap.entrySet()) {
            SpleefStage stageSettings = entry.getValue().getValue(TypeToken.of(SpleefStage.class));

            addStage((String) entry.getKey(), stageSettings);
        }
    }

    public void addStage(String stageId, SpleefStage stage) throws GameException {
        if (stages.containsKey(stageId)) {
            throw new GameException(Text.of(TextColors.RED, "✗ステージIDが重複しています。"));
        }

        this.stages.put(stageId, stage);
        this.stageIds.put(stage, stageId);
    }

    @Override
    public void onStoppingServerProxy() throws Exception {
        CommentedConfigurationNode nodeStages = CMcGamePlugin.getGameConfigNode().getNode("spleef", "stages");

        for (Map.Entry<String, SpleefStage> entry : stages.entrySet()) {
            nodeStages.getNode(entry.getKey()).setValue(TypeToken.of(SpleefStage.class), entry.getValue());
        }
    }

    public Optional<SpleefStage> getStage(String stageId) {
    }

    public Set<String> getStageIds() {
        return stages.keySet();
    }

    public Optional<SpleefRoom> getRoomOfStageId(String stageId) {
        SpleefStage stage = stages.get(stageId);

        roomStageMap

        return Optional.ofNullable(roomStageMap.get(stageId));
    }

    public void addRoom(int roomNumber, SpleefRoom spleefRoom) throws GameException {
        String stageId = stageIds.get(spleefRoom.stage);

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
}
