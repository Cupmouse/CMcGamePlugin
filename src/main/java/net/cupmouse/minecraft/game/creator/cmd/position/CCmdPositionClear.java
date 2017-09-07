package net.cupmouse.minecraft.game.creator.cmd.position;

import net.cupmouse.minecraft.game.creator.CreatorModule;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import static org.spongepowered.api.command.args.GenericArguments.none;

public class CCmdPositionClear implements CommandExecutor {

    public static CommandCallable CALLABLE = CommandSpec.builder()
            .arguments(none())
            .executor(new CCmdPositionClear())
            .build();

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        CreatorModule.getOrCreateBankOf(src).setPosition(null);

        src.sendMessage(Text.of(TextColors.GOLD, "✓バンクにロードされたポジションを削除しました"));

        return CommandResult.success();
    }
}
