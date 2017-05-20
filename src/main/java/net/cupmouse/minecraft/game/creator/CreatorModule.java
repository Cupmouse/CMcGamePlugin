package net.cupmouse.minecraft.game.creator;

import net.cupmouse.minecraft.CMcCore;
import net.cupmouse.minecraft.PluginModule;
import net.cupmouse.minecraft.game.CMcGamePlugin;
import net.cupmouse.minecraft.game.creator.command.CCmdArea;
import net.cupmouse.minecraft.game.creator.command.CCmdLocation;
import net.cupmouse.minecraft.game.creator.command.CommandElementGameType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.LiteralText;
import org.spongepowered.api.text.Text;
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
                .child(CCmdLocation.CALLABLE, "location", "loc", "l")
                .build(), "gc");

        CMcCore.getLogger().warn("=========================================");
        CMcCore.getLogger().warn("クリエイターモードが有効化されました！");
        CMcCore.getLogger().warn("=========================================");

    }

    public static CreatorSessionInfo getOrCreateSession(CommandSource commandSource) {
        CreatorSessionInfo sessionInfo = creatorSessionMap.get(commandSource);

        if (sessionInfo == null) {
            sessionInfo = new CreatorSessionInfo();
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

        if (!player.get(Keys.IS_SNEAKING).orElse(false)) {
            // 姿勢を低くした状態ならスキップ
            return;
        }

        // なんでnullの可能性があるのか不明だが、isPresentでチェック
        Optional<Location<World>> locOpt = event.getTargetBlock().getLocation();

        if (!locOpt.isPresent()) {
            return;
        }

        Location<World> clickLoc = locOpt.get();

        Optional<ItemStack> optional = player.getItemInHand(HandTypes.MAIN_HAND);

        if (optional.isPresent()) {
            ItemStack itemStack = optional.get();

            if (event instanceof InteractBlockEvent.Primary) {
                // そのまま左クリックなら、破壊したはずのブロックを位置に登録

                if (itemStack.getItem() == ItemTypes.MAGMA) {
                    // 第一ポイントを決定
                    getOrCreateSession(player).firstLoc = clickLoc;
                } else if (itemStack.getItem() == ItemTypes.PACKED_ICE) {
                    // 第二ポイントを決定
                    getOrCreateSession(player).secondLoc = clickLoc;
                }
            } else if (event instanceof InteractBlockEvent.Secondary) {
                // 右クリックでブロックを置こうとしたら、その置こうとしたところの位置を登録

                Location<World> placeLoc = clickLoc.getRelative(event.getTargetSide());

                if (itemStack.getItem() == ItemTypes.MAGMA) {
                    // 第一ポイントを決定
                    getOrCreateSession(player).firstLoc = placeLoc;

                } else if (itemStack.getItem() == ItemTypes.PACKED_ICE) {
                    // 第二ポイントを決定
                    getOrCreateSession(player).secondLoc = placeLoc;
                }
            }
        }
        // TODO fakeblock とメッセージ
    }
}
