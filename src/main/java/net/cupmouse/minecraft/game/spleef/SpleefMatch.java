package net.cupmouse.minecraft.game.spleef;

import net.cupmouse.minecraft.CMcCore;
import net.cupmouse.minecraft.game.CMcGamePlugin;
import net.cupmouse.minecraft.game.manager.GameException;
import net.cupmouse.minecraft.game.manager.GameRoomState;
import net.cupmouse.minecraft.worlds.WorldTagArea;
import net.cupmouse.minecraft.worlds.WorldTagModule;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.boss.BossBarColors;
import org.spongepowered.api.boss.BossBarOverlays;
import org.spongepowered.api.boss.ServerBossBar;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.item.EnchantmentData;
import org.spongepowered.api.data.meta.ItemEnchantment;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.item.Enchantments;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.channel.MutableMessageChannel;
import org.spongepowered.api.text.channel.impl.SimpleMutableMessageChannel;
import org.spongepowered.api.text.chat.ChatType;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 試合の進行を担当する
 * ステージの状況とかそんなのは気にしない元でprepareを呼びなさい
 * 前の試合からの移動は行うが、次の試合への移動等も行わない。この試合が終わったままの状態で役目を終える
 */
public class SpleefMatch {

    private static final int ITEM_COOLDOWN_TIME = 10;
    private final Object playersLocker = new Object();
    private final Random rand;
    private final SpleefRoom room;
    public final SpleefRoomMessageChannel messageChannel;

    private SpleefClockManager clock;
    private Task playerTrackerTask;
    private GameRoomState state = GameRoomState.CLOSED;
    // 切断されたらtryToQuitが呼ばれるのでこのリスト内にはオンラインのしかも試合に参加中のプレイヤーしか入ってない
    private Map<Integer, SpleefPlayer> players;
    private Map<UUID, SpleefPlayer> playerMap;
    private Set<UUID> arrows;
    private ServerBossBar bossBar;
    private SpleefItem item;
    private int nextItemSpawnTime;

    SpleefMatch(SpleefRoom room) {
        this.room = room;
        this.rand = new Random();
        this.clock = new SpleefClockManager(this);
        this.messageChannel = new SpleefRoomMessageChannel();
    }

    public void init() {
        this.players = new HashMap<>();
        this.playerMap = new HashMap<>();
        this.arrows = new HashSet<>();

        // 上部にバーを表示する
        initBossBar();
        // プレイヤー待ち状態にする
        waitPlayers();
    }

    /**
     * パラメータに入れたマッチはここで勝手にクローズされます
     * @param previous
     */
    public void init(SpleefMatch previous) throws GameException {
        if (previous.state != GameRoomState.FINISHED) {
            throw new IllegalStateException("試合中のマッチを元にしようとしました");
        }

        // ここで元になったマッチをクローズ
        // プレイヤーを引き継ぐ
        Map<Integer, SpleefPlayer> previousPlayers = previous.close(false);

        // プレイヤーリストを初期化
        this.players = new HashMap<>();
        this.playerMap = new HashMap<>();
        this.arrows = new HashSet<>();

        initBossBar();

        // 引き継いだプレイヤーで試合が進行できるか確認しできるのなら即時に実行
        if (previousPlayers.size() == room.getStage().getSpawnRocations().size()) {
            // 最大人数だけちょうどいるのですぐにready、移動もしてもらえる
            ready();
        } else {
            // 新しい試合の待機地点に移動させる
            for (SpleefPlayer previousPlayer : previousPlayers.values()) {
                Player player = Sponge.getServer().getPlayer(previousPlayer.playerUUID).get();
                playerJoined(player);
                room.getStage().getWaitingSpawnRocation().teleportHere(player);
            }

            if (players.size() >= room.getStage().getOptions().getMinimumPlayerCount()) {
                // 最少人数以上いるのでwaitMorePlayersでさらなる参加待ち
                waitMorePlayers();
            } else {
                // それ以外は最少人数に達していないのでプレイヤーの参加待ち
                waitPlayers();
            }
        }
    }

    private void initBossBar() {
        this.bossBar = ServerBossBar.builder().name(Text.of(TextColors.AQUA, "[Sp]leef"))
                .color(BossBarColors.BLUE)
                .overlay(BossBarOverlays.PROGRESS)
                .percent(1f)
                .build();
    }

    public SpleefRoom getRoom() {
        return room;
    }

    public GameRoomState getState() {
        return state;
    }

    Optional<SpleefItem> getItem() {
        return Optional.ofNullable(item);
    }

    int getNextItemSpawnTime() {
        return nextItemSpawnTime;
    }

    ServerBossBar getBossBar() {
        return bossBar;
    }

    /**
     * 変更不可
     * @return
     */
    public Collection<SpleefPlayer> getPlayers() {
        return Collections.unmodifiableCollection(players.values());
    }

    public SpleefPlayer getSpleefPlayer(UUID uuid) {
        return playerMap.get(uuid);
    }

    public boolean isPlaying(Player player) {
        return playerMap.containsKey(player.getUniqueId());
    }

    public void tryToJoin(Player player) throws GameException {
        if (state != GameRoomState.WAITING_PLAYERS && state != GameRoomState.FINISHED) {
            // プレイヤーが増えるのを待っているかゲーム終了後でないと参加できない
            throw new GameException(Text.of(TextColors.RED, "✗試合中のため参加できません"));
        }

        // ここまでくれば参加できる試合の状態
        // だけど、部屋の人数が最大なら蹴らなければならない
        if (players.size() == room.getStage().getSpawnRocations().size()) {
            throw new GameException(Text.of(TextColors.RED, "✗部屋に空きがありません"));
        }

        // ここまでくれば試合としては間違いなく参加できる

        // データ登録が行われる
        playerJoined(player);

        // 全員にプレイヤーが入室したことを通知
        messageChannel.send(
                Text.of(TextColors.AQUA, String.format("%sが入室しました(%d/%d-%d)",
                        player.getName(),
                        players.size(),
                        room.getStage().getOptions().getMinimumPlayerCount(),
                        room.getStage().getSpawnRocations().size())
                ), ChatTypes.SYSTEM);

        // データ登録されたので、このプレイヤーが参加したことで試合の進行が変わるかどうかチェックし
        // するならば即時にやる
        if (state == GameRoomState.WAITING_PLAYERS) {
            // 試合がまだ始まっていないしプレイヤー募集中のときしか影響がない、終わった試合にプレイヤーが加わっても
            // 意味ないから

            if (players.size() == room.getStage().getSpawnRocations().size()) {
                // 用意されているスポーンの数とプレイヤーが同じということは、これ以上プレイヤーは試合に参加できない
                // なので瞬時に新規参加が不可能なreadyモードになる
                ready();

                // このときは待機地点に送ってほしくない
                return;
            } else if (players.size() >= room.getStage().getOptions().getMinimumPlayerCount()) {
                // 最低人数以上いるのでとりあえずゲームが開催できる。更にプレイヤーが入るかもしれないので一応待つ
                // waitMorePlayersモードになる
                waitMorePlayers();
            }
        }
        // それ以外は試合の進行に変化なし
        // プレイヤー次の試合までの待機場所に移動する TODO 多分変える
        if (!room.getStage().getWaitingSpawnRocation().teleportHere(player)) {
            throw new GameException(Text.of(TextColors.RED, "✗移動できませんでした"));
        }
    }

    private void playerJoined(Player player) throws GameException {
        // ここで実際にプレイヤーの参加を認めてデータ登録する
        // スポーンリストとプレイヤーリストのkey番号を共有して一人一つのスポーン位置を確保する
        // スポーンリストはシャッフルされてもいいので、毎回同じにすることもできるししないこともできる
        synchronized (playersLocker) {
            int spawnIndex = getAvailableSpawnIndex();
            SpleefPlayer spleefPlayer = new SpleefPlayer(player.getUniqueId(), spawnIndex);
            this.players.put(spawnIndex, spleefPlayer);
            this.playerMap.put(player.getUniqueId(), spleefPlayer);
        }

        // TODO メッセージチャンネルをセットするが、解除を忘れないように
        player.setMessageChannel(messageChannel);

        // サバイバルに変更
        player.offer(Keys.GAME_MODE, GameModes.SURVIVAL);

        // インベントリ全削除
        player.getInventory().clear();

        this.bossBar.addPlayer(player);
    }

    private int getAvailableSpawnIndex() {
        for (int i = 0; i < room.getStage().getSpawnRocations().size(); i++) {
            if (!players.containsKey(i)) {
                // ここ開いてるよ！
                return i;
            }
        }

        // この関数を呼ぶ時点で空きがあるはずなので起きないはず
        return -1;
    }

    public void tryToQuit(Player player) throws GameException {
        // 抜けるのはいつでもOK

        synchronized (playersLocker) {
            // プレイヤーリストから削除する

            SpleefPlayer removed = playerMap.remove(player.getUniqueId());
            if (removed == null) {
                // 削除されなかった==そもそも参加していない
                throw new GameException(Text.of(TextColors.RED, "✗参加していません"));
            }
            // キャッシュの方から削除したので実際のプレイヤーリストから消す
            this.players.remove(removed.spawnLocationNumber);
        }

        playerQuit(player, true);

        // プレイヤーが退室したことを部屋の全員に通知
        messageChannel.send(
                Text.of(TextColors.GRAY,
                        String.format("%sが退出しました(%d/%d-%d)",
                                player.getName(),
                                players.size(),
                                room.getStage().getOptions().getMinimumPlayerCount(),
                                room.getStage().getSpawnRocations().size())
                ), ChatTypes.SYSTEM);

        // いつ抜けてもOKだが、残された人間は困るかも
        // ゲームの進行が変わってしまうかチェックする
        if (room.getStage().getOptions().getMinimumPlayerCount() - players.size() == 1) {
            // ちょうど最低人数より少なくなってしまったので試合が続行できない
            // 現在の試合の状態によって対応が異なる

            switch (state) {
                case WAITING_PLAYERS:
                    waitPlayers();
                    break;
                case FINISHED:
                    // このときは後ほどチェックされてゲーム進行が止まるので別に何もしなくて良い
                    break;
                case READY:
                    // ラッキー。まだ開始してないのでなかったコトにしてまた最低人数以上になるまでプレイヤー募集する
                    waitPlayers();

                    for (SpleefPlayer spleefPlayer : players.values()) {
                        room.getStage().getWaitingSpawnRocation()
                                .teleportHere(Sponge.getServer().getPlayer(spleefPlayer.playerUUID).get());
                    }
                    break;
                case IN_PROGRESS:
                    // これは迷惑。試合は止まってなかったことになる。かわいそう
                    interrupt();
                    break;
                case CLOSED:
                    // TODO
                    break;
                case PREPARING:
                case PREPARED:
                    // SpleefではこのときにtryToQuitは実行されないはず
                    break;
                default:
                    // ありえない！！！
                    CMcCore.getLogger().error("Spleef不明なステート: {}", state.toString());
                    break;
            }
        }
        // それ以外は試合の進行に変化なし
    }

    private void playerQuit(Player player, boolean teleport) {
        // TODO ここのやつ新しいplayerJoinでもう一度やることになるけど
        player.getInventory().clear();
        player.setMessageChannel(Sponge.getServer().getBroadcastChannel());
        player.offer(Keys.GAME_MODE, GameModes.SURVIVAL);
        this.bossBar.removePlayer(player);

        if (teleport) {
            // ロビーに移動
            WorldTagModule.getTaggedWorld(CMcGamePlugin.WORLD_TAG_LOBBY).ifPresent(player::transferToWorld);
        }
    }

    public int countPlayersLiving() {
        int count = 0;
        for (SpleefPlayer spleefPlayer : players.values()) {
            if (!spleefPlayer.dead) {
                count++;
            }
        }

        return count;
    }

    public void playerDied(Player player) {
        CMcCore.getLogger().debug("playerDied");
        // 指定したプレイヤーが死ぬ
        playerMap.get(player.getUniqueId()).dead = true;
        messageChannel.send(Text.of(TextColors.GRAY,
                String.format("%sが落ちました！さよーならー (%d/%d)",
                        player.getName(),
                        countPlayersLiving(),
                        players.size())));

        if (players.size() - countPlayersLiving() == 1) {
            // 最後の一人が残ったら終了
            finish();
        } else {
            // それ以外なら死んだプレイヤーをスペクテーターモードにする
            player.offer(Keys.GAME_MODE, GameModes.SPECTATOR);

            // 残り二人なら弓矢を支給
            if (countPlayersLiving() == 2) {
                players.values().stream().filter(spleefPlayer -> !spleefPlayer.dead).forEach(spleefPlayer -> {
                    Sponge.getServer().getPlayer(spleefPlayer.playerUUID).ifPresent(playerLiving -> {
                        final ItemStack bow = ItemStack.builder()
                                .itemType(ItemTypes.BOW)
                                .quantity(1)
                                .build();

                        bow.offer(Keys.UNBREAKABLE, true);
                        final EnchantmentData enchantmentData = bow.getOrCreate(EnchantmentData.class).get();
                        enchantmentData.enchantments().add(new ItemEnchantment(Enchantments.INFINITY, 1));
                        bow.offer(enchantmentData);

                        playerLiving.getInventory().offer(bow);
                        playerLiving.getInventory().offer(ItemStack.of(ItemTypes.ARROW, 1));
                    });
                });
            }
        }
    }

    public void waitPlayers() {
        this.state = GameRoomState.WAITING_PLAYERS;

        this.bossBar.setName(Text.of("プレイヤーを待っています"));
        this.bossBar.setColor(BossBarColors.YELLOW);
        this.bossBar.setPercent(1f);

        this.clock.cancel();
    }

    public void waitMorePlayers() {
        this.state = GameRoomState.WAITING_PLAYERS;

        this.clock.setClock(new SpleefClockWaitCountdown());
        this.clock.start();
    }

    public void ready() {
        // プレイヤーをステージに移動させ、武器を持たせる
        for (SpleefPlayer spleefPlayer : players.values()) {
            Player player = Sponge.getServer().getPlayer(spleefPlayer.playerUUID).get();
            // 移動
            this.room.getStage().getSpawnRocations().get(spleefPlayer.spawnLocationNumber).teleportHere(player);
            // インベントリを削除
            player.getInventory().clear();
            // シャベルを与える
            player.getInventory().offer(ItemStack.of(ItemTypes.IRON_SHOVEL, 1));
        }

        // 状態を変える
        // この後にテレポートすると、Ready状態で移動できなくなり、元いた場所でろテーションだけ適用され
        // 大変なことになるので注意
        this.state = GameRoomState.READY;

        this.clock.setClock(new SpleefClockReadyCountdown());
        this.clock.start();
    }

    public void start() {
        this.state = GameRoomState.IN_PROGRESS;

        this.messageChannel.send(Text.of(TextColors.GOLD, "試合開始！"));

        // 試合開始！
        this.playerTrackerTask = Task.builder()
                .name("プレイヤートラッカータスク")
                .intervalTicks(1)
                .execute(new PlayerTracker())
                .submit(CMcCore.getPlugin());

        int gameTime = room.getStage().getOptions().getGameTime();
        this.clock.setClock(new SpleefClockGame(gameTime));
        this.clock.start();
        // 初回のアイテム発生時刻を決定する
        decideNextItemSpawnTime(gameTime, false);
    }

    /**
     * 試合が正常終了した。結果を表示し、次の試合の試行までのカウントダウンを始める
     */
    public void finish() {
        // プレイヤートラッカーを停止
        this.playerTrackerTask.cancel();

        // 残っているアイテムを削除する
        if (item != null) {
            this.item.clear();
        }

        removeArrows();

        this.state = GameRoomState.FINISHED;
        this.messageChannel.send(Text.of(TextColors.AQUA, TextStyles.BOLD, "ゲーム終了！"), ChatTypes.SYSTEM);

        // 全員待機場所に移動する
        for (SpleefPlayer spleefPlayer : players.values()) {
            this.room.getStage().getWaitingSpawnRocation()
                    .teleportHere(Sponge.getServer().getPlayer(spleefPlayer.playerUUID).get());
        }

        showResult();

        this.clock.setClock(new SpleefClockResult());
        this.clock.start();
    }

    /**
     * カウントダウンせず即時に次の試合の開始を試行する
     */
    public void interrupt() {
        // プレイヤートラッカーを停止
        this.playerTrackerTask.cancel();

        // 残っているアイテムを削除する
        if (item != null) {
            this.item.clear();
        }

        removeArrows();

        this.state = GameRoomState.FINISHED;
        this.messageChannel.send(Text.of(TextColors.AQUA, TextStyles.BOLD, "ゲーム終了！"), ChatTypes.SYSTEM);
        this.room.nextMatch();
    }

    private void removeArrows() {
        // 矢を削除する
        final Optional<World> worldOptional = WorldTagModule.getTaggedWorld(SpleefManager.WORLD_TAG_SPLEEF);

        if (worldOptional.isPresent()) {
            final World world = worldOptional.get();

            for (UUID arrow : this.arrows) {
                world.getEntity(arrow).ifPresent(Entity::remove);
            }

        } else {
            CMcCore.getLogger().error("SPLEEFワールドが見つからない、矢を削除できない");
        }
    }

    void arrowShot(UUID arrowUUID) {
        this.arrows.add(arrowUUID);
    }

    void arrowRemoved(UUID arrowUUID) {
        this.arrows.remove(arrowUUID);
    }

    /**
     * この試合をもう使えなくする。移動などは発生しない
     */
    public Map<Integer, SpleefPlayer> close(boolean teleport) {
        if (state == GameRoomState.IN_PROGRESS) {
            throw new IllegalStateException();
        }

        this.state = GameRoomState.CLOSED;

        Map<Integer, SpleefPlayer> players = this.players;

        synchronized (playersLocker) {
            this.players = null;
            this.playerMap = null;
        }

        this.arrows = null;

        this.clock.cancel();
        this.clock = null;

        // 退出
        for (SpleefPlayer spleefPlayer : players.values()) {
            Sponge.getServer().getPlayer(spleefPlayer.playerUUID).ifPresent(player -> playerQuit(player, teleport));
        }

        return players;
    }

    private void showResult() {
        // TODO
        int aliveCount = 0;
        SpleefPlayer winner = null;
        for (SpleefPlayer spleefPlayer : players.values()) {
            if (!spleefPlayer.dead) {
                aliveCount++;
                winner = spleefPlayer;
            }
        }

        if (aliveCount == 1) {
            // ただ一人だけ残っているということはその人が優勝!

            String winnerName = Sponge.getServer().getPlayer(winner.playerUUID)
                    .map(User::getName).orElse(winner.playerUUID.toString());

            this.messageChannel.send(Text.of(TextColors.GOLD, String.format("%sの勝利！", winnerName)));
        } else {
            // 何人も残っているもしくは誰も残っていない

            this.messageChannel.send(Text.of(TextColors.GRAY, "引き分け"));
        }
    }

    private void decideNextItemSpawnTime(int ctickLeft, boolean cooldown) {
        // 次のアイテム発生時刻を決定する

        // TODO 最後の5秒とかでアイテム渡されても困るわ
        if (cooldown && ctickLeft <= ITEM_COOLDOWN_TIME) {
            // クールダウンの時間が残っていないので、次のアイテムはない
            this.nextItemSpawnTime = 0;
        } else {
            int working = ctickLeft;

            if (cooldown) {
                // クールダウンタイムは過ぎなければならない
                working -= ITEM_COOLDOWN_TIME + 1;
            }

            // 次のアイテム発生時間を決定する
            while (working >= 0) {
                int itemRandomConstant = room.getStage().getOptions().getItemRandomConstant();

                working -= rand.nextInt(itemRandomConstant);

                if (rand.nextInt(itemRandomConstant) == itemRandomConstant / 2) {
                    break;
                }
            }

            // whileで決定したworkingを設定する

            if (working < 0) {
                working = 0;
            }

            this.nextItemSpawnTime = working;
        }
    }

    void doItemTick(int ctickLeft) {
        if (ctickLeft == 0) {
            // ゲームが終了するときにアイテムが存在しているなら削除する
            if (item != null) {
                this.item.clear();
                this.item = null;
            }
        } else if (item == null) {
            // アイテムがないとき、アイテム発生時刻ならアイテムを発生させる
            // 同時にctickLeft==nextItemSpawnTime==0のときは実行されない

            if (ctickLeft == nextItemSpawnTime) {
                // アイテム発生

                // ランダムでアイテムを選定する
                final int itemType = rand.nextInt(6);
                switch (itemType % 2) {
                    case 0:
                        this.item = new SpleefItemTNT(this);
                        break;
                    case 1:
                        this.item = new SpleefItemTorch(this);
                        break;
                }

                this.messageChannel.send(Text.of(String.format("アイテム[%s]が発生しました!", item.getName())));
                this.item.init();
                this.item.doTick();
            }
        } else {
            // アイテムが存在するときはアイテム独自のtickを行う
            if (!item.doTick()) {
                // 返り値がfalseのとき今後呼ばれなくなる
                // アイテムを削除する
                this.item.clear();
                this.item = null;
                // 次のアイテム発生時刻を設定する
                decideNextItemSpawnTime(ctickLeft, true);

                if (nextItemSpawnTime == 0) {
                    // アクションバーの内容を消す。次のアイテムが発生しないことがわかる
                    for (SpleefPlayer spleefPlayer : players.values()) {
                        Sponge.getServer().getPlayer(spleefPlayer.playerUUID)
                                .ifPresent(player -> this.messageChannel.send(Text.of("", ChatTypes.ACTION_BAR)));
                    }
                }
            }
        }

        if (nextItemSpawnTime != 0) {
            if (ctickLeft <= nextItemSpawnTime) {
                // 現在アイテム発生中

                this.messageChannel.send(Text.of(
                        String.format("アイテム[%s]発生中! %d",
                                item == null ? "" : item.getName(),
                                nextItemSpawnTime - ctickLeft))
                        , ChatTypes.ACTION_BAR);
            } else if (item != null) {
                // 次のアイテムが有る！のでいつ起きるのかわかるようにする

                this.messageChannel.send(Text.of(
                        String.format("次のアイテム発生: %d秒後", ctickLeft - nextItemSpawnTime))
                        , ChatTypes.ACTION_BAR);
            }
        }
    }

    void clearItem() {
        this.item = null;
    }

    public class SpleefRoomMessageChannel implements MessageChannel {

        @Override
        public void send(Text original) {
            // ここでエラーが出るとサーバーがクラッシュするので注意
            synchronized (this) {
                if (players == null) {
                    return;
                }

                for (SpleefPlayer spleefPlayer : players.values()) {
                    Optional<Player> playerOptional = Sponge.getServer().getPlayer(spleefPlayer.playerUUID);

                    if (playerOptional.isPresent()) {
                        Player player = playerOptional.get();
                        player.sendMessage(transformMessage(null, player, original, null).get());
                    }
                }
            }
        }

        @Override
        public void send(Text original, ChatType type) {
            synchronized (this) {
                if (players == null) {
                    return;
                }

                for (SpleefPlayer spleefPlayer : players.values()) {
                    Optional<Player> playerOptional = Sponge.getServer().getPlayer(spleefPlayer.playerUUID);

                    if (playerOptional.isPresent()) {
                        Player player = playerOptional.get();
                        player.sendMessage(type, transformMessage(null, player, original, type).get());
                    }
                }
            }
        }

        @Override
        public void send(@Nullable Object sender, Text original) {
            // TODO
            send(original);
        }

        @Override
        public void send(@Nullable Object sender, Text original, ChatType type) {
            // TODO
            send(original, type);
        }

        @Override
        public Optional<Text> transformMessage(@Nullable Object sender, MessageReceiver recipient, Text original, ChatType type) {
            // TODO
            return Optional.of(type != ChatTypes.ACTION_BAR ? Text.of(TextColors.AQUA, "[Sp] ", original) : original);
        }

        @Override
        public Collection<MessageReceiver> getMembers() {
            // ここで不変のCollectionを返す必要はない？
            synchronized (this) {
                if (players == null) {
                    return new ArrayList<>();
                } else {
                    return players.values().stream()
                            .map(spleefPlayer -> Sponge.getServer().getPlayer(spleefPlayer.playerUUID).get())
                            .collect(Collectors.toCollection(HashSet::new));
                }
            }
        }

        @Override
        public MutableMessageChannel asMutable() {
            return new SimpleMutableMessageChannel(getMembers());
        }
    }

    private class PlayerTracker implements Consumer<Task> {
        @Override
        public void accept(Task task) {
            final WorldTagArea fightingArea = room.getStage().getFightingArea();

            for (SpleefPlayer spleefPlayer : players.values()) {
                final Optional<Player> playerOptional = Sponge.getServer().getPlayer(spleefPlayer.playerUUID);

                if (!playerOptional.isPresent()) {
                    continue;
                }

                final Player player = playerOptional.get();

                // プレイヤーが部屋でプレー中のときはfightingAreaから出ると落ちた判定とし、負け確定
                final Location<World> playerLocation = player.getLocation();

                if (!getSpleefPlayer(player.getUniqueId()).dead && !fightingArea.isInArea(playerLocation)) {
                    // 落ちた！
                    playerDied(player);

                    // プレイヤーが死んでゲームが終了していたら終わりにする
                    if (state != GameRoomState.IN_PROGRESS) {
                        task.cancel();
                        return;
                    }
                }
            }
        }
    }
}
