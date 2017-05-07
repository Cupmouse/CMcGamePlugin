package net.cupmouse.minecraft.game.creator.command;

import net.cupmouse.minecraft.game.GameType;
import net.cupmouse.minecraft.game.manager.GameManager;
import net.cupmouse.minecraft.game.manager.GameRoom;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

public class CCmdAreaLoad {

    public static final CommandCallable CALLABLE = CommandSpec.builder()
            .child(CCmdAreaLoadSpleef.CALLABLE, GameType.SPLEEF.aliases)
            .build();

    private CCmdAreaLoad() {
    }
}
