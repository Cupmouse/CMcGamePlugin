package net.cupmouse.minecraft.game.creator.command;

import net.cupmouse.minecraft.game.creator.command.CommandElementGameType;
import org.spongepowered.api.text.Text;

public final class CCmdArguments {

    private CCmdArguments() {
    }

    public static CommandElementGameType gameType(Text text) {
        return new CommandElementGameType(text);
    }

    public static CommandElementSpleefStageId spleefStageId(Text text) {
        return new CommandElementSpleefStageId(text);
    }
}
