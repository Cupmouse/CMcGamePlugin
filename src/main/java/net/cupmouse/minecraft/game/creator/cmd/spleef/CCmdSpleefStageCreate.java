package net.cupmouse.minecraft.game.creator.cmd.spleef;

import net.cupmouse.minecraft.game.CMcGamePlugin;
import net.cupmouse.minecraft.game.spleef.SpleefRoom;
import net.cupmouse.minecraft.game.spleef.SpleefStage;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

import static org.spongepowered.api.command.args.GenericArguments.integer;
import static org.spongepowered.api.command.args.GenericArguments.onlyOne;
import static org.spongepowered.api.command.args.GenericArguments.string;

public class CCmdSpleefStageCreate implements CommandExecutor {

    public static final CommandCallable CALLABLE = CommandSpec.builder()
            .arguments(onlyOne(string(Text.of("stage_id"))),
                    onlyOne(integer(Text.of("room_number"))))
            .executor(new CCmdSpleefStageCreate())
            .build();

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        String stageId = args.<String>getOne("stage_id").get();
        int roomNumber = args.<Integer>getOne("room_number").get();

        Optional<SpleefRoom> roomOfStageId = CMcGamePlugin.getSpleef().getRoomOfStageId(stageId);

        if (roomOfStageId.isPresent()) {
            throw new CommandException(Text.of(TextColors.RED, "✗そのステージIDのステージはすでに存在します。"));
        }

        Optional<SpleefRoom> roomOptional = CMcGamePlugin.getSpleef().getRoom(roomNumber);

        if (roomOptional.isPresent()) {
            throw new CommandException(Text.of(TextColors.RED, "✗そのルームIDのルームはすでに存在します。"));
        }

        SpleefRoom spleefRoom = new SpleefRoom(SpleefStage.creator());

        CMcGamePlugin.getSpleef().addRoom(stageId, roomNumber, spleefRoom);

        src.sendMessage(Text.of(TextColors.AQUA, "✓ステージID", stageId,"のSpleefステージを作成しました。"));
        return CommandResult.success();
    }
}
