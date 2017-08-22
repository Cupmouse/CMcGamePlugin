package net.cupmouse.minecraft.game.creator.cmd.spleef;

import net.cupmouse.minecraft.game.creator.CreatorBank;
import net.cupmouse.minecraft.game.creator.CreatorModule;
import net.cupmouse.minecraft.game.spleef.SpleefStage;
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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static org.spongepowered.api.command.args.GenericArguments.onlyOne;
import static org.spongepowered.api.command.args.GenericArguments.string;

public class CCmdSpleefLoadarea implements CommandExecutor {

    public static final CommandCallable CALLABLE = CommandSpec.builder()
            .arguments(onlyOne(string(Text.of("area_id"))))
            .executor(new CCmdSpleefLoadarea())
            .build();


    // すべてのエリアIDをIfで書くと読みづらくなるので、ゲッターをローダーとしてラムダ関数をマップする
    // 後に必要なときにステージを入れるとゲッターの結果が帰ってくるものがローダー
    private Map<String, Function<SpleefStage, WorldTagArea>> loaders = new HashMap<>();

    private CCmdSpleefLoadarea() {
        // ローダーを設定する

        Function<SpleefStage, WorldTagArea> groundAreaLoader = SpleefStage::getGroundArea;
        loaders.put("ground", groundAreaLoader);
        loaders.put("g", groundAreaLoader);

        Function<SpleefStage, WorldTagArea> fightingAreaLoader = SpleefStage::getFightingArea;
        loaders.put("fighting", fightingAreaLoader);
        loaders.put("f", fightingAreaLoader);
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        // バンクを読み込み
        CreatorBank bank = CreatorModule.getOrCreateBankOf(src);

        // 入力されたエリアIDを取得するonlyOneなのでisPresentを確認しなくても良い
        String areaId = args.<String>getOne("area_id").get();

        // 予め用意されたローダーからエリアIDと一致するものを入手する
        Function<SpleefStage, WorldTagArea> loader = loaders.get(areaId);

        if (loader == null) {
            throw new CommandException(Text.of(TextColors.RED, "✗入力されたエリアIDは見つかりませんでした。"));
        }

        // バンクにローダーの結果を設定するバンクに設定されていない場合は例外が発生し、設定されない
        bank.setArea(loader.apply(bank.getSpleefSelectedTemplateOrThrow()));

        src.sendMessage(Text.of(TextColors.GREEN, "✓エリアをロードしました。"));

        // コマンドは成功
        return CommandResult.success();
    }
}
