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

import java.util.Optional;

public class CCmdAreaLoad implements CommandExecutor {

    public static final CommandCallable CALLABLE = CommandSpec.builder()
            .arguments(
                    GenericArguments.onlyOne(GenericArguments.choices(Text.of("game_type"))),
                    GenericArguments.onlyOne(GenericArguments.string(Text.of("area_lookup_id"))))
            .executor(new CCmdAreaLoad())
            .build();

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        args.
        String areaLookupId = args.<String>getOne("area_lookup_id").get();



        return null;
    }
}
