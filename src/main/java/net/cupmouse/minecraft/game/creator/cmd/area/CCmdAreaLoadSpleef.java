package net.cupmouse.minecraft.game.creator.cmd.area;

import net.cupmouse.minecraft.game.creator.CreatorModule;
import net.cupmouse.minecraft.game.creator.cmd.CCmdArguments;
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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static org.spongepowered.api.command.args.GenericArguments.onlyOne;
import static org.spongepowered.api.command.args.GenericArguments.string;

public class CCmdAreaLoadSpleef implements CommandExecutor {

    public static final CommandCallable CALLABLE = CommandSpec.builder()
            .arguments(onlyOne(CCmdArguments.spleefStageId(Text.of("stage_id"))),
                    onlyOne(string(Text.of("area_id"))))
            .executor(new CCmdAreaLoadSpleef())
            .build();

    private Map<String, Function<SpleefStage, WorldTagArea>> loaders = new HashMap<>();

    private CCmdAreaLoadSpleef() {
        Function<SpleefStage, WorldTagArea> groundAreaLoader = stage -> stage.getGroundArea();
        loaders.put("ground", groundAreaLoader);
        loaders.put("g", groundAreaLoader);

        Function<SpleefStage, WorldTagArea> fightingAreaLoader =
                stage -> stage.getFightingArea();
        loaders.put("fighting", fightingAreaLoader);
        loaders.put("f", fightingAreaLoader);
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        SpleefStage stage = args.<SpleefStage>getOne("stage_id").get();
        String areaId = args.<String>getOne("area_id").get();

        // セッションに設定する

        Function<SpleefStage, WorldTagArea> loader = loaders.get(areaId);

        if (loader == null) {
            throw new CommandException(Text.of(TextColors.RED, "✗入力されたエリアIDは見つかりませんでした。"));
        }

        CreatorModule.getOrCreateSession(src).loadedArea = loader.apply(stage);

        src.sendMessage(Text.of(TextColors.GREEN, "✓入力されたエリアIDのエリアをロードしました。"));

        return CommandResult.success();
    }
}
