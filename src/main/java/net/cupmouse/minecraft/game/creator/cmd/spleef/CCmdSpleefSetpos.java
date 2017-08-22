package net.cupmouse.minecraft.game.creator.cmd.spleef;

import net.cupmouse.minecraft.game.creator.CreatorModule;
import net.cupmouse.minecraft.game.spleef.SpleefStage;
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

public class CCmdSpleefSetpos implements CommandExecutor {

    public static final CommandCallable CALLABLE = CommandSpec.builder()
            .arguments(onlyOne(string(Text.of("position_id"))))
            .executor(new CCmdSpleefSetpos())
            .build();

    private Map<String, DualConsumer<SpleefStage, WorldTagPosition>> setters = new HashMap<>();

    private CCmdSpleefSetpos() {
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        SpleefStage stage = args.<SpleefStage>getOne("stage_id").get();
        String positionId = args.<String>getOne("position_id").get();

        WorldTagPosition loadedPos = CreatorModule.getOrCreateBankOf(src).loadedLoc;

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

            if (stage.getSpawnRocations().size() == number) {
                stage.getSpawnRocations().add(spawnRoc);
            } else {
                stage.getSpawnRocations().set(number, spawnRoc);
            }
        } else {
            DualConsumer<SpleefStage, WorldTagPosition> setter = setters.get(positionId);

            if (setter == null) {
                throw new CommandException(
                        Text.of(TextColors.RED, "✗指定されたポジションIDは見つかりませんでした。"));
            }

            // 変更してもらう
            setter.accept(stage, loadedPos);
        }

        src.sendMessage(Text.of(TextColors.AQUA, "✓設定しました。"));
        return CommandResult.success();
    }
}
