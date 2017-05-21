package net.cupmouse.minecraft.game.manager;

import org.spongepowered.api.entity.living.player.Player;

public interface GameRoom {

    GameRoomState getState();

    boolean isPlayerPlaying(Player player);

    void tryJoinRoom(Player player) throws GameRoomException;

    /**
     * 与えられたプレイヤーがゲームに参加している場合は、ルームから退出させる。
     * 参加していない場合は、何もしない。
     *
     * @param player
     * @return 参加していた場合はtrue、参加していなかった場合はfalse
     */
    void tryLeaveRoom(Player player) throws GameRoomException;
}
