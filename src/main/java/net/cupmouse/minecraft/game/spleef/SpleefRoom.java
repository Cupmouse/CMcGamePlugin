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

    private SpleefMatch match;

    SpleefRoom(SpleefStage stage) {
        this.stage = stage;
    }

    public SpleefStage getStage() {
        return stage;
    }

    @Override
    public GameRoomState getState() {
        return match == null ? GameRoomState.CLOSED : match.getState();
    }

    SpleefMatch getMatch() {
        return match;
    }

    @Override
    public boolean isPlayerPlaying(Player player) {
        return match != null && match.isPlaying(player);
    }

    // 外部向け処理

    @Override
    public void tryJoinRoom(Player player) throws GameException {
        CMcCore.getLogger().debug("tryJoinRoom");

        // 試合をしたことがないようなので作ってあげる
        if (match == null) {
            nextMatch();
        }

        if (CMcGamePlugin.getRoomPlayerJoin(player).isPresent()) {
            // すでに何処かでプレイ中
            throw new GameException(
                    Text.of(TextColors.RED, "✗あなたはすでに参加しています"));
        }

        match.tryToJoin(player);
    }

    /*
    ゲームをプレイ中の機能
     */
    @Override
    public void tryLeaveRoom(Player player) throws GameException {
        CMcCore.getLogger().debug("tryLeaveRoom");
        match.tryToQuit(player);
    }

    public void nextMatch() {
        // ステージの整備を行う
        prepareStage();

        // 新しい試合を作成する
        SpleefMatch nextMatch = new SpleefMatch(this);

        if (match != null) {
            // エラーが起きてもなかったことになるだけで済むのでnullにしておく
            SpleefMatch previous = match;
            this.match = null;
            // 前回の試合を引き継ぐ
            try {
                nextMatch.init(previous);
            } catch (GameException e) {
                e.printStackTrace();
                nextMatch = null;
            }
        } else {
            // 何も引き継がずに新しい試合を始める
            nextMatch.init();
        }

        this.match = nextMatch;
    }

    private void prepareStage() {
        // 床を埋め直す
        for (Vector3i blockPos : stage.getGroundArea().getEveryBlocks().blockLocs) {
            World world = WorldTagModule.getTaggedWorld(stage.getGroundArea().getEveryBlocks().worldTag).get();
            world.setBlock(blockPos, stage.getGroundSample(), Cause.source(CMcCore.getPluginContainer()).build());
        }
        // スポーン位置をシャフルする
        stage.shuffleSpawnRocations();
    }

    /**
     * ルームを入室不可にする
     */
    public void closeRoom() {
        CMcCore.getLogger().debug("closeRoom");

        Map<Integer, SpleefPlayer> players = this.match.close();

        for (SpleefPlayer spleefPlayer : players.values()) {
            // TODO ロビーに移動
            Sponge.getServer().getPlayer(spleefPlayer.playerUUID).get();
        }
    }

    public int getPlayerCount() {
        // TODO
        return match == null ? 0 : match.getPlayers().size();
    }
}
