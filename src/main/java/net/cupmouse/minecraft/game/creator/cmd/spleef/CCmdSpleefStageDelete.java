package net.cupmouse.minecraft.game.creator.cmd.spleef;

import net.cupmouse.minecraft.game.CMcGamePlugin;
import net.cupmouse.minecraft.game.creator.cmd.CCmdArguments;
import net.cupmouse.minecraft.game.spleef.SpleefRoom;
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

import java.util.Optional;

import static net.cupmouse.minecraft.game.creator.cmd.CCmdArguments.spleefStageId;
import static org.spongepowered.api.command.args.GenericArguments.onlyOne;
import static org.spongepowered.api.command.args.GenericArguments.string;

public class CCmdSpleefStageDelete implements CommandExecutor {

    public static final CommandCallable CALLABLE = CommandSpec.builder()
            .arguments(onlyOne(string(Text.of("stage_id"))))
            .executor(new CCmdSpleefStageDelete())
            .build();

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        String stageId = args.<String>getOne("stage_id").get();

        if (!CMcGamePlugin.getSpleef().getRoomOfStageId(stageId).isPresent()) {
            throw new CommandException(Text.of(TextColors.RED, "✗そのステージIDのステージは存在しません。"));
        }

        CMcGamePlugin.getSpleef().removeRoom(stageId);
        src.sendMessage(Text.of(TextColors.AQUA, "✓ステージを削除しました。"));
        return CommandResult.success();
    }
}
