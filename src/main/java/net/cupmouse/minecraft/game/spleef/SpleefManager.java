package net.cupmouse.minecraft.game.spleef;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.reflect.TypeToken;
import net.cupmouse.minecraft.CMcCore;
import net.cupmouse.minecraft.game.CMcGamePlugin;
import net.cupmouse.minecraft.game.manager.GameException;
import net.cupmouse.minecraft.game.manager.GameManager;
import net.cupmouse.minecraft.game.manager.GameRoomState;
import net.cupmouse.minecraft.worlds.WorldTag;
import net.cupmouse.minecraft.worlds.WorldTagLocation;
import net.cupmouse.minecraft.worlds.WorldTagModule;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializerCollection;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.explosive.PrimedTNT;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.entity.explosive.DetonateExplosiveEvent;
import org.spongepowered.api.event.filter.cause.Last;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class SpleefManager implements GameManager {

    public static final WorldTag WORLD_TAG_SPLEEF = WorldTag.byName("spleef");

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

    public SpleefStageTemplate getStageTemplateOrThrow(String templateId) throws CommandException {
        SpleefStageTemplate stageTemplate = stageTemplates.get(templateId);


        if (stageTemplate == null) {
            throw new CommandException(
                    Text.of(TextColors.RED, "✗そのようなステージテンプレートIDは見つかりませんでした"), false);
        }

        return stageTemplate;
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

    public Optional<SpleefRoom> getRoomPlayerJoin(Player player) {
        for (SpleefRoom spleefRoom : rooms.values()) {
            if (spleefRoom.isPlayerPlaying(player)) {
                return Optional.of(spleefRoom);
            }
        }
        return Optional.empty();
    }

    @Override
    public void onInitializationProxy() throws Exception {
        // イベント登録
        Sponge.getEventManager().registerListeners(CMcCore.getPlugin(), this);

        // シリアライザ－登録
        TypeSerializerCollection defaultSerializers = TypeSerializers.getDefaultSerializers();

        defaultSerializers.registerType(TypeToken.of(SpleefStageOptions.class), new SpleefStageOptions.Serializer());
        defaultSerializers.registerType(TypeToken.of(SpleefStageTemplateInfo.class),
                new SpleefStageTemplateInfo.Serializer());
        defaultSerializers.registerType(TypeToken.of(SpleefStageTemplate.class), new SpleefStageTemplate.Serializer());

        load();
        CMcCore.getLogger().info("Spleefロード完了");
    }

    /*
    ゲーム進行に必要なイベントリスナー
     */

    @Listener
    public void onInteractBlock(InteractBlockEvent.Primary event, @Last Player player) {
        // ゲーム中の部屋の床はぶっ壊せる
        Optional<Location<World>> locationOpt = event.getTargetBlock().getLocation();

        locationOpt.ifPresent(worldLocation -> {
            for (SpleefRoom spleefRoom : rooms.values()) {
                // Spleefの部屋にある床か確認
                // ゲームが進行中でなければ壊せない
                if (spleefRoom.getState() == GameRoomState.IN_PROGRESS
                        && spleefRoom.getStage().getGroundArea().isInArea(worldLocation)) {
                    // 床を壊す

                    worldLocation.setBlockType(BlockTypes.AIR, BlockChangeFlag.NONE);

                    // 上にトーチがあるなら削除
                    Location<World> up = worldLocation.getRelative(Direction.UP);
                    if (up.getBlockType() == BlockTypes.TORCH) {
                        up.setBlockType(BlockTypes.AIR, BlockChangeFlag.NONE);
                        spleefRoom.getMatch().ifPresent(match ->match.getItem()
                                        .filter(spleefItem -> spleefItem instanceof SpleefItemTorch)
                                        .ifPresent(spleefItem -> ((SpleefItemTorch) spleefItem).torchBroke(up)));
                    }

                    event.setCancelled(true);
                }
            }
        });
    }

    @Listener
    public void onPlayerMove(MoveEntityEvent event, @Last Player player) {
        getRoomPlayerJoin(player).ifPresent(spleefRoom -> {
            // プレイヤーが部屋でプレー中のときはfightingAreaから出ると落ちた判定とし、負け確定
            Location<World> playerLocation = player.getLocation();

            spleefRoom.getMatch().ifPresent(match -> {
                if (match.getState() == GameRoomState.IN_PROGRESS
                        && !match.getSpleefPlayer(player.getUniqueId()).dead
                        && !spleefRoom.stage.getFightingArea().isInArea(playerLocation)) {
                    // 落ちた！
                    match.playerDied(player);
                } else if (!spleefRoom.stage.getSpectetorArea().isInArea(playerLocation)) {
                    // 見物範囲内しか移動できない
                    // ここでevent.setToTransform(event.getFromTransform())とすると動けなくなる
                    // (onPlayerMoveがまた呼ばれて…)
                    event.setToTransform(spleefRoom.stage.getWaitingSpawnRocation().convertToTransform().get());
                } else if (match.getState() == GameRoomState.READY) {
                    // 試合開始直前は、位置を動いた場合のみ無効とする。よって首を回転させても大丈夫。
                    if (!event.getFromTransform().getPosition().equals(event.getToTransform().getPosition())) {
                        // 無効にする
                        event.setToTransform(event.getFromTransform());
                    }
                }
            });
        });
    }

    @Listener
    public void onBlockPlaced(ChangeBlockEvent.Place event, @Last Player player) {
        BlockSnapshot snapshot = event.getTransactions().get(0).getFinal();

        if (!WorldTagModule.isThis(WORLD_TAG_SPLEEF, snapshot.getWorldUniqueId())) {
            // spleefワールドでないならさようなら
            return;
        }

        if (snapshot.getState().getType() == BlockTypes.TNT) {
            // TNTが置かれたなら

            // インベントリから削除する
            player.getInventory().query(ItemTypes.TNT).poll(1);
            snapshot.getLocation().ifPresent(worldLocation -> {
                // 着火TNTをスポーンさせる
                Location<World> spawnLocation = worldLocation.add(new Vector3d(.5, .5, .5));
                Entity entity = spawnLocation.createEntity(EntityTypes.PRIMED_TNT);
                spawnLocation.spawnEntity(entity);
            });
        } else if (snapshot.getState().getType() == BlockTypes.TORCH) {
            // トーチが置かれたならトーチアイテム発生中だとして進む
            Optional<Location<World>> locationOptional = snapshot.getLocation();
            if (!locationOptional.isPresent()) {
                return;
            }
            Location<World> location = locationOptional.get();

            Optional<Direction> directionOptional = snapshot.getState().get(Keys.DIRECTION);
            if (!directionOptional.isPresent()) {
                return;
            }


            getRoomPlayerJoin(player).ifPresent(room -> room.getMatch().ifPresent(match -> {
                match.getItem().filter(spleefItem -> spleefItem instanceof SpleefItemTorch).ifPresent(item -> {
                    ((SpleefItemTorch) item).torchPlaced(location);
                });
            }));
        }
    }

    @Listener
    public void onTNTExplode(DetonateExplosiveEvent event) {
        // 爆発は基本的にキャンセル
        event.setCancelled(true);
        // TNTが試合中のものならその試合の地面を破壊する
        if (!(event.getTargetEntity() instanceof PrimedTNT)) {
            return;
        }

        final PrimedTNT primedTNT = (PrimedTNT) event.getTargetEntity();

        // どの試合のものか判定する
        SpleefMatch match = null;
        for (SpleefRoom room : rooms.values()) {
            final Optional<SpleefMatch> spleefMatchOptional = room.getMatch();
            if (!spleefMatchOptional.isPresent()) {
                continue;
            }

            final SpleefMatch spleefMatch = spleefMatchOptional.get();

            final Optional<SpleefItem> spleefItemOptional = spleefMatch.getItem();
            if (!spleefItemOptional.isPresent()) {
                continue;
            }

            final SpleefItem item = spleefItemOptional.get();
            if (item instanceof SpleefItemTNT) {
                if (((SpleefItemTNT) item).getPrimedTNTs().contains(event.getTargetEntity().getUniqueId())) {
                    // この試合のTNTだ！

                    match = spleefMatch;
                    break;
                }
            }
        }

        if (match == null) {
            // どの試合にも属さない
            return;
        }

        final Location<World> centerLocation =
                event.getOriginalExplosion().getLocation().getBlockRelative(Direction.DOWN);
        final World world = centerLocation.getExtent();
        final Vector3i center = centerLocation.getPosition().toInt();

        final int x = center.getX();
        final int y = center.getY();
        final int z = center.getZ();

        final SpleefRoom room = match.getRoom();

        tryRemovingGround(room, world, x - 2, y, z);

        tryRemovingGround(room, world, x - 1, y, z - 1);
        tryRemovingGround(room, world, x - 1, y, z);
        tryRemovingGround(room, world, x - 1, y, z + 1);

        tryRemovingGround(room, world, x, y, z - 2);
        tryRemovingGround(room, world, x, y, z - 1);
        tryRemovingGround(room, world, x, y, z);
        tryRemovingGround(room, world, x, y, z + 1);
        tryRemovingGround(room, world, x, y, z + 2);

        tryRemovingGround(room, world, x + 1, y, z - 1);
        tryRemovingGround(room, world, x + 1, y, z);
        tryRemovingGround(room, world, x + 1, y, z + 1);

        tryRemovingGround(room, world, x + 2, y, z);
    }

    private void tryRemovingGround(SpleefRoom room, World world, int x, int y, int z) {
        if (room.getStage().getGroundArea().isInArea(x, y, z)) {
            // 地面内なら削除

            world.setBlockType(x, y, z, BlockTypes.AIR, BlockChangeFlag.NONE);
        }
    }

    /*
    セーブとロード
     */

    public void load() throws ObjectMappingException, IllegalStateException, NumberFormatException, GameException {
        // TODO
        this.rooms.clear();
        this.stageTemplates.clear();
        CommentedConfigurationNode nodeSpleef = CMcGamePlugin.getGameConfigNode().getNode("spleef");

        // テンプレートのロード
        CommentedConfigurationNode nodeTemplates = nodeSpleef.getNode("templates");
        for (Map.Entry<Object, ? extends CommentedConfigurationNode> entry :
                nodeTemplates.getChildrenMap().entrySet()) {
            SpleefStageTemplate template = entry.getValue().getValue(TypeToken.of(SpleefStageTemplate.class));
            this.stageTemplates.put(((String) entry.getKey()), template);
        }

        // 部屋のロード
        CommentedConfigurationNode nodeRooms = nodeSpleef.getNode("rooms");

        Map<Object, ? extends CommentedConfigurationNode> childrenMap = nodeRooms.getChildrenMap();

        for (Map.Entry<Object, ? extends CommentedConfigurationNode> entry : childrenMap.entrySet()) {
            String templateId = entry.getValue().getNode("template").getString();
            SpleefStageTemplate template = stageTemplates.get(templateId);

            // テンプレートIDにマッチするテンプレートが存在しないならエラー
            if (template == null) {
                throw new IllegalStateException();
            }

            // 基準位置をロード
            WorldTagLocation relativeBaseLocation = entry.getValue().getNode("relative_base_location")
                    .getValue(TypeToken.of(WorldTagLocation.class));

            // 基準位置とテンプレートで新しい部屋を作成し登録
            SpleefStage spleefStage = new SpleefStage(template, relativeBaseLocation);
            int roomNumber = Integer.parseInt((String) entry.getKey());
            addRoom(roomNumber, new SpleefRoom(spleefStage));

            CMcCore.getLogger().info(
                    String.format("SPLEEFルーム%s(テンプレート%s)を読み込みました", roomNumber, templateId));
        }
    }

    public void save() throws ObjectMappingException {
        CommentedConfigurationNode nodeSpleef = CMcGamePlugin.getGameConfigNode().getNode("spleef");

        // TODO クリエイターモードのみ？
        // テンプレートの保存
        CommentedConfigurationNode nodeTemplates = nodeSpleef.getNode("templates");

        for (Map.Entry<String, SpleefStageTemplate> entry : stageTemplates.entrySet()) {
            nodeTemplates.getNode(entry.getKey()).setValue(TypeToken.of(SpleefStageTemplate.class), entry.getValue());
        }

        // 部屋の保存
        CommentedConfigurationNode nodeRooms = nodeSpleef.getNode("rooms");

        for (Map.Entry<Integer, SpleefRoom> entry : rooms.entrySet()) {
            CommentedConfigurationNode nodeRoom = nodeRooms.getNode(entry.getKey().toString());
            SpleefRoom room = entry.getValue();

            nodeRoom.getNode("template").setValue(stageTemplates.getKey(room.getStage().getTemplate()));
            nodeRoom.getNode("relative_base_location")
                    .setValue(TypeToken.of(WorldTagLocation.class), room.stage.getRelativeBaseLocation());
        }
    }

    @Override
    public void onStoppingServerProxy() throws Exception {
        save();
    }
}
