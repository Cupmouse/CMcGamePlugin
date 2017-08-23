package net.cupmouse.minecraft.game.spleef;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.reflect.TypeToken;
import net.cupmouse.minecraft.CMcCore;
import net.cupmouse.minecraft.game.CMcGamePlugin;
import net.cupmouse.minecraft.game.manager.GameException;
import net.cupmouse.minecraft.game.manager.GameManager;
import net.cupmouse.minecraft.worlds.WorldTagLocation;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializerCollection;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class SpleefManager implements GameManager {

    /**
     * 部屋番号とSpleefRoomのマップ
     */
    private BidiMap<Integer, SpleefRoom> rooms = new DualHashBidiMap<>();
    private BidiMap<String, SpleefStageTemplate> stageTemplates = new DualHashBidiMap<>();

    public Optional<SpleefRoom> getRoomsByNumber(int roomNumber) {
        return Optional.ofNullable(rooms.get(roomNumber));
    }

    public Set<String> getStageTemplateIds() {
        return stageTemplates.keySet();
    }

    public Optional<SpleefStageTemplate> getStageTemplate(String templateId) {
        SpleefStageTemplate stageTemplate = stageTemplates.get(templateId);

        return Optional.ofNullable(stageTemplate);
    }

    private void addStageTemplate(SpleefStageTemplate template, String templateId) throws GameException {
        if (stageTemplates.containsKey(templateId)) {
            throw new GameException(Text.of(TextColors.RED, "✗ステージテンプレートIDが重複しています"));
        }

        stageTemplates.put(templateId, template);
    }

    public SpleefStageTemplate newStageTemplate(String templateId) throws GameException {
        SpleefStageTemplate template = SpleefStageTemplate.createNew();
        addStageTemplate(template, templateId);
        return template;
    }

    public void newRoom(int roomNumber, String templateId, WorldTagLocation relativeBaseLocation) throws GameException {
        SpleefStageTemplate stageTemplate = stageTemplates.get(templateId);

        if (stageTemplate == null) {
            throw new GameException(Text.of(TextColors.RED, "✗そのステージテンプレートは存在しません"));
        }

        SpleefStage stage = new SpleefStage(stageTemplate, relativeBaseLocation);
        SpleefRoom spleefRoom = new SpleefRoom(stage);
        addRoom(roomNumber, spleefRoom);
    }

    public void removeRoom(int roomNumber) throws GameException {
        SpleefRoom removed = rooms.remove(roomNumber);

        if (removed == null) {
            throw new GameException(Text.of(TextColors.RED, "✗その部屋番号は存在しません。もう一度ご確認ください"));
        }

        // 部屋を閉じる
        removed.closeRoom();

        this.rooms.remove(roomNumber);
    }

    private void addRoom(int roomNumber, SpleefRoom room) throws GameException {
        if (rooms.containsKey(roomNumber)) {
            throw new GameException(
                    Text.of(TextColors.RED, String.format("✗部屋番号が重複しています [%d]", roomNumber)));
        }

        this.rooms.put(roomNumber, room);
    }

    public Set<Integer> getRoomNumbers() {
        return Collections.unmodifiableSet(rooms.keySet());
    }

    public Set<SpleefRoom> getRooms() {
        return Collections.unmodifiableSet(rooms.values());
    }

    public Set<Map.Entry<SpleefRoom, Integer>> getRoomsAndItsNumber() {
        return rooms.inverseBidiMap().entrySet();
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
        this.stageTemplates.clear();

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
                int roomNumber = Integer.parseInt((String) entry.getKey());
                addRoom(roomNumber, spleefRoom);
                CMcCore.getLogger().info(String.format("SPLEEFルーム(ステージ%s)を読み込みました", roomNumber));
            } catch (GameException e) {
                e.printStackTrace();
            }
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
