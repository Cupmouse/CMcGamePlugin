package net.cupmouse.minecraft.game.mod;

import net.cupmouse.minecraft.CMcCore;
import net.cupmouse.minecraft.PluginModule;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.spec.CommandSpec;

public class ModeratorCommandModule implements PluginModule {

    @Override
    public void onInitializationProxy() throws Exception {
//        Sponge.getCommandManager().register(CMcCore.getPlugin(), MCmdRoom.CALLABLE);
    }
}
