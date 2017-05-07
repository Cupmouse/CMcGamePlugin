package net.cupmouse.minecraft.game.creator.command;

import net.cupmouse.minecraft.CMcCore;
import net.cupmouse.minecraft.game.CMcGamePlugin;
import net.cupmouse.minecraft.game.creator.CreatorModule;
import net.cupmouse.minecraft.game.spleef.SpleefArea;
import net.cupmouse.minecraft.game.spleef.SpleefRoom;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyle;

import java.util.Optional;

public class CCmdAreaLoadSpleef implements CommandExecutor {

    public static final CommandCallable CALLABLE = CommandSpec.builder()
            .arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("stage_id"))),
                    GenericArguments.onlyOne(GenericArguments.string(Text.of("area_id"))))
            .build();

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        String stageId = args.<String>getOne("stage_id").get();
        String areaId = args.<String>getOne("area_id").get();

        Optional<SpleefRoom> roomOpt = CMcGamePlugin.getSpleef().getRoomOfStageId(stageId);

        if (roomOpt.isPresent()) {
            SpleefArea selectedArea = null;

            switch (areaId) {
                case "f":
                    // Fighting Area

                    selectedArea = roomOpt.get().stageSettings.fightingArea;
                    break;
                case "g":
                    // Ground Area

                    selectedArea = roomOpt.get().stageSettings.groundArea;
                    break;
            }

            if (selectedArea == null) {
                src.sendMessage(Text.of(TextColors.RED, "✗そのようなエリアIDは見つかりませんでした。"));

                return CommandResult.empty();
            } else {
                // セッションに設定する

                CreatorModule.getOrCreateSession(src).spleefArea = selectedArea;

                src.sendMessage(Text.of(TextColors.GREEN, "✓入力されたエリアIDのエリアをロードしました。"));

                return CommandResult.success();
            }
        } else {
            src.sendMessage(Text.of(TextColors.RED, "✗そのようなステージIDは見つかりませんでした。"));
            return CommandResult.empty();
        }
    }
}
