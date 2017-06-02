package net.cupmouse.minecraft.game.creator;

import io.netty.internal.tcnative.SessionTicketKey;
import net.cupmouse.minecraft.CMcCore;
import net.cupmouse.minecraft.PluginModule;
import net.cupmouse.minecraft.game.CMcGamePlugin;
import net.cupmouse.minecraft.game.GameType;
import net.cupmouse.minecraft.game.creator.cmd.CCmdReloadConfig;
import net.cupmouse.minecraft.game.creator.cmd.CCmdSelection;
import net.cupmouse.minecraft.game.creator.cmd.CCmdTools;
import net.cupmouse.minecraft.game.creator.cmd.area.CCmdArea;
import net.cupmouse.minecraft.game.creator.cmd.position.CCmdPosition;
import net.cupmouse.minecraft.game.creator.cmd.spleef.CCmdSpleef;
import net.cupmouse.minecraft.game.creator.cmd.spleef.CCmdSpleefStage;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
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
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class CreatorModule implements PluginModule {

    public static final String TEXT_DEFAULT_DESCRIPTION = "THIS IS FOR LIMITED PEOPLE ONLY. " +
            "IF YOU NEED HELP WITH THIS COMMAND, " +
            "THEN CHECK OUT OTHER PAPER FOR USAGE OF THE COMMAND, " +
            "OR JUST ASK SOMEONE WHO KNOWS ABOUT IT.";

    private static Map<CommandSource, CreatorSessionInfo> creatorSessionMap = new HashMap<>();

    private boolean creatorEnabled;

    @Override
    public void onInitializationProxy() throws Exception {
        this.creatorEnabled = CMcGamePlugin.getGameConfigNode().getNode("creator_enabled").getBoolean();

        if (!creatorEnabled) {
            return;
        }

        Sponge.getEventManager().registerListeners(CMcCore.getPlugin(), this);

        Sponge.getCommandManager().register(CMcCore.getPlugin(), CommandSpec.builder()
                .description(Text.of(TEXT_DEFAULT_DESCRIPTION))
                .permission("cmc.game.creator")
                .child(CCmdArea.CALLABLE, "area", "a")
                .child(CCmdPosition.CALLABLE, "position", "pos", "p")
                .child(CCmdTools.CALLABLE, "tool", "tools", "t")
                .child(CCmdSelection.CALLABLE, "selection", "sel", "s")
                .child(CCmdSpleef.CALLABLE, GameType.SPLEEF.aliases)
                .child(CCmdReloadConfig.CALLABLE, "reloadconfig", "rc")
                .build(), "gc");

        CMcCore.getLogger().warn("=========================================");
        CMcCore.getLogger().warn("クリエイターモードが有効化されました！");
        CMcCore.getLogger().warn("=========================================");

    }

    public static CreatorSessionInfo getOrCreateSession(CommandSource commandSource) {
        CreatorSessionInfo sessionInfo = creatorSessionMap.get(commandSource);

        if (sessionInfo == null) {
            sessionInfo = new CreatorSessionInfo();
            CreatorModule.creatorSessionMap.put(commandSource, sessionInfo);
        }

        return sessionInfo;
    }

    @Listener
    public void onPlayerDisconnect(ClientConnectionEvent.Disconnect event) {
        // 削除してみる
        creatorSessionMap.remove(event.getTargetEntity());
    }

    @Listener
    public void onInteractBlock(InteractBlockEvent event, @First Player player) {
        CreatorSessionInfo session = getOrCreateSession(player);

        if (!session.selectionEnabled) {
            return;
        }

        Optional<ItemStack> optional = player.getItemInHand(HandTypes.MAIN_HAND);

        if (!optional.isPresent()) {
            return;
        }

        ItemStack itemStack = optional.get();

        if (itemStack.getItem() == ItemTypes.MAGMA || itemStack.getItem() == ItemTypes.PACKED_ICE) {

            if (player.get(Keys.IS_SNEAKING).orElse(false)) {
                // 姿勢を低くした状態ならスキップ
                return;
            }

            // なんでnullの可能性があるのか不明だが、isPresentでチェック
            Optional<Location<World>> locOpt = event.getTargetBlock().getLocation();

            if (!locOpt.isPresent()) {
                return;
            }

            Location<World> clickLoc = locOpt.get();

            // 実際に設定するロケーション
            Location<World> locToSet;

            if (event instanceof InteractBlockEvent.Primary) {
                // そのまま左クリックなら、破壊したはずのブロックを位置に登録
                locToSet = clickLoc;

            } else if (event instanceof InteractBlockEvent.Secondary) {
                // 右クリックでブロックを置こうとしたら、その置こうとしたところの位置を登録

                locToSet = clickLoc.getRelative(event.getTargetSide());
            } else {
                return;
            }

            // アイテムによって第一ポイントか第二ポイントか変える
            if (itemStack.getItem() == ItemTypes.MAGMA) {
                // 以前選択していたところがあるならそのブロックをなかったコトにする
                if (session.firstLoc != null) {
                    player.resetBlockChange(session.firstLoc.getBlockPosition());
                }

                // 第一ポイントを決定
                session.firstLoc = locToSet;

                player.sendMessage(Text.of(TextColors.AQUA, "第1ポイントを設定しました/",
                        locToSet.getBlockPosition().toString()));
            } else if (itemStack.getItem() == ItemTypes.PACKED_ICE) {
                if (session.secondLoc != null) {
                    player.resetBlockChange(session.secondLoc.getBlockPosition());
                }

                // 第二ポイントを決定
                session.secondLoc = locToSet;

                player.sendMessage(Text.of(TextColors.AQUA, "第2ポイントを設定しました/",
                        locToSet.getBlockPosition().toString()));
            }

            // その後溶岩と水に変化したように見せかける
            Sponge.getScheduler().createTaskBuilder().delayTicks(1)
                    .execute(() -> {
                        player.sendBlockChange(locToSet.getBlockPosition(),
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

                if (session.firstLoc != null) {
                    player.resetBlockChange(session.firstLoc.getBlockPosition());
                }
                if (session.secondLoc != null) {
                    player.resetBlockChange(session.secondLoc.getBlockPosition());
                }

                event.setCancelled(true);
            }
        }
    }
}
