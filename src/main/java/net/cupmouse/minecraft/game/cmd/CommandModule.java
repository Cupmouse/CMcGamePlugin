package net.cupmouse.minecraft.game.cmd;

import net.cupmouse.minecraft.CMcCore;
import net.cupmouse.minecraft.PluginModule;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandManager;

public class CommandModule implements PluginModule {

    @Override
    public void onInitializationProxy() throws Exception {
        CommandManager cm = Sponge.getCommandManager();
        cm.register(CMcCore.getPlugin(), CmdRoom.CALLABLE, "room");
    }
}
