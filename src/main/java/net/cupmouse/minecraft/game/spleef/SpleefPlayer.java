package net.cupmouse.minecraft.game.spleef;

import org.spongepowered.api.entity.living.player.Player;

import java.util.UUID;

public final class SpleefPlayer {

    public final UUID playerUUID;
    public final int spawnLocationNumber;
    public boolean dead;

    public SpleefPlayer(UUID playerUUID, int spawnLocationNumber) {
        this.playerUUID = playerUUID;
        this.spawnLocationNumber = spawnLocationNumber;
    }
}
