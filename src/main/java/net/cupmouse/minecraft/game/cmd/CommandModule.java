package net.cupmouse.minecraft.game.cmd;

import net.cupmouse.minecraft.CMcCore;
import net.cupmouse.minecraft.PluginModule;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.spec.CommandSpec;

public class CommandModule implements PluginModule {

    @Override
    public void onInitializationProxy() throws Exception {
        Sponge.getCommandManager().register(CMcCore.getPlugin(), CmdSpleef.CALLABLE, "spleef");
        Sponge.getCommandManager().register(CMcCore.getPlugin(), CmdQuit.CALLABLE, "quit", "q");
    }
}
