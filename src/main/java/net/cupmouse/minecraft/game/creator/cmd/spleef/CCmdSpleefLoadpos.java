package net.cupmouse.minecraft.game.creator.cmd.spleef;

import net.cupmouse.minecraft.game.creator.CreatorBank;
import net.cupmouse.minecraft.game.creator.CreatorModule;
import net.cupmouse.minecraft.game.spleef.SpleefStage;
import net.cupmouse.minecraft.game.spleef.SpleefStageTemplate;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import static org.spongepowered.api.command.args.GenericArguments.onlyOne;
import static org.spongepowered.api.command.args.GenericArguments.string;

public class CCmdSpleefLoadpos implements CommandExecutor {

    public static final CommandCallable CALLABLE = CommandSpec.builder()
            .arguments(onlyOne(string(Text.of("position_id"))))
            .executor(new CCmdSpleefLoadpos())
            .build();

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        String positionId = args.<String>getOne("position_id").get();

        CreatorBank bank = CreatorModule.getOrCreateBankOf(src);

        if (positionId.startsWith("spawn.")) {
            // スポーンをロード

            int number;

            try {
                number = Integer.parseInt(positionId.substring("spawn.".length()));
            } catch (NumberFormatException e) {
                throw new CommandException(
                        Text.of(TextColors.RED, "✗スポーン番号を正しく入力して下さい。"), false);
            }

            SpleefStageTemplate template = bank.getSpleefSelectedTemplateOrThrow();
            bank.setLocation(template.getRelativeSpawnRocations().get(number).relativeBasePoint(((SpleefStageTemplate) template).));
        } else {
            // TODO
        }

        src.sendMessage(Text.of(TextColors.AQUA, "✓ロードしました。"));
        return CommandResult.success();
    }
}
