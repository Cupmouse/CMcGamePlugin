package net.cupmouse.minecraft.game.creator;

import net.cupmouse.minecraft.CMcCore;
import net.cupmouse.minecraft.PluginModule;
import net.cupmouse.minecraft.game.CMcGamePlugin;
import net.cupmouse.minecraft.game.GameType;
import net.cupmouse.minecraft.game.creator.cmd.CCmdReloadConfig;
import net.cupmouse.minecraft.game.creator.cmd.CCmdSelection;
import net.cupmouse.minecraft.game.creator.cmd.CCmdTest;
import net.cupmouse.minecraft.game.creator.cmd.CCmdTools;
import net.cupmouse.minecraft.game.creator.cmd.area.CCmdArea;
import net.cupmouse.minecraft.game.creator.cmd.position.CCmdPosition;
import net.cupmouse.minecraft.game.creator.cmd.spleef.CCmdSpleef;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class CreatorModule implements PluginModule {

    public static final String TEXT_DEFAULT_DESCRIPTION = "This command is for limited people only." +
            " Usage can be found in the stage creator documentation.";

    private static Map<CommandSource, CreatorBank> creatorSessionMap = new HashMap<>();

    private static boolean creatorEnabled;

    @Override
    public void onInitializationProxy() throws Exception {
        creatorEnabled = CMcGamePlugin.getGameConfigNode().getNode("creator_enabled").getBoolean();

        if (!creatorEnabled) {
            return;
        }

        Sponge.getEventManager().registerListeners(CMcCore.getPlugin(), this);

        CommandManager commandManager = Sponge.getCommandManager();
        commandManager.register(CMcCore.getPlugin(), CommandSpec.builder()
                .description(Text.of(TEXT_DEFAULT_DESCRIPTION))
                .permission("cmc.game.creator")
                .child(CCmdReloadConfig.CALLABLE, "reloadconfig", "rc")
                .build(), "c");

        commandManager.register(CMcCore.getPlugin(), CCmdArea.CALLABLE, "area", "a");
        commandManager.register(CMcCore.getPlugin(), CCmdPosition.CALLABLE, "position", "pos", "p");
        commandManager.register(CMcCore.getPlugin(), CCmdTools.CALLABLE, "tool", "tools", "t");
        commandManager.register(CMcCore.getPlugin(), CCmdSelection.CALLABLE, "selection", "sel", "s");

        commandManager.register(CMcCore.getPlugin(), CCmdSpleef.CALLABLE,
                GameType.SPLEEF.aliases.stream().map(s -> "c" + s).toArray(String[]::new));

        commandManager.register(CMcCore.getPlugin(), CCmdTest.CALLABLE, "ctest");

        CMcCore.getLogger().warn("=========================================");
        CMcCore.getLogger().warn("?????????????????????????????????????????????????????????");
        CMcCore.getLogger().warn("=========================================");

    }

    public static boolean isCreatorEnabled() {
        return creatorEnabled;
    }

    public static CreatorBank getOrCreateBankOf(CommandSource commandSource) {
        CreatorBank sessionInfo = creatorSessionMap.get(commandSource);

        if (sessionInfo == null) {
            sessionInfo = new CreatorBank();
            CreatorModule.creatorSessionMap.put(commandSource, sessionInfo);
        }

        return sessionInfo;
    }

    @Listener
    public void onPlayerDisconnect(ClientConnectionEvent.Disconnect event) {
        // ??????????????????
        creatorSessionMap.remove(event.getTargetEntity());
    }

    @Listener
    public void onInteractBlock(InteractBlockEvent event, @First Player player) {
        // ???????????????????????????
        CreatorBank bank = getOrCreateBankOf(player);

        if (!bank.isSelectionEnabled) {
            // ?????????????????????????????????????????????
            return;
        }

        Optional<ItemStack> optional = player.getItemInHand(HandTypes.MAIN_HAND);

        if (!optional.isPresent()) {
            // ????????????????????????????????????
            return;
        }

        ItemStack itemStack = optional.get();

        // ?????????????????????????????????????????????/?????????????????????????????????(???????????????????????????)???????????????
        if (itemStack.getItem() == ItemTypes.MAGMA || itemStack.getItem() == ItemTypes.PACKED_ICE) {

            if (player.get(Keys.IS_SNEAKING).orElse(false)) {
                // ???????????????????????????????????????(??????????????????????????????????????????ON?????????????????????????????????????????????????????????)
                return;
            }

            // ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
            // ???????????????????????????????????????
            // ?????????null??????????????????????????????????????????isPresent???????????????
            Optional<Location<World>> targetBlockLocOpt = event.getTargetBlock().getLocation();

            if (!targetBlockLocOpt.isPresent()) {
                // ?????????????????????
                return;
            }

            Location<World> targetLoc = targetBlockLocOpt.get();

            // ?????????????????????????????????????????????????????????????????????
            Location<World> selectedLoc;

            if (event instanceof InteractBlockEvent.Primary) {
                // ???????????????????????????????????????????????????????????????????????????
                selectedLoc = targetLoc;

            } else if (event instanceof InteractBlockEvent.Secondary) {
                // ????????????????????????????????????????????????????????????????????????????????????????????????????????????
                // ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????

                selectedLoc = targetLoc.getRelative(event.getTargetSide());
            } else {
                // ????????????????????????????????????
                return;
            }

            // ???????????????????????????(????????????)????????????????????????????????????????????????????????????????????????????????????
            if (itemStack.getItem() == ItemTypes.MAGMA) {
                // ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                if (bank.getFirstLoc() != null) {
                    player.resetBlockChange(bank.getFirstLoc().getBlockPosition());
                }

                // ???????????????????????????
                bank.setFirstLoc(selectedLoc);

                player.sendMessage(Text.of(TextColors.AQUA, "???1?????????????????????????????????/",
                        selectedLoc.getBlockPosition().toString()));
            } else if (itemStack.getItem() == ItemTypes.PACKED_ICE) {
                if (bank.getSecondLoc() != null) {
                    player.resetBlockChange(bank.getSecondLoc().getBlockPosition());
                }

                // ???????????????????????????
                bank.setSecondLoc(selectedLoc);

                player.sendMessage(Text.of(TextColors.AQUA, "???2?????????????????????????????????/",
                        selectedLoc.getBlockPosition().toString()));
            }

            // ????????????????????????????????????????????????????????????
            Sponge.getScheduler().createTaskBuilder().delayTicks(1)
                    .execute(() -> {
                        player.sendBlockChange(selectedLoc.getBlockPosition(),
                                BlockState.builder().blockType(
                                        itemStack.getItem().getBlock().map(blockType -> {
                                            if (blockType == BlockTypes.MAGMA) {
                                                return BlockTypes.LAVA;
                                            } else {
                                                return BlockTypes.WATER;
                                            }
                                        }).orElse(BlockTypes.DEADBUSH)
                                ).build()
                        );
                    }).submit(CMcCore.getPlugin());

            event.setCancelled(true);

        } else if (itemStack.getItem() == ItemTypes.WOOL) {
            if (event instanceof InteractBlockEvent.Secondary) {

                if (bank.getFirstLoc() != null) {
                    player.resetBlockChange(bank.getFirstLoc().getBlockPosition());
                }
                if (bank.getSecondLoc() != null) {
                    player.resetBlockChange(bank.getSecondLoc().getBlockPosition());
                }

                event.setCancelled(true);
            }
        }
    }
}
