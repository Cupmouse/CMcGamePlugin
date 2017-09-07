package net.cupmouse.minecraft.game.spleef;

import net.cupmouse.minecraft.CMcCore;
import net.cupmouse.minecraft.game.manager.GameException;
import net.cupmouse.minecraft.game.manager.GameRoomState;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.channel.MutableMessageChannel;
import org.spongepowered.api.text.channel.impl.SimpleMutableMessageChannel;
import org.spongepowered.api.text.chat.ChatType;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 試合の進行を担当する
 * ステージの状況とかそんなのは気にしない元でprepareを呼びなさい
 * 前の試合からの移動は行うが、次の試合への移動等も行わない。この試合が終わったままの状態で役目を終える
 */
public class SpleefMatch {

    private final Object playersLocker = new Object();
    private final SpleefRoom room;
    public final SpleefRoomMessageChannel messageChannel;

    private SpleefClockManager clock;
    private GameRoomState state;

    // 切断されたらtryToQuitが呼ばれるのでこのリスト内にはオンラインのしかも試合に参加中のプレイヤーしか入ってない
    private Map<Integer, SpleefPlayer> players;
    private Map<UUID, SpleefPlayer> playerMap;

    SpleefMatch(SpleefRoom room) {
        this.room = room;
        this.clock = new SpleefClockManager(this);
        this.messageChannel = new SpleefRoomMessageChannel();
    }

    public void init() {
        this.state = GameRoomState.WAITING_PLAYERS;
        players = new HashMap<>();
        playerMap = new HashMap<>();
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
        Map<Integer, SpleefPlayer> previousPlayers = previous.close();

        // プレイヤーリストを初期化
        players = new HashMap<>();
        playerMap = new HashMap<>();

        // 引き継いだプレイヤーで試合が進行できるか確認しできるのなら即時に実行
        if (previousPlayers.size() == room.stage.getSpawnRocations().size()) {
            // 最大人数だけちょうどいるのですぐにready、移動もしてもらえる
            ready();
        } else {
            // 新しい試合の待機地点に移動させる
            for (SpleefPlayer previousPlayer : previousPlayers.values()) {
                Player player = Sponge.getServer().getPlayer(previousPlayer.playerUUID).get();
                playerJoined(player);
                room.stage.getWaitingSpawnRocation().teleportHere(player);
            }

            if (players.size() >= room.stage.getOptions().getMinimumPlayerCount()) {
                // 最少人数以上いるのでwaitMorePlayersでさらなる参加待ち
                waitMorePlayers();
            }
            // それ以外は最少人数に達していないのでプレイヤーの参加待ち
        }

        this.state = GameRoomState.WAITING_PLAYERS;
    }

    public SpleefRoom getRoom() {
        return room;
    }

    public GameRoomState getState() {
        return state;
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
        if (players.size() == room.stage.getSpawnRocations().size()) {
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
                        room.stage.getOptions().getMinimumPlayerCount(),
                        room.stage.getSpawnRocations().size())
                ), ChatTypes.SYSTEM);

        // データ登録されたので、このプレイヤーが参加したことで試合の進行が変わるかどうかチェックし
        // するならば即時にやる
        if (state == GameRoomState.WAITING_PLAYERS) {
            // 試合がまだ始まっていないしプレイヤー募集中のときしか影響がない、終わった試合にプレイヤーが加わっても
            // 意味ないから

            if (players.size() == room.stage.getSpawnRocations().size()) {
                // 用意されているスポーンの数とプレイヤーが同じということは、これ以上プレイヤーは試合に参加できない
                // なので瞬時に新規参加が不可能なreadyモードになる
                ready();

                // このときは待機地点に送ってほしくない
                return;
            } else if (players.size() >= room.stage.getOptions().getMinimumPlayerCount()) {
                // 最低人数以上いるのでとりあえずゲームが開催できる。更にプレイヤーが入るかもしれないので一応待つ
                // waitMorePlayersモードになる
                waitMorePlayers();
            }
        }
        // それ以外は試合の進行に変化なし
        // プレイヤー次の試合までの待機場所に移動する TODO 多分変える
        if (!room.stage.getWaitingSpawnRocation().teleportHere(player)) {
            throw new GameException(Text.of(TextColors.RED, "✗移動できませんでした"));
        }
    }

    private void playerJoined(Player player) throws GameException {
        // ここで実際にプレイヤーの参加を認めてデータ登録する
        // スポーンリストとプレイヤーリストのkey番号を共有して一人一つのスポーン位置を確保する
        // スポーンリストはシャッフルされてもいいので、毎回同じにすることもできるししないこともできる
        synchronized (playersLocker) {
            SpleefPlayer spleefPlayer = new SpleefPlayer(player.getUniqueId(), players.size());
            this.players.put(players.size(), spleefPlayer);
            this.playerMap.put(player.getUniqueId(), spleefPlayer);
        }

        // TODO メッセージチャンネルをセットするが、解除を忘れないように
        player.setMessageChannel(messageChannel);

        // サバイバルに変更
        player.offer(Keys.GAME_MODE, GameModes.SURVIVAL);

        // インベントリ全削除
        player.getInventory().clear();
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

        playerQuit(player);

        // プレイヤーが退室したことを部屋の全員に通知
        messageChannel.send(
                Text.of(TextColors.GRAY,
                        String.format("%sが退出しました(%d/%d-%d)",
                                player.getName(),
                                players.size(),
                                room.stage.getOptions().getMinimumPlayerCount(),
                                room.stage.getSpawnRocations().size())
                ), ChatTypes.SYSTEM);

        // いつ抜けてもOKだが、残された人間は困るかも
        // ゲームの進行が変わってしまうかチェックする
        if (room.stage.getOptions().getMinimumPlayerCount() - players.size() == 1) {
            // ちょうど最低人数より少なくなってしまったので試合が続行できない
            // 現在の試合の状態によって対応が異なる

            switch (state) {
                case WAITING_PLAYERS:
                case FINISHED:
                    // このときは後ほどチェックされてゲーム進行が止まるので別に何もしなくて良い
                    break;
                case READY:
                    // ラッキー。まだ開始してないのでなかったコトにしてまた最低人数以上になるまでプレイヤー募集する
                    waitPlayers();

                    for (SpleefPlayer spleefPlayer : players.values()) {
                        room.stage.getWaitingSpawnRocation()
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
            }
        }
        // それ以外は試合の進行に変化なし
    }

    private void playerQuit(Player player) {
        player.getInventory().clear();
        player.setMessageChannel(Sponge.getServer().getBroadcastChannel());

        // TODO ロビーに移動
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
        }
    }

    public void waitPlayers() {
        this.state = GameRoomState.WAITING_PLAYERS;

        this.clock.cancel();
    }

    public void waitMorePlayers() {
        this.state = GameRoomState.WAITING_PLAYERS;

        this.clock.setClock(new SpleefClockWaitCountdown());
        this.clock.start();
    }

    public void ready() {
        // 状態を変える
        this.state = GameRoomState.READY;

        // プレイヤーをステージに移動させ、武器を持たせる
        for (SpleefPlayer spleefPlayer : players.values()) {
            Player player = Sponge.getServer().getPlayer(spleefPlayer.playerUUID).get();
            // 移動
            room.stage.getSpawnRocations().get(spleefPlayer.spawnLocationNumber).teleportHere(player);
            // インベントリを削除
            player.getInventory().clear();
            // シャベルを与える
            player.getInventory().offer(ItemStack.of(ItemTypes.IRON_SHOVEL, 1));
        }

        this.clock.setClock(new SpleefClockReadyCountdown());
        this.clock.start();
    }

    public void start() {
        this.state = GameRoomState.IN_PROGRESS;

        messageChannel.send(Text.of(TextColors.GOLD, "試合開始！"));

        // 試合開始！
        this.clock.setClock(new SpleefClockGame(room.stage.getOptions().getGameTime()));
        this.clock.start();
    }

    /**
     * 試合が正常終了した。結果を表示し、次の試合の試行までのカウントダウンを始める
     */
    public void finish() {
        this.state = GameRoomState.FINISHED;
        messageChannel.send(Text.of(TextColors.AQUA, TextStyles.BOLD, "ゲーム終了！"), ChatTypes.SYSTEM);

        // 全員待機場所に移動する
        for (SpleefPlayer spleefPlayer : players.values()) {
            room.stage.getWaitingSpawnRocation()
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
        this.state = GameRoomState.FINISHED;
        messageChannel.send(Text.of(TextColors.AQUA, TextStyles.BOLD, "ゲーム終了！"), ChatTypes.SYSTEM);
        room.nextMatch();
    }

    /**
     * この試合をもう使えなくする。移動などは発生しない
     */
    public Map<Integer, SpleefPlayer> close() {
        this.state = GameRoomState.CLOSED;

        Map<Integer, SpleefPlayer> players = this.players;

        synchronized (playersLocker) {
            this.players = null;
            this.playerMap = null;
        }
        this.clock.cancel();
        this.clock = null;

        return players;
    }

    private void showResult() {
        // TODO
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
                        player.sendMessage(transformMessage(null, player, original, type).get());
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
            return Optional.of(Text.of(TextColors.AQUA, "[Sp] ", original));
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
}
