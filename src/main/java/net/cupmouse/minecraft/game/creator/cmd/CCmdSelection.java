package net.cupmouse.minecraft.game.creator.cmd;

import net.cupmouse.minecraft.game.CMcGamePlugin;
import net.cupmouse.minecraft.game.creator.CreatorModule;
import net.cupmouse.minecraft.game.creator.CreatorSessionInfo;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class CCmdSelection implements CommandExecutor {
    public static final CommandCallable CALLABLE = CommandSpec.builder()
            .arguments(GenericArguments.none())
            .executor(new CCmdSelection())
            .build();

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        CreatorSessionInfo session = CreatorModule.getOrCreateSession(src);

        session.selectionEnabled = !session.selectionEnabled;

        if (session.selectionEnabled) {
            src.sendMessage(Text.of(TextColors.AQUA, "✓範囲指定は現在",
                    TextColors.GREEN, "有効", TextColors.AQUA, "です。"));
        } else {
            src.sendMessage(Text.of(TextColors.AQUA, "✓範囲指定は現在",
                    TextColors.RED, "無効", TextColors.AQUA, "です。"));
        }

        return CommandResult.success();
    }
}
