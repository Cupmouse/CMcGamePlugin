package net.cupmouse.minecraft.game.creator.cmd.spleef;

import net.cupmouse.minecraft.game.creator.CreatorBank;
import net.cupmouse.minecraft.game.creator.CreatorModule;
import net.cupmouse.minecraft.game.spleef.SpleefStageTemplate;
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
import java.util.function.BiConsumer;

import static org.spongepowered.api.command.args.GenericArguments.onlyOne;
import static org.spongepowered.api.command.args.GenericArguments.string;
// TODO
public class CCmdSpleefSetarea implements CommandExecutor {

    public static final CommandCallable CALLABLE = CommandSpec.builder()
            .arguments(onlyOne(string(Text.of("area_id"))))
            .executor(new CCmdSpleefSetarea())
            .build();

    private Map<String, BiConsumer<SpleefStageTemplate, WorldTagArea>> setters = new HashMap<>();

    private CCmdSpleefSetarea() {
        BiConsumer<SpleefStageTemplate, WorldTagArea> fightingAreaSetter = SpleefStageTemplate::setFightingArea;
        this.setters.put("f", fightingAreaSetter);
        this.setters.put("fighting", fightingAreaSetter);

        BiConsumer<SpleefStageTemplate, WorldTagArea> groundAreaSetter = SpleefStageTemplate::setGroundArea;
        this.setters.put("g", groundAreaSetter);
        this.setters.put("ground", groundAreaSetter);

        BiConsumer<SpleefStageTemplate, WorldTagArea> setSpectatorAreaSetter = SpleefStageTemplate::setSpectatorArea;
        this.setters.put("s", setSpectatorAreaSetter);
        this.setters.put("spectator", setSpectatorAreaSetter);
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        String areaId = args.<String>getOne("area_id").get();

        // ????????????????????????????????????????????????????????????????????????????????????????????????????????????
        CreatorBank bank = CreatorModule.getOrCreateBankOf(src);
        WorldTagArea loadedArea = bank.getAreaOrThrow();
        SpleefStageTemplate template = bank.getSpleefSelectedTemplateOrThrow();

        BiConsumer<SpleefStageTemplate, WorldTagArea> setter = this.setters.get(areaId);

        if (setter == null) {
            throw new CommandException(
                    Text.of(TextColors.RED, "???????????????????????????ID????????????????????????"), false);
        }

        setter.accept(template, loadedArea);

        // ??????????????????????????????????????????????????????????????????
        src.sendMessage(Text.of(TextColors.GOLD, String.format("??????????????????????????????%s?????????????????????", areaId)));
        return CommandResult.success();
    }
}
