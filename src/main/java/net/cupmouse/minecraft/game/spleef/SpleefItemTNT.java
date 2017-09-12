package net.cupmouse.minecraft.game.spleef;

import net.cupmouse.minecraft.worlds.WorldTagModule;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.world.World;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class SpleefItemTNT implements SpleefItem {

    private final SpleefMatch match;
    private final Set<UUID> primedTNTs = new HashSet<>();

    private int ticks = 20;

    public SpleefItemTNT(SpleefMatch match) {
        this.match = match;
    }

    @Override
    public void init() {
        // すべてのプレイヤーにTNTを与える

        for (SpleefPlayer spleefPlayer : match.getPlayers()) {
            Optional<Player> playerOptional = Sponge.getServer().getPlayer(spleefPlayer.playerUUID);

            playerOptional.ifPresent(player -> player.getInventory().offer(ItemStack.of(ItemTypes.TNT, 3)));
        }
    }

    @Override
    public boolean doTick() {

        return false;
    }

    @Override
    public void clear() {
        Optional<World> worldOptional = WorldTagModule.getTaggedWorld(SpleefManager.WORLD_TAG_SPLEEF);

        if (!worldOptional.isPresent()) {
            return;
        }

        World world = worldOptional.get();

        // このTNTアイテムに関連するすべてのTNTを削除する
        this.primedTNTs.forEach(uuid -> world.getEntity(uuid).ifPresent(Entity::remove));
    }

    @Override
    public String getName() {
        return "TNT";
    }

    public Set<UUID> getPrimedTNTs() {
        return primedTNTs;
    }
}
