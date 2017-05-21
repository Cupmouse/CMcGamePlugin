package net.cupmouse.minecraft.game.creator.cmd.position;

import net.cupmouse.minecraft.game.creator.CreatorModule;
import net.cupmouse.minecraft.game.spleef.SpleefRoom;
import net.cupmouse.minecraft.util.DualConsumer;
import net.cupmouse.minecraft.worlds.WorldTagLocation;
import net.cupmouse.minecraft.worlds.WorldTagPosition;
import net.cupmouse.minecraft.worlds.WorldTagRocation;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.HashMap;
import java.util.Map;

import static net.cupmouse.minecraft.game.creator.cmd.CCmdArguments.spleefStageId;
import static org.spongepowered.api.command.args.GenericArguments.onlyOne;
import static org.spongepowered.api.command.args.GenericArguments.string;

public class CCmdPositionSetSpleef implements CommandExecutor {

    public static final CommandCallable CALLABLE = CommandSpec.builder()
            .arguments(onlyOne(spleefStageId(Text.of("stage_id"))),
                    onlyOne(string(Text.of("area_id"))))
            .executor(new CCmdPositionSetSpleef())
            .build();

    private Map<String, DualConsumer<SpleefRoom, WorldTagPosition>> setters = new HashMap<>();

    private CCmdPositionSetSpleef() {
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        SpleefRoom spleefRoom = args.<SpleefRoom>getOne("stage_id").get();
        String positionId = args.<String>getOne("position_id").get();

        WorldTagPosition loadedPos = CreatorModule.getOrCreateSession(src).loadedPos;

        if (loadedPos == null) {
            throw new CommandException(Text.of(TextColors.RED, "✗ポジションをロードして下さい。"));
        }

        if (positionId.startsWith("spawn.")) {
            // スポーンを設定

            int number;

            try {
                number = Integer.parseInt(
                        positionId.substring("spawn.".length())
                );
            } catch (NumberFormatException e) {
                throw new CommandException(
                        Text.of(TextColors.RED, "✗スポーン番号を正しく入力して下さい。")
                        , false);
            }

            // Rocationかどうか確認してそうでなければ変換する

            WorldTagRocation spawnRoc;

            if (loadedPos instanceof WorldTagRocation) {
                spawnRoc = ((WorldTagRocation) loadedPos);
            } else {
                src.sendMessage(Text.of(TextColors.GOLD,
                        "❢ロードされたポジションが回転要素を持っていません。")
                );

                spawnRoc = ((WorldTagLocation) loadedPos).convertRocation();
            }

            spleefRoom.stageSettings.getSpawnRocations().set(number, spawnRoc);
        } else {
            DualConsumer<SpleefRoom, WorldTagPosition> setter = setters.get(positionId);

            if (setter == null) {
                throw new CommandException(
                        Text.of(TextColors.RED, "✗指定されたポジションIDは見つかりませんでした。"));
            }

            // 変更してもらう
            setter.accept(spleefRoom, loadedPos);
        }

        src.sendMessage(Text.of(TextColors.AQUA, "✓設定しました。"));
        return CommandResult.success();
    }
}
