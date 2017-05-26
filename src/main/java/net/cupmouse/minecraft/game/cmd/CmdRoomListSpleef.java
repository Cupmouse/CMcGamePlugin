package net.cupmouse.minecraft.game.cmd;

import net.cupmouse.minecraft.game.CMcGamePlugin;
import net.cupmouse.minecraft.game.spleef.SpleefRoom;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import java.util.ArrayList;
import java.util.Map;

import static org.spongepowered.api.command.args.GenericArguments.none;

public class CmdRoomListSpleef implements CommandExecutor {

    public static final CommandCallable CALLABLE = CommandSpec.builder()
            .arguments(none())
            .executor(new CmdRoomListSpleef())
            .build();

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        ArrayList<Text> texts = new ArrayList<>();

        for (Map.Entry<SpleefRoom, Integer> entry : CMcGamePlugin.getSpleef().getRoomsAndItsNumber()) {
            SpleefRoom room = entry.getKey();
            int roomNumber = entry.getValue();

            int playersInRoom = room.getSpleefPlayerPlaying().size();
            int maxPlayersInRoom = room.stage.getSpawnRocations().size();
            float occupRate = ((float) playersInRoom) / maxPlayersInRoom;

            TextColor color;
            if (occupRate <= 0) {
                color = TextColors.GRAY;
            } else if (occupRate <= 0.3) {
                color = TextColors.GREEN;
            } else if (occupRate <= 0.8) {
                color = TextColors.DARK_GREEN;
            } else if (occupRate <= 0.9) {
                color = TextColors.RED;
            } else if (occupRate >= 1) {
                color = TextColors.DARK_RED;
            } else {
                color = TextColors.DARK_GRAY;
            }

            Text joinButton = Text.builder("[参加]")
                    .onClick(TextActions.runCommand("/room join spleef " + roomNumber)).build();
            texts.add(Text.of(color, TextStyles.BOLD, "(" + playersInRoom + "/" + maxPlayersInRoom + ")",
                    TextStyles.RESET, " ", roomNumber, " | ", joinButton));
        }

        PaginationList list = PaginationList.builder()
                .title(Text.of(TextColors.AQUA, "Spleef部屋一覧"))
                .contents(texts)
                .header(Text.of("人数はコマンド発行時のものです。"))
                .padding(Text.of("="))
                .build();

        list.sendTo(src);

        return CommandResult.success();
    }
}
