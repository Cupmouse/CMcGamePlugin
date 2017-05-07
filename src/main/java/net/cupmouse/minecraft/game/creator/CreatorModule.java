package net.cupmouse.minecraft.game.creator;

import net.cupmouse.minecraft.CMcCore;
import net.cupmouse.minecraft.PluginModule;
import net.cupmouse.minecraft.game.CMcGamePlugin;
import net.cupmouse.minecraft.game.creator.command.CCmdArea;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

public class CreatorModule implements PluginModule {

    public static final String TEXT_DEFAULT_DESCRIPTION = "THIS IS FOR LIMITED PEOPLE ONLY. " +
            "IF YOU NEED HELP WITH THIS COMMAND, " +
            "THEN CHECK OUT OTHER PAPER FOR USAGE OF THE COMMAND, " +
            "OR JUST ASK SOMEONE WHO KNOWS ABOUT IT.";

    private boolean creatorEnabled;

    @Override
    public void onInitializationProxy() throws Exception {
        this.creatorEnabled = CMcGamePlugin.getGameConfigNode().getNode("creator_enabled").getBoolean();

        if (!creatorEnabled) {
            return;
        }

        CMcCore.getLogger().warn("=========================================");
        CMcCore.getLogger().warn("クリエイターモードが有効化されました！");
        CMcCore.getLogger().warn("=========================================");

        Sponge.getCommandManager().register(CMcCore.getPlugin(), CommandSpec.builder()
                .description(Text.of(TEXT_DEFAULT_DESCRIPTION))
                .permission("cmc.game.creator")
                .child(CCmdArea.CALLABLE, "area", "a")
                .build(), "gc");
    }


}
