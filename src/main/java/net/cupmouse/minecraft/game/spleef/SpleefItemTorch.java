package net.cupmouse.minecraft.game.spleef;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

public class SpleefItemTorch implements SpleefItem {

    private final SpleefMatch match;
    // 20秒
    private int ticks = 21;
    private Set<Location<World>> torchLocations = new HashSet<>();

    public SpleefItemTorch(SpleefMatch match) {
        this.match = match;
    }

    @Override
    public void init() {
        // 全てのプレイヤーにトーチをあげる
        for (SpleefPlayer spleefPlayer : match.getPlayers()) {
            Optional<Player> playerOptional = Sponge.getServer().getPlayer(spleefPlayer.playerUUID);
            playerOptional.ifPresent(player -> player.getInventory().offer(ItemStack.of(ItemTypes.TORCH, 5)));
        }
    }

    @Override
    public boolean doTick() {
        Iterator<Location<World>> iterator = torchLocations.iterator();

        while (iterator.hasNext()) {
            Location<World> torchLocation = iterator.next();

            // 床の色を変えていく 黄色->赤->削除
            Location<World> root = torchLocation.getRelative(Direction.DOWN);

            Optional<DyeColor> dyeColorOptional = root.getBlock().get(Keys.DYE_COLOR);
            if (dyeColorOptional.isPresent()) {
                DyeColor dyeColor = dyeColorOptional.get();

                if (dyeColor == DyeColors.RED) {
                    // 赤色で削除
                    root.setBlockType(BlockTypes.AIR, BlockChangeFlag.NONE);
                    torchLocation.setBlockType(BlockTypes.AIR, BlockChangeFlag.NONE);
                    // セットからも削除
                    iterator.remove();

                } else if (dyeColor == DyeColors.YELLOW) {
                    // 赤色にする
                    root.setBlock(BlockState.builder().blockType(BlockTypes.GLASS)
                            .add(Keys.DYE_COLOR, DyeColors.RED).build(), BlockChangeFlag.NONE);
                }
            } else {
                // 黄色にする
                root.setBlock(BlockState.builder().blockType(BlockTypes.GLASS)
                        .add(Keys.DYE_COLOR, DyeColors.YELLOW).build(), BlockChangeFlag.NONE);
            }

        }

        return --ticks > 0;
    }

    @Override
    public void clear() {
        // トーチが残っているなら削除
        for (Location<World> torchLocation : torchLocations) {
            if (torchLocation.getBlockType() == BlockTypes.TORCH) {
                torchLocation.setBlockType(BlockTypes.AIR, BlockChangeFlag.NONE);
            }
        }
        for (SpleefPlayer spleefPlayer : match.getPlayers()) {
            Sponge.getServer().getPlayer(spleefPlayer.playerUUID)
                    .ifPresent(player -> player.getInventory().query(ItemTypes.TORCH).poll());
        }
    }

    @Override
    public String getName() {
        return "トーチ";
    }

    void torchPlaced(Location<World> location) {
        this.torchLocations.add(location);
    }

    void torchBroke(Location<World> location) {
        this.torchLocations.remove(location);
    }
}
