package net.cupmouse.minecraft.game.spleef;

import net.cupmouse.minecraft.worlds.WorldTagModule;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;
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

    // 20秒
    private int ticks = 21;

    public SpleefItemTNT(SpleefMatch match) {
        this.match = match;
    }

    @Override
    public void init() {
        // すべてのプレイヤーにTNTを与える

        for (SpleefPlayer spleefPlayer : match.getPlayers()) {
            Sponge.getServer().getPlayer(spleefPlayer.playerUUID)
                    .ifPresent(player -> player.getInventory().offer(ItemStack.of(ItemTypes.TNT, 3)));
        }
    }

    @Override
    public boolean doTick() {
        return --ticks > 0;
    }

    @Override
    public void clear() {
        final Optional<World> worldOptional = WorldTagModule.getTaggedWorld(SpleefManager.WORLD_TAG_SPLEEF);

        if (!worldOptional.isPresent()) {
            return;
        }

        final World world = worldOptional.get();

        // このTNTアイテムに関連するすべてのTNTを削除する
        this.primedTNTs.forEach(uuid -> world.getEntity(uuid).ifPresent(Entity::remove));

        // 全てのプレイヤーからTNTを没収
        for (SpleefPlayer spleefPlayer : match.getPlayers()) {
            Sponge.getServer().getPlayer(spleefPlayer.playerUUID).ifPresent(player -> player.getInventory().query(ItemTypes.TNT).poll());
        }
    }

    @Override
    public String getName() {
        return "TNT";
    }

    public Set<UUID> getPrimedTNTs() {
        return primedTNTs;
    }
}
