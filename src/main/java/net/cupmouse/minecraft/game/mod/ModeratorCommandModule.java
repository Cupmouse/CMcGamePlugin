package net.cupmouse.minecraft.game.mod;

import net.cupmouse.minecraft.CMcCore;
import net.cupmouse.minecraft.PluginModule;
import net.cupmouse.minecraft.game.GameType;
import org.spongepowered.api.Sponge;

public class ModeratorCommandModule implements PluginModule {

    @Override
    public void onInitializationProxy() throws Exception {
        Sponge.getCommandManager().register(CMcCore.getPlugin(), MCmdSpleefRoom.CALLABLE,
                GameType.SPLEEF.aliases.stream().map(s -> "m" + s).toArray(String[]::new));

    }
}
