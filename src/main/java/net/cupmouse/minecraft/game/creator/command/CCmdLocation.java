package net.cupmouse.minecraft.game.creator.command;

import net.cupmouse.minecraft.game.GameType;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

public final class CCmdLocation implements CommandExecutor {

    public static CommandCallable CALLABLE = CommandSpec.builder()
            .arguments(GenericArguments.onlyOne(CCmdArguments.gameType(Text.of("game_type"))),
                    GenericArguments.onlyOne(GenericArguments.string(Text.of("lookup_id"))))
            .executor(new CCmdLocation())
            .build();

    private CCmdLocation() {
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        GameType gameType = args.<GameType>getOne("game_type").get();
        String lookupId = args.<String>getOne("lookup_id").get();
        


        return CommandResult.success();
    }
}
