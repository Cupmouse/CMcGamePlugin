package net.cupmouse.minecraft.game.spleef;

import net.cupmouse.minecraft.game.manager.GameException;
import net.cupmouse.minecraft.game.manager.GameRoom;
import net.cupmouse.minecraft.game.manager.GameRoomState;
import net.cupmouse.minecraft.worlds.WorldTagRocation;
import org.spongepowered.api.entity.living.player.Player;
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

    @Override
    public GameRoomState getState() {
        return state;
    }

    public Optional<SpleefClockManager> getClock() {
        return Optional.ofNullable(clock);
    }

    public Optional<SpleefPlayer> getSpleefPlayer(Player player) {
        for (SpleefPlayer spleefPlayer : players.values()) {
            if (spleefPlayer.spongePlayer == player) {
                return Optional.of(spleefPlayer);
            }
        }

        return Optional.empty();
    }

    @Override
    public boolean isPlayerPlaying(Player player) {
        for (SpleefPlayer spleefPlayer : players.values()) {
            if (spleefPlayer.spongePlayer == player) {
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
        if (state != GameRoomState.WAITING_PLAYERS) {
            // プレイヤーを募集していないので無理
            throw new GameException(Text.of(""));
        }
        if (isPlayerPlaying(player)) {
            // すでにプレイ中
            throw new GameException(
                    Text.of(TextColors.RED, "✗あなたはすでに参加しています。"));
        }
        if (stage.getSpawnRocations().size() <= players.size()) {
            // ステージのプレイ最大人数を超えているので参加不可
            throw new GameException(
                    Text.of(TextColors.RED, "✗部屋の最大人数に達しています。参加できませんでした。"));
        }

        // プレイヤーを参加させる
        int spawnId = players.size();
        // テレポートできるか確かめてから
        WorldTagRocation spawnRoc = stage.getSpawnRocations().get(spawnId);
        if (!spawnRoc.teleportHere(player)) {
            throw new GameException(
                    Text.of(TextColors.RED, "✗テレポートできませんでした。参加できませんでした。"));
        }

        players.put(spawnId, new SpleefPlayer(player, spawnId));

        // TODO メッセージチャンネルをセットするが、解除を忘れないように
        player.setMessageChannel(messageChannel);

        // 全員にプレイヤーが入室したことを通知
        messageChannel.send(
                Text.of(TextColors.AQUA, player.getName(), "が入室しました(" + players.size() + "/"
                        + stage.getMinimumPlayerCount() + "-"
                        + stage.getSpawnRocations().size() + ")")
                , ChatTypes.SYSTEM);

        // プレイヤー人数が、ちょうど最低人数に達したら、プレイヤー待ちカウントダウンを開始する。
        if (players.size() == stage.getMinimumPlayerCount()) {
            startCountdown();
        }
    }


    @Override
    public void tryLeaveRoom(Player player) throws GameException {
        Optional<SpleefPlayer> optional = getSpleefPlayer(player);

        // プレイヤーはこの部屋にいて、削除されたか。
        if (optional.isPresent() && players.values().remove(optional.get())) {
            messageChannel.send(
                    Text.of(TextColors.GRAY, player.getName(), "が退出しました(" + players.size() + "/"
                            + stage.getMinimumPlayerCount() + "-"
                            + stage.getSpawnRocations().size() + ")")
                    , ChatTypes.SYSTEM);

            if (players.size() < 2) {
                // プレイヤーが足りないならゲームを終了する
                abortGame();
            }
        } else {
            throw new GameException(Text.of(TextColors.RED, "✗部屋へ入室していません。"));
        }
    }

    /**
     * どのような状態でもゲームを終了させる
     */
    public void abortGame() {
        stopGame();
        // プレイヤーの移動
    }

    /**
     * ゲームを正常に終了させる。
     */
    public void finishGame() {
        stopGame();

        // TODO プレイヤーの移動と結果発表
        clock.setClock(new SpleefClockPrepare());
        clock.start();
    }

    // 内部向け処理

    void prepareGame() {
        // TODO ステージの整備
    }

    /**
     * ゲームの開催を試みる
     *
     * @return
     */
    boolean tryHoldNextGame() {
        if (state == GameRoomState.PREPARED) {
            if (players.size() >= stage.getSpawnRocations().size()) {
                // すでに最高人数揃っているならすぐスタートカウントダウン
                ready();
                return true;
            } else if (players.size() >= stage.getMinimumPlayerCount()) {
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
        this.state = GameRoomState.WAITING_PLAYERS;
        this.clock.cancel();
    }

    /**
     * カウントダウンを開始する
     */
    void startCountdown() {
        this.state = GameRoomState.WAITING_PLAYERS;
        this.clock.setClock(new SpleefClockWaitCountdown());
        this.clock.start();
    }

    /**
     * ゲームスタートへカウントダウンを開始する
     */
    void ready() {
        this.state = GameRoomState.READY;

        // プレイヤーに必要物資を配る
        for (SpleefPlayer spleefPlayer : this.players.values()) {
            spleefPlayer.spongePlayer.getInventory().offer(ItemStack.of(ItemTypes.IRON_SHOVEL, 1));
        }

        this.clock.setClock(new SpleefClockReadyCountdown());
        this.clock.start();
    }

    void startGame() {
        this.state = GameRoomState.IN_PROGRESS;
        this.clock.setClock(new SpleefClockGame(stage.getOptions().getGameTime()));
        this.clock.start();
    }

    /**
     * 内部的にゲームを終了させる
     */
    void stopGame() {
        this.state = GameRoomState.FINISHED;
        this.clock.cancel();

        messageChannel.send(Text.of(TextColors.AQUA, TextStyles.BOLD, "ゲーム終了！"), ChatTypes.SYSTEM);
    }

    /**
     * ルームを入室不可にする
     */
    public void closeRoom() {
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
                Player spongePlayer = spleefPlayer.spongePlayer;
                spongePlayer.sendMessage(transformMessage(null, spongePlayer, original, null).get());
            }
        }

        @Override
        public void send(Text original, ChatType type) {
            for (SpleefPlayer spleefPlayer : players.values()) {
                Player spongePlayer = spleefPlayer.spongePlayer;
                spongePlayer.sendMessage(type, transformMessage(null, spongePlayer, original, type).get());
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
            return players.values().stream().map(spleefPlayer -> spleefPlayer.spongePlayer)
                    .collect(Collectors.toCollection(HashSet::new));
        }

        @Override
        public MutableMessageChannel asMutable() {
            return new SimpleMutableMessageChannel(getMembers());
        }
    }
}
