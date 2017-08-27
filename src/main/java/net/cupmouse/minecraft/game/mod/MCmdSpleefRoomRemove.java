package net.cupmouse.minecraft.game.mod;

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

public class MCmdSpleefRoomRemove implements CommandExecutor {

    public static final CommandCallable CALLABLE = CommandSpec.builder()
            .arguments(onlyOne(integer(Text.of("room_number"))))
            .executor(new MCmdSpleefRoomRemove())
            .build();

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        int roomNumber = args.<Integer>getOne("room_number").get();

        try {
            CMcGamePlugin.getSpleef().removeRoom(roomNumber);
        } catch (GameException e) {
            throw new CommandException(Text.of(TextColors.RED, "✗ステージを削除できませんでした。", e.getText()),
                    e, false);
        }
        src.sendMessage(Text.of(TextColors.GOLD, "✓ステージを削除しました。"));
        return CommandResult.success();
    }
}
