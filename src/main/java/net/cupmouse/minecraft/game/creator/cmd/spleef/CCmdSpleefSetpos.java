package net.cupmouse.minecraft.game.creator.cmd.spleef;

import net.cupmouse.minecraft.game.creator.CreatorBank;
import net.cupmouse.minecraft.game.creator.CreatorModule;
import net.cupmouse.minecraft.game.spleef.SpleefStageTemplate;
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

import java.util.List;

import static org.spongepowered.api.command.args.GenericArguments.onlyOne;
import static org.spongepowered.api.command.args.GenericArguments.string;

public class CCmdSpleefSetpos implements CommandExecutor {

    public static final CommandCallable CALLABLE = CommandSpec.builder()
            .arguments(onlyOne(string(Text.of("position_id"))))
            .executor(new CCmdSpleefSetpos())
            .build();

    private CCmdSpleefSetpos() {
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        String positionId = args.<String>getOne("position_id").get();

        CreatorBank bank = CreatorModule.getOrCreateBankOf(src);
        // 選択されていない場合は例外が発生し、これ以上実行されない
        SpleefStageTemplate template = bank.getSpleefSelectedTemplateOrThrow();

        if (positionId.startsWith("spawn.")) {
            // スポーンを設定

            int spawnIndex;

            try {
                spawnIndex = Integer.parseInt(positionId.substring("spawn.".length()));
            } catch (NumberFormatException e) {
                throw new CommandException(Text.of(TextColors.RED, "✗0以上の整数を入力してください"), false);
            }

            // バンクからRocationをロードする、
            // 回転要素無のLocationがロードされている場合は例外が発生し、続行されない(コマンド異常終了)が
            // ロードされていなくても通る
            WorldTagRocation rocation = bank.getPositionAsRocationOrThrow();

            List<WorldTagRocation> relativeSpawnRocations = template.getSpawnRocations();

            if (rocation == null) {
                // 設定されていないとき、指定されたインデックスの要素削除

                if (spawnIndex < 0|| spawnIndex >= relativeSpawnRocations.size()) {
                    throw new CommandException(Text.of(TextColors.RED, String.format(
                            "✗%d個のスポーン位置が設定されています、それ未満の値を入力してください", spawnIndex))
                            , false);
                }

                relativeSpawnRocations.remove(spawnIndex);

                src.sendMessage(Text.of(TextColors.GOLD, "✓指定されたポジションIDを削除しました"));
                return CommandResult.success();
            } else {
                if (relativeSpawnRocations.size() == spawnIndex) {
                    relativeSpawnRocations.add(spawnIndex, rocation);
                } else {
                    relativeSpawnRocations.set(spawnIndex, rocation);
                }

                src.sendMessage(Text.of(TextColors.GOLD,
                        String.format("✓バンクからポジション%sへ設定しました", positionId)));
                return CommandResult.success();
            }
        }

        throw new CommandException(Text.of(TextColors.RED, "✗ポジションIDが間違っています"));
    }
}
