package net.cupmouse.minecraft.game.creator.cmd;

import org.spongepowered.api.text.Text;

public final class CCmdArguments {

    private CCmdArguments() {
    }

    public static CommandElementGameType gameType(Text text) {
        return new CommandElementGameType(text);
    }
}
