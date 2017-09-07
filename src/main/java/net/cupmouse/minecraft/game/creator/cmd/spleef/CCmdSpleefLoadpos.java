package net.cupmouse.minecraft.game.creator.cmd.spleef;

import net.cupmouse.minecraft.game.creator.CreatorBank;
import net.cupmouse.minecraft.game.creator.CreatorModule;
import net.cupmouse.minecraft.game.spleef.SpleefStageTemplate;
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
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.spongepowered.api.command.args.GenericArguments.onlyOne;
import static org.spongepowered.api.command.args.GenericArguments.string;

public class CCmdSpleefLoadpos implements CommandExecutor {

    public static final CommandCallable CALLABLE = CommandSpec.builder()
            .arguments(onlyOne(string(Text.of("position_id"))))
            .executor(new CCmdSpleefLoadpos())
            .build();

    private Map<String, Function<SpleefStageTemplate, WorldTagPosition>> loaders = new HashMap<>();

    private CCmdSpleefLoadpos() {
        Function<SpleefStageTemplate, WorldTagPosition> relativeBaseLocationLoader =
                SpleefStageTemplate::getRelativeBaseLocation;
        this.loaders.put("rbl", relativeBaseLocationLoader);
        this.loaders.put("relativeBaseLocation", relativeBaseLocationLoader);

        Function<SpleefStageTemplate, WorldTagPosition> waitingSpawnRocationLoader =
                SpleefStageTemplate::getWaitingSpawnRocation;
        this.loaders.put("wsr", waitingSpawnRocationLoader);
        this.loaders.put("waitingSpawnRocation", waitingSpawnRocationLoader);
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        String positionId = args.<String>getOne("position_id").get();

        CreatorBank bank = CreatorModule.getOrCreateBankOf(src);

        if (positionId.startsWith("spawn.")) {
            // スポーンをロード

            // 整数値が入力されたか確認する
            int spawnIndex;

            try {
                spawnIndex = Integer.parseInt(positionId.substring("spawn.".length()));
            } catch (NumberFormatException e) {
                throw new CommandException(Text.of(TextColors.RED, "✗0以上の整数を入力してください"), false);
            }

            SpleefStageTemplate template = bank.getSpleefSelectedTemplateOrThrow();

            List<WorldTagRocation> relativeSpawnRocations = template.getSpawnRocations();

            // 入力された整数値が定義されているスポーンの数を超えるとぬるぽなので回避
            if (spawnIndex >= relativeSpawnRocations.size()) {
                throw new CommandException(Text.of(TextColors.RED, String.format(
                                "✗%d個のスポーン位置が設定されています、それ未満の値を入力してください", spawnIndex))
                        , false);
            }
            // 実際にバンクに設定する
            bank.setPosition(relativeSpawnRocations.get(spawnIndex));
        } else {
            // ローダーに任せる
            Function<SpleefStageTemplate, WorldTagPosition> loader = this.loaders.get(positionId);

            if (loader == null) {
                throw new CommandException(Text.of(TextColors.RED, "✗ポジションIDが間違っています"));
            }

            SpleefStageTemplate template = bank.getSpleefSelectedTemplateOrThrow();

            bank.setPosition(loader.apply(template));

        }

        // ここまで来ると正常にロードされている。コマンド正常終了
        src.sendMessage(Text.of(TextColors.GOLD, String.format("✓バンクに位置%sをロードしました", positionId)));
        return CommandResult.success();
    }
}
