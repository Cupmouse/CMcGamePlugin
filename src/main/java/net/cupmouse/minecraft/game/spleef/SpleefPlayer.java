package net.cupmouse.minecraft.game.spleef;

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
