package net.cupmouse.minecraft.game.cmd;

import net.cupmouse.minecraft.game.CMcGamePlugin;
import net.cupmouse.minecraft.game.spleef.SpleefRoom;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.ClickAction;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import java.util.ArrayList;

import static org.spongepowered.api.command.args.GenericArguments.none;

public class CmdSpleefList implements CommandExecutor {

    public static final CommandCallable CALLABLE = CommandSpec.builder()
            .arguments(none())
            .executor(new CmdSpleefList())
            .build();

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        ArrayList<Text> texts = new ArrayList<>();

        for (SpleefRoom spleefRoom : CMcGamePlugin.getSpleef().getRooms()) {
            int playersInRoom = spleefRoom.getSpleefPlayerPlaying().size();
            int maxPlayersInRoom = spleefRoom.stageSettings.getSpawnRocations().size();
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
                    .onClick(TextActions.runCommand("join " + spleefRoom.roomNumber)).build();
            texts.add(Text.of(color, TextStyles.BOLD, "(" + playersInRoom + "/" + maxPlayersInRoom + ")",
                    TextStyles.RESET, " ", spleefRoom.roomNumber, " | ", joinButton));
        }

        PaginationList list = PaginationList.builder()
                .title(Text.of(TextColors.AQUA, "Spleef部屋一覧"))
                .contents(texts)
                .header(Text.of("人数はコマンド発行時のものです。"))
                .padding(Text.of("=sp"))
                .build();

        list.sendTo(src);

        return CommandResult.success();
    }
}
