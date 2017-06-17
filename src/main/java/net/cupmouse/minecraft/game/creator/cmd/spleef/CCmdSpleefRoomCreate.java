package net.cupmouse.minecraft.game.creator.cmd.spleef;

import net.cupmouse.minecraft.game.CMcGamePlugin;
import net.cupmouse.minecraft.game.manager.GameException;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import static org.spongepowered.api.command.args.GenericArguments.integer;
import static org.spongepowered.api.command.args.GenericArguments.onlyOne;
import static org.spongepowered.api.command.args.GenericArguments.string;

public class CCmdSpleefRoomCreate implements CommandExecutor {

    public static final CommandCallable CALLABLE = CommandSpec.builder()
            .arguments(onlyOne(integer(Text.of("room_number"))),
                    onlyOne(string(Text.of("stage_id"))))
            .executor(new CCmdSpleefRoomCreate())
            .build();

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Integer roomNumber = args.<Integer>getOne("room_number").get();
        String stageId = args.<String>getOne("stage_id").get();

        try {
            CMcGamePlugin.getSpleef().newRoom(roomNumber, stageId);
        } catch (GameException e) {
            throw new CommandException(Text.of(TextColors.RED, "✗ステージを作成できませんでした。", e.getText()),
                    e, false);
        }

        src.sendMessage(Text.of(TextColors.AQUA, "✓ステージID", stageId,"のSpleefステージを作成しました。"));
        return CommandResult.success();
    }
}
