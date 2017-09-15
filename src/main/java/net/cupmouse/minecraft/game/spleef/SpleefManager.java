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
import net.cupmouse.minecraft.worlds.WorldTagArea;
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
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.explosive.Explosive;
import org.spongepowered.api.entity.explosive.PrimedTNT;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.entity.projectile.arrow.Arrow;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.CollideBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.entity.AttackEntityEvent;
import org.spongepowered.api.event.entity.CollideEntityEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.entity.explosive.DetonateExplosiveEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.cause.Last;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
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
    public void onInteractBlockPrimary(InteractBlockEvent.Primary event, @Last Player player) {
        // ゲーム中の部屋の床はぶっ壊せる
        // Spleefワールド以外は知らない
        if (!WorldTagModule.isThis(WORLD_TAG_SPLEEF, player.getWorld())) {
            return;
        }

        // プレイヤーが手に持っているものが鉄シャベルでなければキャンセル
        final Optional<ItemStack> itemInHandOptional = player.getItemInHand(HandTypes.MAIN_HAND);
        if (!itemInHandOptional.isPresent() || itemInHandOptional.get().getType() != ItemTypes.IRON_SHOVEL) {
            event.setCancelled(true);
            return;
        }

        // プレイヤーが部屋に入室していないならキャンセル
        final Optional<SpleefRoom> roomOptional = getRoomPlayerJoin(player);
        if (!roomOptional.isPresent()) {
            event.setCancelled(true);
            return;
        }
        final SpleefRoom room = roomOptional.get();

        // 試合が行われていないならキャンセル
        final Optional<SpleefMatch> matchOptional = room.getMatch();
        if (!matchOptional.isPresent()) {
            event.setCancelled(true);
            return;
        }
        final SpleefMatch match = matchOptional.get();

        // 試合中でなければキャンセル
        if (match.getState() != GameRoomState.IN_PROGRESS) {
            event.setCancelled(true);
            return;
        }

        // クリックした先のロケーションが存在しないならキャンセル
        final Optional<Location<World>> locationOptional = event.getTargetBlock().getLocation();
        if (!locationOptional.isPresent()) {
            event.setCancelled(true);
            return;
        }
        final Location<World> location = locationOptional.get();

        // クリックしたブロックがSpleefの部屋にある床か確認
        if (!room.getStage().getGroundArea().isInArea(location)) {
            event.setCancelled(true);
            return;
        }

        // 床を壊す
        location.setBlockType(BlockTypes.AIR, BlockChangeFlag.NONE);

        // 上にトーチがあるなら削除
        Location<World> up = location.getRelative(Direction.UP);
        if (up.getBlockType() == BlockTypes.TORCH) {
            up.setBlockType(BlockTypes.AIR, BlockChangeFlag.NONE);

            // 何故か終わっているのにトーチが残っていたりすると..何も起きませんなのでこれでOK!
            match.getItem().filter(spleefItem -> spleefItem instanceof SpleefItemTorch)
                    .ifPresent(spleefItem -> ((SpleefItemTorch) spleefItem).torchBroke(up));
        }

        event.setCancelled(true);
    }

    @Listener
    public void onInteractBlockSecondary(InteractBlockEvent.Secondary event, @Last Player player) {
        // TNTアイテムのために、TNTを置こうとしたときにTNTをスポーンさせる

        // spleefワールドでないならさようなら
        if (!WorldTagModule.isThis(WORLD_TAG_SPLEEF, player.getWorld().getUniqueId())) {
            return;
        }

        // メインハンドにTNTを持っていない場合さようなら
        final Optional<ItemStack> itemInHand = player.getItemInHand(HandTypes.MAIN_HAND);

        if (!itemInHand.isPresent()) {
            event.setCancelled(true);
            return;
        } else if (itemInHand.get().getType() != ItemTypes.TNT) {
            final ItemStack itemStackInHand = itemInHand.get();

            // TNTでなくトーチを持っている場合はChangeBlockEvent.Placeに進むようにイベントをキャンセルしない
            // 弓を持っている場合も矢を引けなくなるのでキャンセルしない
            if (itemStackInHand.getType() != ItemTypes.TORCH && itemStackInHand.getType() != ItemTypes.BOW) {
                event.setCancelled(true);
            }
            return;
        }

        // ブロックの上面をクリックしていなければキャンセル
        if (event.getTargetSide() != Direction.UP) {
            event.setCancelled(true);
            return;
        }

        // プレイヤーがSpleefの部屋に入室していないならキャンセル
        final Optional<SpleefRoom> roomOptional = getRoomPlayerJoin(player);
        if (!roomOptional.isPresent()) {
            event.setCancelled(true);
            return;
        }

        final SpleefRoom room = roomOptional.get();

        // 置こうとした場所を確認、無いならキャンセル
        final Optional<Location<World>> locationOptional = event.getTargetBlock().getLocation();

        if (!locationOptional.isPresent()) {
            event.setCancelled(true);
            return;
        }

        final Location<World> clickedBlockLocation = locationOptional.get();

        // クリックしたブロックがSpleef地面でなければキャンセル
        if (!room.getStage().getGroundArea().isInArea(clickedBlockLocation)) {
            event.setCancelled(true);
            return;
        }

        // 部屋で試合が行われていない場合はキャンセル
        final Optional<SpleefMatch> matchOptional = room.getMatch();
        if (!matchOptional.isPresent()) {
            event.setCancelled(true);
            return;
        }

        // 発生しているアイテムがないもしくはTNTアイテムではないときはキャンセル
        final Optional<SpleefItem> itemOptional = matchOptional.get().getItem();
        if (!itemOptional.isPresent() || !(itemOptional.get() instanceof SpleefItemTNT)) {
            event.setCancelled(true);
            return;
        }

        // クリックしたブロックと、クリックした側面からブロックが置かれる位置を取得し、そこに着火TNTをスポーンさせる

        Location<World> tntBlockLocation = clickedBlockLocation.getRelative(event.getTargetSide());
        Location<World> spawnLocation = tntBlockLocation.add(new Vector3d(.5, .5, .5));
        Entity entity = spawnLocation.createEntity(EntityTypes.PRIMED_TNT);

        // アイテムにTNTがスポーンしたことを通知する
        ((SpleefItemTNT) itemOptional.get()).tntPlaced(entity.getUniqueId());

        // インベントリから削除する
        player.getInventory().query(ItemTypes.TNT).poll(1);
        // 着火TNTをスポーンさせる
        spawnLocation.spawnEntity(entity);
    }

    @Listener
    public void onPlayerMove(MoveEntityEvent event, @Last Player player) {
        // FIXME クライアント上ではspleefワールドにいるのにサーバーにはlobbyにいるというわけのわからない状態になる
        if (!WorldTagModule.isThis(WORLD_TAG_SPLEEF, event.getFromTransform().getExtent())) {
            return;
        }


//        if (event.getFromTransform().getExtent() != event.getToTransform().getExtent()) {
//            return;
//        }

        getRoomPlayerJoin(player).ifPresent(spleefRoom -> {
            // プレイヤーが部屋でプレー中のときはfightingAreaから出ると落ちた判定とし、負け確定
            Location<World> playerLocation = player.getLocation();

            spleefRoom.getMatch().ifPresent(match -> {
                if (!spleefRoom.getStage().getSpectetorArea().isInArea(playerLocation)) {
                    // 見物範囲内しか移動できない
                    // ここでevent.setToTransform(event.getFromTransform())とすると動けなくなる
                    // (onPlayerMoveがまた呼ばれて…)
                    event.setToTransform(spleefRoom.getStage().getWaitingSpawnRocation().convertToTransform().get());
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
    public void onBlockPlace(ChangeBlockEvent.Place event, @Last Player player) {
        // トーチアイテムのために、トーチが置かれたら登録する

        if (!WorldTagModule.isThis(WORLD_TAG_SPLEEF, player.getWorld().getUniqueId())) {
            // spleefワールドでないならさようなら
            return;
        }

        Transaction<BlockSnapshot> transaction = event.getTransactions().get(0);
        BlockSnapshot snapshot = transaction.getFinal();

        if (snapshot.getState().getType() == BlockTypes.TORCH) {
            // トーチが置かれたならトーチアイテム発生中だとして進む
            // 置いたトーチの位置が設定されていない場合はキャンセル TODO 起こるの？
            Optional<Location<World>> locationOptional = snapshot.getLocation();

            if (!locationOptional.isPresent()) {
                return;
            }

            Location<World> location = locationOptional.get();

            // トーチがステージの側面に置かれた場合はキャンセルしてなかったことにする
            Optional<Direction> directionOptional = snapshot.getState().get(Keys.DIRECTION);
            if (!directionOptional.isPresent() || directionOptional.get() != Direction.UP) {
                event.setCancelled(true);
                return;
            }

            // トーチを置いたプレイヤーがどの部屋にも入室していなければキャンセル
            Optional<SpleefRoom> roomOptional = getRoomPlayerJoin(player);
            if (!roomOptional.isPresent()) {
                event.setCancelled(true);
                return;
            }

            SpleefRoom room = roomOptional.get();

            // ゲームが進行中でなければキャンセル
            if (room.getState() != GameRoomState.IN_PROGRESS) {
                event.setCancelled(true);
                return;
            }

            // ステージの地面の上に置かれたトーチでなければキャンセル
            if (!room.getStage().getGroundArea().shift(0, 1, 0).isInArea(location)) {
                event.setCancelled(true);
                return;
            }

            // 試合が行われていなければキャンセル
            Optional<SpleefMatch> matchOptional = room.getMatch();

            if (!matchOptional.isPresent()) {
                event.setCancelled(true);
                return;
            }

            // トーチアイテムが発生中でなければキャンセル
            Optional<SpleefItem> itemOptional = matchOptional.get().getItem();
            if (!itemOptional.isPresent()) {
                event.setCancelled(true);
                return;
            }

            SpleefItem item = itemOptional.get();
            if (!(item instanceof SpleefItemTorch)) {
                event.setCancelled(true);
                return;
            }

            // アイテムにトーチの位置を記録してもらって管理を任せる
            ((SpleefItemTorch) item).torchPlaced(location);
        }
    }

    @Listener
    public void onDetonateExplosive(DetonateExplosiveEvent event) {
        // 爆発は基本的にキャンセル FIXME クラッシュする
//        event.setCancelled(true);
        // TNTが試合中のものならその試合の地面を破壊する

        // TNT以外の爆発はそのままにする
        if (!(event.getTargetEntity() instanceof PrimedTNT)) {
            return;
        }

        final PrimedTNT primedTNT = (PrimedTNT) event.getTargetEntity();

        // Spleefワールド以外の話は知らないのでそのまま
        if (!WorldTagModule.isThis(WORLD_TAG_SPLEEF, primedTNT.getWorld())) {
            return;
        }

        // 爆発時のアニメーションを表示する
        event.getExplosionBuilder().shouldPlaySmoke(true);

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
                if (((SpleefItemTNT) item).getPrimedTNTs().contains(primedTNT.getUniqueId())) {
                    // この試合のTNTだ！

                    match = spleefMatch;
                    break;
                }
            }
        }

        if (match == null) {
            // TODO どの試合にも属さない爆発、起きないはず
            return;
        }

        final Location<World> centerLocation =
                event.getOriginalExplosion().getLocation().getBlockRelative(Direction.DOWN);
        final World world = centerLocation.getExtent();
        final Vector3i center = centerLocation.getPosition().toInt();

        final SpleefRoom room = match.getRoom();

        tryRemovingGroundCircle(room.getStage().getGroundArea(), world, center.getX(), center.getY(), center.getZ());
    }

    private void tryRemovingGroundCircle(WorldTagArea groundArea, World world, int x, int y, int z) {
        tryRemovingGround(groundArea, world, x - 2, y, z);

        tryRemovingGround(groundArea, world, x - 1, y, z - 1);
        tryRemovingGround(groundArea, world, x - 1, y, z);
        tryRemovingGround(groundArea, world, x - 1, y, z + 1);

        tryRemovingGround(groundArea, world, x, y, z - 2);
        tryRemovingGround(groundArea, world, x, y, z - 1);
        tryRemovingGround(groundArea, world, x, y, z);
        tryRemovingGround(groundArea, world, x, y, z + 1);
        tryRemovingGround(groundArea, world, x, y, z + 2);

        tryRemovingGround(groundArea, world, x + 1, y, z - 1);
        tryRemovingGround(groundArea, world, x + 1, y, z);
        tryRemovingGround(groundArea, world, x + 1, y, z + 1);

        tryRemovingGround(groundArea, world, x + 2, y, z);
    }

    private void tryRemovingGround(WorldTagArea groundArea, World world, int x, int y, int z) {
        if (groundArea.isInArea(x, y, z)) {
            // 地面内なら削除

            world.setBlockType(x, y, z, BlockTypes.AIR, BlockChangeFlag.NONE);

            // トーチのことは同時にアイテムが発生することがないので気にしなくて良い
        }
    }

    @Listener
    public void onCollideEntity(CollideEntityEvent event, @First Arrow arrow) {
        // 矢でプレイヤーがプレイヤーを撃ったときのイベント

        // Spleefワールドでなければ関係ないのでさようなら
        if (!WorldTagModule.isThis(WORLD_TAG_SPLEEF, arrow.getWorld())) {
            return;
        }

        // 矢を撃ったのがプレイヤーでない場合は削除しキャンセル
        if (!(arrow.getShooter() instanceof Player)) {
            arrow.remove();
            event.setCancelled(true);
            return;
        }
        final Player shooter = (Player) arrow.getShooter();

        // プレイヤーが部屋に入室していないなら矢を消しキャンセル
        final Optional<SpleefRoom> roomOptional = getRoomPlayerJoin(shooter);
        if (!roomOptional.isPresent()) {
            arrow.remove();
            event.setCancelled(true);
            return;
        }

        final SpleefRoom room = roomOptional.get();

        // 試合中でなければ矢を消しキャンセル
        if (room.getState() != GameRoomState.IN_PROGRESS) {
            arrow.remove();
            event.setCancelled(true);
            return;
        }

        // 矢が当たったのが同じ試合の相手でない場合は矢を消しキャンセル
        // TODO 自分自身に当たった物は無効にしないと撃った瞬間に自分自身に当たって地面がぶっ壊れるので
        // TODO それも条件に入れているが、動物＋撃った人が同時にいる場合矢が削除されず、もし地面に当たらないと
        // TODO 残ってしまう
        Player targetPlayer = null;

        boolean shooterIntact = false;

        for (Entity entity : event.getEntities()) {
            if (shooter.equals(entity)) {
                shooterIntact = true;
            } else {
                if (entity instanceof Player && room.isPlayerPlaying(((Player) entity))) {
                    targetPlayer = ((Player) entity);
                    break;
                }
            }
        }

        if (targetPlayer == null) {
            // 撃った本人のみがぶつかっているなら
            if (!shooterIntact) {
                arrow.remove();
            }
            event.setCancelled(true);
            return;
        }

        // yを無視した距離で、20ブロックより遠くから撃った場合効果なし。矢を消しキャンセル
        final Location<World> targetLocation = targetPlayer.getLocation();
        if (shooter.getLocation().getPosition().mul(1, 0, 1)
                .distanceSquared(targetLocation.getPosition().mul(1, 0, 1)) > 400) {
            arrow.remove();
            event.setCancelled(true);
            return;
        }

        // プレイヤーの現在位置のブロックから下がっていってステージの地面を5ブロック以内で探しそこを中心に地面破壊
        // ジャンプ状態かもしれないので1ブロック下を壊さないようにするため
        final Vector3i targetBlockPosition = targetLocation.getBlockPosition();
        final int x = targetBlockPosition.getX();
        final int y = targetBlockPosition.getY();
        final int z = targetBlockPosition.getZ();
        final WorldTagArea groundArea = room.getStage().getGroundArea();

        for (int i = y; i > y - 5; i--) {
            if (groundArea.isInArea(x, i, z)) {
                // 中心に破壊
                tryRemovingGroundCircle(groundArea, targetPlayer.getWorld(), x, i, z);
                break;
            }
        }

        // 矢を削除する
        arrow.remove();
    }

    @Listener
    public void onCollideBlock(CollideBlockEvent event, @First Arrow arrow) {
        // Spleefワールドの話でなければ終了
        if (!WorldTagModule.isThis(WORLD_TAG_SPLEEF, event.getTargetLocation().getExtent())) {
            return;
        }

        // 矢を撃ったのがプレイヤーでない場合は削除しキャンセル
        if (!(arrow.getShooter() instanceof Player)) {
            arrow.remove();
            event.setCancelled(true);
            return;
        }

        // プレイヤーが部屋に入室していない場合は矢を削除しキャンセル
        final Optional<SpleefRoom> roomOptional = getRoomPlayerJoin(((Player) arrow.getShooter()));
        if (!roomOptional.isPresent()) {
            arrow.remove();
            event.setCancelled(true);
            return;
        }

        final SpleefRoom room = roomOptional.get();

        // 試合が行われていない場合は矢を削除しキャンセル
        if (room.getState() != GameRoomState.IN_PROGRESS) {
            arrow.remove();
            event.setCancelled(true);
            return;
        }

        // 矢が刺さったブロックが地面でないなら矢を削除しキャンセル
        if (!room.getStage().getGroundArea().isInArea(event.getTargetLocation())) {
            arrow.remove();
            event.setCancelled(true);
            return;
        }

        // 刺さったブロックを削除する
        event.getTargetLocation().setBlockType(BlockTypes.AIR, BlockChangeFlag.NONE);
        // 矢は削除する
        arrow.remove();
    }

    @Listener
    public void onDropItem(DropItemEvent event, @First Player player) {
        // Spleefワールドのことでなければさようなら
        if (!WorldTagModule.isThis(WORLD_TAG_SPLEEF, player.getWorld())) {
            return;
        }

        // 現時点でアイテムドロップは禁止
        event.setCancelled(true);
        return;
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
                    .setValue(TypeToken.of(WorldTagLocation.class), room.getStage().getRelativeBaseLocation());
        }
    }

    @Override
    public void onStoppingServerProxy() throws Exception {
        save();
    }
}
