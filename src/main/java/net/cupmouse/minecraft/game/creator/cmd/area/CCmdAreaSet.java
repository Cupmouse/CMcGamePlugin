package net.cupmouse.minecraft.game.creator.cmd.area;

import net.cupmouse.minecraft.game.CMcGamePlugin;
import net.cupmouse.minecraft.game.GameType;
import net.cupmouse.minecraft.game.creator.CreatorModule;
import net.cupmouse.minecraft.game.spleef.stage.SpleefStage;
import net.cupmouse.minecraft.worlds.WorldTagArea;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.ArrayList;
import java.util.Optional;

import static net.cupmouse.minecraft.game.creator.cmd.CCmdArguments.gameType;
import static org.spongepowered.api.command.args.GenericArguments.allOf;
import static org.spongepowered.api.command.args.GenericArguments.onlyOne;
import static org.spongepowered.api.command.args.GenericArguments.string;
// TODO
public class CCmdAreaSet implements CommandExecutor {

    public static final CommandCallable CALLABLE = CommandSpec.builder()
            .arguments(onlyOne(gameType(Text.of("game_type"))),
                    allOf(string(Text.of("lookup_id"))))
            .executor(new CCmdAreaSet())
            .build();


    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        GameType gameType = args.<GameType>getOne("game_type").get();
        ArrayList<String> lookupId = new ArrayList<>(args.<String>getAll("lookup_id"));

        WorldTagArea loadedArea = CreatorModule.getOrCreateSession(src).loadedArea;
        if (loadedArea == null) {
            throw new CommandException(Text.of(TextColors.RED, "✗エリアをロードしてください。"));
        }

        if (gameType == GameType.SPLEEF && lookupId.size() == 2) {
            // SPLEEFの場合、引数は２個

            String stageId = lookupId.get(0);
            String areaId = lookupId.get(1);

            Optional<SpleefStage> stageOptional = CMcGamePlugin.getSpleef().getStage(stageId);

            if (!stageOptional.isPresent()) {
                throw new CommandException(
                        Text.of(TextColors.RED, "✗そのようなステージIDは見つかりませんでした。"), false);
            }

            SpleefStage stage = stageOptional.get();

            switch (areaId) {
                case "fighting":
                case "f":
                    stage.setFightingArea(loadedArea);
                    break;
                case "ground":
                case "g":
                    stage.setGroundArea(loadedArea);
                    break;
                default:
                    throw new CommandException(
                            Text.of(TextColors.RED,
                                    "✗そのようなSPLEEFエリアIDは見つかりませんでした。"), false);
            }
        } else {
            throw new CommandException(
                    Text.of(TextColors.RED, "✗そのようなゲームまたはエリアIDは見つかりませんでした。"), false);
        }

        src.sendMessage(Text.of(TextColors.AQUA, "✓設定しました。"));
        return CommandResult.success();
    }
}
