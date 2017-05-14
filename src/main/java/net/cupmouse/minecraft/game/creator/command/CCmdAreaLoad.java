package net.cupmouse.minecraft.game.creator.command;

import net.cupmouse.minecraft.game.CMcGamePlugin;
import net.cupmouse.minecraft.game.GameType;
import net.cupmouse.minecraft.game.creator.CreatorModule;
import net.cupmouse.minecraft.worlds.WorldTagArea;
import net.cupmouse.minecraft.game.spleef.SpleefRoom;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.cupmouse.minecraft.game.creator.command.CCmdArguments.gameType;
import static org.spongepowered.api.command.args.GenericArguments.onlyOne;
import static org.spongepowered.api.command.args.GenericArguments.string;

public final class CCmdAreaLoad implements CommandExecutor {

    public static final CommandCallable CALLABLE = CommandSpec.builder()
            .arguments(onlyOne(gameType(Text.of("game_type"))),
                    onlyOne(string(Text.of("lookup_id"))))
            .build();

    private static final Pattern SPLEEF_LOOKUP_ID_REGEX =
            Pattern.compile("^([a-zA-Z\\d]+)[.]([a-zA-Z\\d]+)$");

    private CCmdAreaLoad() {
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        GameType gameType = args.<GameType>getOne("game_type").get();
        String lookupId = args.<String>getOne("lookup_id").get();

        WorldTagArea selectedArea = null;

        switch (gameType) {
            case SPLEEF:
                Matcher matcher = SPLEEF_LOOKUP_ID_REGEX.matcher(lookupId);

                if (!matcher.find()) {
                    // マッチしなかったら、IDとして存在しない処理となる。
                    break;
                }

                String stageId = matcher.group(1);
                String areaId = matcher.group(2);

                Optional<SpleefRoom> roomOpt = CMcGamePlugin.getSpleef().getRoomOfStageId(stageId);

                if (roomOpt.isPresent()) {

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

                } else {
                    src.sendMessage(Text.of(TextColors.RED, "✗そのようなステージIDは見つかりませんでした。"));
                    return CommandResult.empty();
                }

                break;
        }

        if (selectedArea == null) {
            src.sendMessage(Text.of(TextColors.RED, "✗そのようなゲームもしくはエリアIDは見つかりませんでした。"));

            return CommandResult.empty();
        } else {
            // セッションに設定する

            CreatorModule.getOrCreateSession(src).worldTagArea = selectedArea;

            src.sendMessage(Text.of(TextColors.GREEN, "✓入力されたエリアIDのエリアをロードしました。"));

            return CommandResult.success();
        }
    }
}
