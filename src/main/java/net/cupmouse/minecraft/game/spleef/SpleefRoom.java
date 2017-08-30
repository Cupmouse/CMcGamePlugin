package net.cupmouse.minecraft.game.spleef;

import com.flowpowered.math.vector.Vector3i;
import net.cupmouse.minecraft.CMcCore;
import net.cupmouse.minecraft.game.CMcGamePlugin;
import net.cupmouse.minecraft.game.manager.GameException;
import net.cupmouse.minecraft.game.manager.GameRoom;
import net.cupmouse.minecraft.game.manager.GameRoomState;
import net.cupmouse.minecraft.worlds.WorldTagModule;
import net.cupmouse.minecraft.worlds.WorldTagRocation;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.event.cause.Cause;
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
import org.spongepowered.api.world.World;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Hold game - ゲーム自体のカウントダウンを始める
 * Start game - ゲーム自体を開始する。試合開始。プレー開始。
 * Stop game - 内部的にゲーム自体を終了させる。それ以外のことはしない。プレイヤーを移動もしない。
 * 　主に、下2つの処理のためにある。
 * Finish game - 結果が決まって終了する。Stopさせて、プレイヤーの移動をして、その後、結果発表。
 * Abort game - 結果が決まったかどうかにかかわらず、ゲームを終了させる。プレイヤーの移動をする。結果発表はしない。
 *
 * ↓hold
 * ↓countdown ←カウントダウン始まってる
 * ↓ready←もう参加できない
 * ↓start
 * ↓finish/abort
 */
public final class SpleefRoom implements GameRoom {

    public final SpleefStage stage;
    public final SpleefRoomMessageChannel messageChannel;

    private Map<Integer, SpleefPlayer> players = new HashMap<>();
    private SpleefClockManager clock;
    private GameRoomState state;

    SpleefRoom(SpleefStage stage) {
        this.stage = stage;
        this.messageChannel = new SpleefRoomMessageChannel();

        this.clock = new SpleefClockManager(this);
        this.state = GameRoomState.WAITING_PLAYERS;
    }

    public SpleefStage getStage() {
        return stage;
    }

    @Override
    public GameRoomState getState() {
        return state;
    }

    public Optional<SpleefClockManager> getClock() {
        return Optional.ofNullable(clock);
    }

    public Optional<SpleefPlayer> getSpleefPlayer(Player player) {
        for (SpleefPlayer spleefPlayer : players.values()) {
            if (spleefPlayer.playerUUID.equals(player.getUniqueId())) {
                return Optional.of(spleefPlayer);
            }
        }

        return Optional.empty();
    }

    @Override
    public boolean isPlayerPlaying(Player player) {
        for (SpleefPlayer spleefPlayer : players.values()) {
            if (spleefPlayer.playerUUID.equals(player.getUniqueId())) {
                return true;
            }
        }

        return false;
    }

    public Collection<SpleefPlayer> getSpleefPlayerPlaying() {
        return players.values();
    }

    // 外部向け処理

    @Override
    public void tryJoinRoom(Player player) throws GameException {
        CMcCore.getLogger().debug("tryJoinRoom");
        if (state != GameRoomState.WAITING_PLAYERS) {
            // プレイヤーを募集していないので無理
            throw new GameException(
                    Text.of("✗ゲームがすでに開始しています。終了するまで入室できません"));
        }
        if (CMcGamePlugin.getRoomPlayerJoin(player).isPresent()) {
            // すでに何処かでプレイ中
            throw new GameException(
                    Text.of(TextColors.RED, "✗あなたはすでに参加しています"));
        }
        if (stage.getSpawnRocations().size() <= players.size()) {
            // ステージのプレイ最大人数を超えているので参加不可
            throw new GameException(
                    Text.of(TextColors.RED,
                            String.format("✗部屋に入れる人数は%d人までです", stage.getSpawnRocations().size())));
        }

        // プレイヤーを参加させる
        int spawnId = players.size();
        // テレポートできるか確かめてから
        WorldTagRocation spawnRoc = stage.getWaitingSpawnRocation();
        if (!spawnRoc.teleportHere(player)) {
            throw new GameException(
                    Text.of(TextColors.RED, "✗テレポートできませんでした。参加できませんでした。"));
        }

        players.put(spawnId, new SpleefPlayer(player.getUniqueId(), spawnId));

        // TODO メッセージチャンネルをセットするが、解除を忘れないように
        player.setMessageChannel(messageChannel);
        // サバイバルに変更
        player.offer(Keys.GAME_MODE, GameModes.SURVIVAL);
        // インベントリ全削除
        player.getInventory().clear();

        // 全員にプレイヤーが入室したことを通知
        messageChannel.send(
                Text.of(TextColors.AQUA, String.format("%sが入室しました(%d/%d-%d)",
                        player.getName(),
                        players.size(),
                        stage.getOptions().getMinimumPlayerCount(),
                        stage.getSpawnRocations().size())
                ), ChatTypes.SYSTEM);

        // プレイヤー人数が、ちょうど最低人数に達したら、プレイヤー待ちカウントダウンを開始する。
        if (players.size() == stage.getOptions().getMinimumPlayerCount()) {
            startCountdown();
        } else if (players.size() == stage.getSpawnRocations().size()) {
            // プレイヤー人数が最高人数に達したら、プレイヤーは待たないですぐReadyカウントダウンする
            ready();
        }
    }

    /*
    ゲームをプレイ中の機能
     */

    @Override
    public void tryLeaveRoom(Player player) throws GameException {
        CMcCore.getLogger().debug("tryLeaveRoom");
        Optional<SpleefPlayer> optional = getSpleefPlayer(player);

        // プレイヤーはこの部屋にいて、削除されたか。
        if (optional.isPresent() && players.values().remove(optional.get())) {
            messageChannel.send(
                    Text.of(TextColors.GRAY,
                            String.format("%sが退出しました(%d/%d-%d)",
                                    player.getName(),
                                    players.size(),
                                    stage.getOptions().getMinimumPlayerCount(),
                                    stage.getSpawnRocations().size())
                    ), ChatTypes.SYSTEM);

            if (players.size() < stage.getOptions().getMinimumPlayerCount()) {
                // プレイヤーが足りないならゲームを終了する
                interruptGame();
            }
        } else {
            throw new GameException(Text.of(TextColors.RED, "✗部屋へ入室していません"));
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
        getSpleefPlayer(player).get().dead = true;
        messageChannel.send(Text.of(TextColors.GRAY,
                String.format("%sが落ちました！さよーならー (%d/%d)",
                        player.getName(), countPlayersLiving(), players.size())));

        if (players.size() - countPlayersLiving() == 1) {
            // 最後の一人が残ったら終了
            finishGame();
        } else {
            // それ以外なら死んだプレイヤーをスペクテーターモードにする
            player.offer(Keys.GAME_MODE, GameModes.SPECTATOR);
        }
    }


    /**
     * どのような状態でもゲームを終了させる。勝敗は決まらなかったことになる。
     * 次回は開催しない
     */
    public void abortGame() {
        // TODO closeRoomだけでいいよ
        CMcCore.getLogger().debug("abort");
        stopGame();
        // TODO プレイヤーの移動
    }

    /**
     * どのような状態でもゲームを終了させる、勝敗は決まらなかったことになる。
     * 次回が開催されるかどうかが即時に判定され、即時に開催される
     */
    public void interruptGame() {
        CMcCore.getLogger().debug("interruptGame");
        // ゲームを終了
        stopGame();

        // 全員移動
        teleportAllToWaitingSpawn();

        // 瞬時に次のゲームの準備
        prepareGame();

        // 次のゲームの開催を試行する
        tryHoldNextGame();
    }

    /**
     * ゲームを正常に終了させる。制限時間が過ぎたもしくは残り一人になったときに呼ぶ。勝敗もしくはドローが決まる。
     * 次回も続けて開催され、開催までのカウントダウンが始まる
     */
    public void finishGame() {
        CMcCore.getLogger().debug("finished");
        // ゲームを終了
        stopGame();

        // 全員移動
        teleportAllToWaitingSpawn();

        // 瞬時に次のゲームの準備
        prepareGame();

        // 次のゲームの開催試行までカウントダウンを開始する
        clock.setClock(new SpleefClockPrepare());
        clock.start();
    }

    private void teleportAllToWaitingSpawn() {
        for (SpleefPlayer spleefPlayer : players.values()) {
            // サバイバルモードにする
            Player player = Sponge.getServer().getPlayer(spleefPlayer.playerUUID).get();
            player.offer(Keys.GAME_MODE, GameModes.SURVIVAL);
            stage.getWaitingSpawnRocation().teleportHere(player);
        }
    }

    // 内部向け処理

    void prepareGame() {
        CMcCore.getLogger().debug("prepareGame");
        this.state = GameRoomState.PREPARING;
        // 床を埋め直す
        for (Vector3i blockPos : stage.getGroundArea().getEveryBlocks().blockLocs) {
            World world = WorldTagModule.getTaggedWorld(stage.getGroundArea().getEveryBlocks().worldTag).get();

            world.setBlock(blockPos, stage.getGroundSample(), Cause.source(CMcCore.getPluginContainer()).build());
        }
        // プレイヤーのスポーン位置をランダムに変更
        ArrayList<SpleefPlayer> newPlayerList = new ArrayList<>(players.values());
        Collections.shuffle(newPlayerList);

        this.players.clear();

        for (int i = 0; i < newPlayerList.size(); i++) {
            this.players.put(i, new SpleefPlayer(newPlayerList.get(i).playerUUID, i));
        }

        this.state = GameRoomState.PREPARED;
    }

    /**
     * ゲームの開催を試みる
     *
     * @return
     */
    boolean tryHoldNextGame() {
        CMcCore.getLogger().debug("tryHoldNextGame");
        if (state == GameRoomState.PREPARED) {
            if (players.size() >= stage.getSpawnRocations().size()) {
                // すでに最高人数揃っているならすぐスタートカウントダウン
                ready();
                return true;
            } else if (players.size() >= stage.getOptions().getMinimumPlayerCount()) {
                // でなくても、最低人数揃っているならプレイヤー待ちカウントダウン
                startCountdown();
                return true;
            } else {
                // そうでもないなら開催できないのでプレイヤー待ち！

                waitPlayers();
                return false;
            }
        }

        return false;
    }

    void waitPlayers() {
        CMcCore.getLogger().debug("waitPlayers");
        this.state = GameRoomState.WAITING_PLAYERS;
        this.clock.cancel();
    }

    /**
     * カウントダウンを開始する
     */
    void startCountdown() {
        CMcCore.getLogger().debug("startCountdown");
        this.state = GameRoomState.WAITING_PLAYERS;
        this.clock.setClock(new SpleefClockWaitCountdown());
        this.clock.start();
    }

    /**
     * ゲームスタートへカウントダウンを開始する
     */
    void ready() {
        CMcCore.getLogger().debug("ready");
        this.state = GameRoomState.READY;

        for (SpleefPlayer spleefPlayer : this.players.values()) {
            Player player = Sponge.getServer().getPlayer(spleefPlayer.playerUUID).get();
            // プレイヤーを各スポーン位置に移動する
            stage.getSpawnRocations().get(spleefPlayer.spawnLocationNumber).teleportHere(player);
            // プレイヤーに必要物資を配る
            player.getInventory().offer(ItemStack.of(ItemTypes.IRON_SHOVEL, 1));
        }

        this.clock.setClock(new SpleefClockReadyCountdown());
        this.clock.start();
    }

    void startGame() {
        CMcCore.getLogger().debug("startGame");
        this.state = GameRoomState.IN_PROGRESS;
        this.clock.setClock(new SpleefClockGame(stage.getOptions().getGameTime()));
        this.clock.start();
    }

    /**
     * 内部的にゲームを終了する
     */
    void stopGame() {
        CMcCore.getLogger().debug("stopGame");
        this.state = GameRoomState.FINISHED;
        this.clock.cancel();

        messageChannel.send(Text.of(TextColors.AQUA, TextStyles.BOLD, "ゲーム終了！"), ChatTypes.SYSTEM);
    }

    /**
     * ルームを入室不可にする
     */
    public void closeRoom() {
        CMcCore.getLogger().debug("closeRoom");
        switch (state) {
            case WAITING_PLAYERS:

                break;
            case READY:
            case IN_PROGRESS:
                // すでにゲームが始まっているときは、中止する
                abortGame();
                break;
            case FINISHED:
                // 終了していて結果発表中ならプレイヤーの移動

                break;
            default:
                // その他は何もしない
                break;
        }

        // もしかしたら動いているかもしれないので、時計を一応止めておく
        this.clock.cancel();
        // これをしないと、いつまでもルーム入室中になる
        this.players.clear();

        // TODO プレイヤーの移動
    }

    public class SpleefRoomMessageChannel implements MessageChannel {

        @Override
        public void send(Text original) {
            for (SpleefPlayer spleefPlayer : players.values()) {
                Player player = Sponge.getServer().getPlayer(spleefPlayer.playerUUID).get();
                player.sendMessage(transformMessage(null, player, original, null).get());
            }
        }

        @Override
        public void send(Text original, ChatType type) {
            for (SpleefPlayer spleefPlayer : players.values()) {
                Player player = Sponge.getServer().getPlayer(spleefPlayer.playerUUID).get();
                player.sendMessage(type, transformMessage(null, player, original, type).get());
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
            return Optional.of(Text.of(TextColors.AQUA, "[S] ", original));
        }

        @Override
        public Collection<MessageReceiver> getMembers() {
            // ここで不変のCollectionを返す必要はない？
            return players.values().stream()
                    .map(spleefPlayer -> Sponge.getServer().getPlayer(spleefPlayer.playerUUID).get())
                    .collect(Collectors.toCollection(HashSet::new));
        }

        @Override
        public MutableMessageChannel asMutable() {
            return new SimpleMutableMessageChannel(getMembers());
        }
    }
}
