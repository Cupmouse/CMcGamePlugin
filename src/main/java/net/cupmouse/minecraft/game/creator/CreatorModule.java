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
        CMcCore.getLogger().warn("クリエイターモードが有効化されました！");
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
        // 削除してみる
        creatorSessionMap.remove(event.getTargetEntity());
    }

    @Listener
    public void onInteractBlock(InteractBlockEvent event, @First Player player) {
        // 立体選択の実装です
        CreatorBank bank = getOrCreateBankOf(player);

        if (!bank.isSelectionEnabled) {
            // 無効になっているので無視します
            return;
        }

        Optional<ItemStack> optional = player.getItemInHand(HandTypes.MAIN_HAND);

        if (!optional.isPresent()) {
            // 何も持っていないなら無視
            return;
        }

        ItemStack itemStack = optional.get();

        // 手に持っているアイテムがマグマ/硬い氷のときは範囲選択(ポイントが選ばれた)ということ
        if (itemStack.getItem() == ItemTypes.MAGMA || itemStack.getItem() == ItemTypes.PACKED_ICE) {

            if (player.get(Keys.IS_SNEAKING).orElse(false)) {
                // 姿勢を低くした状態なら無視(姿勢を低くすると、範囲選択がONでもこれらのブロックを置くことができる)
                return;
            }

            // クリックされた位置を入手する。右クリックの場合は、右クリックしたブロックが、左クリックの場合は破壊した
            // ブロックの位置が帰って来る
            // なんでnullの可能性があるのか不明だが、isPresentでチェック
            Optional<Location<World>> targetBlockLocOpt = event.getTargetBlock().getLocation();

            if (!targetBlockLocOpt.isPresent()) {
                // なかったら無視
                return;
            }

            Location<World> targetLoc = targetBlockLocOpt.get();

            // 実際に選択された点として記録する位置がこれです
            Location<World> selectedLoc;

            if (event instanceof InteractBlockEvent.Primary) {
                // 左クリックなら、破壊したはずのブロックを位置に登録
                selectedLoc = targetLoc;

            } else if (event instanceof InteractBlockEvent.Secondary) {
                // 右クリックでブロックを置こうとしたら、その置こうとしたところの位置を登録
                // つまり右クリックされたブロックの、クリックされた側面からその方向に１ブロック先が選択点

                selectedLoc = targetLoc.getRelative(event.getTargetSide());
            } else {
                // それ以外は普通ないが無視
                return;
            }

            // 持っているアイテム(ブロック)によって第一ポイントか第二ポイントが選択されたかを変える
            if (itemStack.getItem() == ItemTypes.MAGMA) {
                // 以前選択していたところがあるならその位置のビジュアルビューを消して新しくフェイクブロックを置く
                if (bank.getFirstLoc() != null) {
                    player.resetBlockChange(bank.getFirstLoc().getBlockPosition());
                }

                // 第一ポイントを決定
                bank.setFirstLoc(selectedLoc);

                player.sendMessage(Text.of(TextColors.AQUA, "第1ポイントを設定しました/",
                        selectedLoc.getBlockPosition().toString()));
            } else if (itemStack.getItem() == ItemTypes.PACKED_ICE) {
                if (bank.getSecondLoc() != null) {
                    player.resetBlockChange(bank.getSecondLoc().getBlockPosition());
                }

                // 第二ポイントを決定
                bank.setSecondLoc(selectedLoc);

                player.sendMessage(Text.of(TextColors.AQUA, "第2ポイントを設定しました/",
                        selectedLoc.getBlockPosition().toString()));
            }

            // その後溶岩と水に変化したように見せかける
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
