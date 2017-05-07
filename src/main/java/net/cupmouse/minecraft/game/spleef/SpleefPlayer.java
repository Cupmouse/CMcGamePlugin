package net.cupmouse.minecraft.game.spleef;

import org.spongepowered.api.entity.living.player.Player;

public final class SpleefPlayer {

    public final Player spongePlayer;
    public final int spawnLocationNumber;

    public SpleefPlayer(Player spongePlayer, int spawnLocationNumber) {
        this.spongePlayer = spongePlayer;
        this.spawnLocationNumber = spawnLocationNumber;
    }
}
