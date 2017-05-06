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

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public final class SpleefRoom implements GameRoom {

    private final SpleefStageSettings stageSettings;

    private Map<Integer, SpleefPlayer> players = new HashMap<>();
    private SpleefGameClock clock;

    public SpleefRoom(SpleefStageSettings stageSettings) {
        this.stageSettings = stageSettings;

        // tODO
        this.clock = new SpleefGameClock(this, 0);
    }

    @Override
    public GameRoomState getState() {
        return GameRoomState.CLOSED;
    }

    public Optional<SpleefGameClock> getClock() {
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

    @Override
    public boolean tryJoinRoom(Player player) {
        if (isPlayerPlaying(player)) {
            // すでにプレイ中
            return false;
        }
        if (stageSettings.getSpawnLocations().size() >= players.size()) {
            // ステージのプレイ最大人数を超えているので参加不可
            return false;
        }

        // プレイヤーを参加させる
        players.put(0, new SpleefPlayer(player, 0));


        return false;
    }


    @Override
    public boolean tryLeaveRoom(Player player) {
        Optional<SpleefPlayer> optional = getSpleefPlayer(player);

        // プレイヤーはこの部屋にいて、削除されたか。
        if (optional.isPresent() && players.values().remove(optional.get())) {

            if (players.size() < 2) {
                // プレイヤーが足りないならゲームを終了する
                resetClockAndFinishGame();
            }

            return true;
        } else {

            return false;
        }
    }

    /**
     * ゲームを正常に終了させる。
     */
    void finishGame() {

    }

    private void resetClockAndFinishGame() {
        clock.reset();
        finishGame();
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
            return Optional.of(original);
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
