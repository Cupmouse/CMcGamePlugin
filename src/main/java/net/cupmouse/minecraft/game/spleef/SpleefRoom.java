package net.cupmouse.minecraft.game.spleef;

import net.cupmouse.minecraft.game.manager.GameRoom;
import net.cupmouse.minecraft.game.manager.GameRoomState;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.channel.MutableMessageChannel;
import org.spongepowered.api.text.channel.impl.SimpleMutableMessageChannel;
import org.spongepowered.api.text.chat.ChatType;
import org.spongepowered.api.text.format.TextColors;

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

    public final int roomNumber;
    public final SpleefStageSettings stageSettings;
    public final SpleefRoomMessageChannel messageChannel;

    private Map<Integer, SpleefPlayer> players = new HashMap<>();
    private SpleefClockManager clock;
    private GameRoomState state;

    public SpleefRoom(int roomNumber, SpleefStageSettings stageSettings) {
        this.roomNumber = roomNumber;
        this.stageSettings = stageSettings;
        this.messageChannel = new SpleefRoomMessageChannel();

        this.clock = new SpleefClockManager(this);
        this.state = GameRoomState.WAITING_PLAYERS;
    }

    @Override
    public GameRoomState getState() {
        return state;
    }

    void setState(GameRoomState state) {
        this.state = state;
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

    // 外部向け処理

    /**
     * ゲームの開催を試みる
     *
     * @return
     */
    public boolean tryHoldGame() {
        if (state == GameRoomState.PREPARED) {
            if (players.size() >= stageSettings.spawnLocations.size()) {
                // すでに最高人数揃っているならすぐスタートカウントダウン
                ready();
            } else {
                // 揃っていないならプレイヤー待ちカウントダウン
                startCountdown();
            }
            this.clock.setClock(new SpleefClockWaitMorePlayer());
            this.clock.start();

            return true;
        }

        return false;
    }

    @Override
    public boolean tryJoinRoom(Player player) {
        if (state != GameRoomState.WAITING_PLAYERS) {
            // プレイヤーを募集していないので無理
            return false;
        }
        if (isPlayerPlaying(player)) {
            // すでにプレイ中
            return false;
        }
        if (stageSettings.spawnLocations.size() >= players.size()) {
            // ステージのプレイ最大人数を超えているので参加不可
            return false;
        }

        // プレイヤーを参加させる
        int spawnId = stageSettings.spawnLocations.size();
        players.put(spawnId, new SpleefPlayer(player, spawnId));
        // TODO メッセージチャンネルをセットするが、解除を忘れないように
        player.setMessageChannel(messageChannel);

        // プレイヤー人数が、ちょうど最低人数に達したら、プレイヤー待ちカウントダウンを開始する。
        if (players.size() == stageSettings.minimumPlayerCount) {
            startCountdown();
        }

        return false;
    }


    @Override
    public boolean tryLeaveRoom(Player player) {
        Optional<SpleefPlayer> optional = getSpleefPlayer(player);

        // プレイヤーはこの部屋にいて、削除されたか。
        if (optional.isPresent() && players.values().remove(optional.get())) {

            if (players.size() < 2) {
                // プレイヤーが足りないならゲームを終了する
                abortGame();
            }

            return true;
        } else {

            return false;
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
        messageChannel.send(Text.of(TextColors.AQUA, "ゲーム終了！"));

        // TODO プレイヤーの移動と結果発表
        clock.setClock(new SpleefClockPrepare());
        clock.start();
    }

    // 内部向け処理

    void prepareGame() {
        // TODO ステージの整備
    }

    /**
     * カウントダウンを開始する
     */
    void startCountdown() {
        this.state = GameRoomState.WAITING_PLAYERS;
        this.clock.setClock(new SpleefClockWaitMorePlayer());
        this.clock.start();
    }

    /**
     * ゲームスタートへカウントダウンを開始する
     */
    void ready() {
        this.state = GameRoomState.READY;
        this.clock.setClock(new SpleefClockCountdown());
        clock.start();
    }

    /**
     * 内部的にゲームを終了させる
     */
    void stopGame() {
        this.state = GameRoomState.FINISHED;
        this.clock.cancel();
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
