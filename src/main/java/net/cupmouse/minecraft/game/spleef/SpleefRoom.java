package net.cupmouse.minecraft.game.spleef;

import com.flowpowered.math.vector.Vector3i;
import net.cupmouse.minecraft.CMcCore;
import net.cupmouse.minecraft.game.CMcGamePlugin;
import net.cupmouse.minecraft.game.manager.GameException;
import net.cupmouse.minecraft.game.manager.GameRoom;
import net.cupmouse.minecraft.game.manager.GameRoomState;
import net.cupmouse.minecraft.worlds.WorldTagModule;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.World;

import java.util.Map;
import java.util.Optional;

/**
 * ****Outdated***
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

    Optional<SpleefMatch> getMatch() {
        return Optional.ofNullable(match);
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
        // TODO ロビーに移動
    }

    public void nextMatch() {
        CMcCore.getLogger().debug("nextMatch");
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
            world.setBlock(blockPos, stage.getGroundSample(), BlockChangeFlag.NONE);
        }

        // スポーン位置をシャフルする
        stage.shuffleSpawnRocations();
    }

    /**
     * ルームを入室不可にする
     */
    public void closeRoom() {
        CMcCore.getLogger().debug("closeRoom");

        if (match != null) {
            Map<Integer, SpleefPlayer> players = this.match.close(true);
        }
        // 何もしなくていい
    }

    public int getPlayerCount() {
        // TODO
        return match == null ? 0 : match.getPlayers().size();
    }
}
