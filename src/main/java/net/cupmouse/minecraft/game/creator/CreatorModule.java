package net.cupmouse.minecraft.game.creator;

import net.cupmouse.minecraft.CMcCore;
import net.cupmouse.minecraft.PluginModule;
import net.cupmouse.minecraft.game.CMcGamePlugin;
import net.cupmouse.minecraft.game.creator.command.CCmdArea;
import net.cupmouse.minecraft.game.creator.command.CommandElementGameType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.LiteralText;
import org.spongepowered.api.text.Text;

import java.util.HashMap;
import java.util.Map;

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
}
