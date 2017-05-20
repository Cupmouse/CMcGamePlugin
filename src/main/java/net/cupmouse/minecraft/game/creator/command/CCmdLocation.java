package net.cupmouse.minecraft.game.creator.command;

import net.cupmouse.minecraft.game.CMcGamePlugin;
import net.cupmouse.minecraft.game.GameType;
import net.cupmouse.minecraft.game.spleef.SpleefRoom;
import net.cupmouse.minecraft.worlds.WorldTagLocation;
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

import static net.cupmouse.minecraft.game.creator.command.CCmdArguments.gameType;
import static org.spongepowered.api.command.args.GenericArguments.allOf;
import static org.spongepowered.api.command.args.GenericArguments.onlyOne;
import static org.spongepowered.api.command.args.GenericArguments.string;

public final class CCmdLocation implements CommandExecutor {

    public static CommandCallable CALLABLE = CommandSpec.builder()
            .arguments(onlyOne(gameType(Text.of("game_type"))),
                    allOf(string(Text.of("lookup_id"))))
            .executor(new CCmdLocation())
            .build();

    private CCmdLocation() {
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        GameType gameType = args.<GameType>getOne("game_type").get();
        ArrayList<String> lookupId = new ArrayList<>(args.<String>getAll("lookup_id"));

        WorldTagLocation selectedLoc = null;

        if (gameType == GameType.SPLEEF && lookupId.size() >= 2) {
            // SPLEEFの場合、引数は必ず２個以上

            // 第一引数は必ずステージ名
            String stageName = lookupId.get(1);
            // 第二引数も必ずロケーション名
            String locationId = lookupId.get(2);

            Optional<SpleefRoom> roomOptional = CMcGamePlugin.getSpleef().getRoomOfStageId(stageName);

            if (roomOptional.isPresent()) {
                src.sendMessage(Text.of(TextColors.RED, "✗そのようなステージIDは見つかりませんでした。"));
                return CommandResult.empty();
            }

            SpleefRoom spleefRoom = roomOptional.get();

            if (lookupId.size() == 2) {
                // 引数が２個のとき


            } else if (lookupId.size() == 3) {
                // 引数が３個のとき

                if (locationId.equals("spawn")) {
                    selectedLoc = spleefRoom.stageSettings.spawnRocations.get(Integer.parseInt(lookupId.get(2)));
                }
            }
            // 引数が二個以上ないとIDがないこととして処理する
        }

        if (selectedLoc != null) {

            src.sendMessage(Text.of(TextColors.AQUA,
                    "✓入力されたロケーションIDのロケーションをロードしました。"));
            return CommandResult.success();
        } else {

            src.sendMessage(Text.of(TextColors.RED,
                    "✗そのようなゲームもしくはロケーションIDが見つかりませんでした。"));
            return CommandResult.empty();
        }

    }
}
