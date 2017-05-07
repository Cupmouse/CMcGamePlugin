package net.cupmouse.minecraft.game.creator.command;

import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

import static net.cupmouse.minecraft.game.creator.CreatorModule.TEXT_DEFAULT_DESCRIPTION;

public class CCmdArea {

    private static final CommandCallable CALLABLE = CommandSpec.builder()
            .arguments()
            .child(CCmdAreaLoad.CALLABLE)
            .build();

}
