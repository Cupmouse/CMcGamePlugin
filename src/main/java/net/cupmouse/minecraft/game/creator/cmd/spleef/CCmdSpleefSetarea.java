package net.cupmouse.minecraft.game.creator.cmd.spleef;

import net.cupmouse.minecraft.game.CMcGamePlugin;
import net.cupmouse.minecraft.game.creator.CreatorModule;
import net.cupmouse.minecraft.game.spleef.SpleefStageTemplate;
import net.cupmouse.minecraft.util.DualConsumer;
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
import java.util.Optional;

import static org.spongepowered.api.command.args.GenericArguments.onlyOne;
import static org.spongepowered.api.command.args.GenericArguments.string;
// TODO
public class CCmdSpleefSetarea implements CommandExecutor {

    public static final CommandCallable CALLABLE = CommandSpec.builder()
            .arguments(onlyOne(string(Text.of("area_id"))))
            .executor(new CCmdSpleefSetarea())
            .build();

    private Map<String, DualConsumer<SpleefStageTemplate, WorldTagArea>> setter = new HashMap<>();

    private CCmdSpleefSetarea() {
        DualConsumer<SpleefStageTemplate, WorldTagArea> fightingAreaSetter = SpleefStageTemplate::setRelativeFightingArea;
        this.setter.put("f", fightingAreaSetter);
        this.setter.put("fighting", fightingAreaSetter);

    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        String areaId = args.<String>getOne("area_id").get();

        // エリアがバンクにロードされていない場合は例外になってこれ以上続行されない
        WorldTagArea loadedArea = CreatorModule.getOrCreateBankOf(src).getAreaOrThrow();

        Optional<SpleefStageTemplate> templateOptional = CMcGamePlugin.getSpleef().getStageTemplate(areaId);

        if (!templateOptional.isPresent()) {
            throw new CommandException(
                    Text.of(TextColors.RED, "✗そのようなステージIDは見つかりませんでした。"), false);
        }

        SpleefStageTemplate template = templateOptional.get();

        DualConsumer<SpleefStageTemplate, WorldTagArea> setter = this.setter.get(areaId);

        if (setter == null) {
            throw new CommandException(
                    Text.of(TextColors.RED, "✗入力されたエリアIDは間違っています"), false);
        }

        // コマンドは正常に実行され、エリアが設定された
        src.sendMessage(Text.of(TextColors.AQUA, "✓バンクからエリアを設定しました。"));
        return CommandResult.success();
    }
}
