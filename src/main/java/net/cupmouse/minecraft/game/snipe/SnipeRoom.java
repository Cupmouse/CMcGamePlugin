package net.cupmouse.minecraft.game.snipe;

import net.cupmouse.minecraft.game.manager.GameException;
import net.cupmouse.minecraft.game.manager.GameRoom;
import net.cupmouse.minecraft.game.manager.GameRoomState;
import net.cupmouse.minecraft.worlds.WorldTag;
import org.spongepowered.api.entity.living.player.Player;

public class SnipeRoom implements GameRoom {

    public static final WorldTag WORLD_TAG_SNIPE = WorldTag.byName("snipe");
    private SnipeMatch match;

    @Override
    public GameRoomState getState() {
        return match == null ? GameRoomState.CLOSED : match.getState();
    }

    @Override
    public boolean isPlayerPlaying(Player player) {
        return match.isPlayerPlaying(player);
    }

    @Override
    public void tryJoinRoom(Player player) throws GameException {

    }

    @Override
    public void tryLeaveRoom(Player player) throws GameException {

    }
}
