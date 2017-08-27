package net.cupmouse.minecraft.game.creator.cmd;

import net.cupmouse.minecraft.game.CMcGamePlugin;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

public class CCmdTest implements CommandExecutor {

    public static final CommandCallable CALLABLE = CommandSpec.builder()
            .arguments(GenericArguments.allOf(GenericArguments.string(Text.of("argdesu"))))
            .executor(new CCmdTest())
            .build();


    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        CMcGamePlugin.getSpleef().save();

        return CommandResult.success();
    }
}
