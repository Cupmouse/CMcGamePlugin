package net.cupmouse.minecraft.game.creator.cmd.spleef;

import net.cupmouse.minecraft.game.CMcGamePlugin;
import net.cupmouse.minecraft.game.creator.CreatorBank;
import net.cupmouse.minecraft.game.creator.CreatorModule;
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

import java.util.Optional;

import static org.spongepowered.api.command.args.GenericArguments.onlyOne;
import static org.spongepowered.api.command.args.GenericArguments.string;

public class CCmdSpleefSelect implements CommandExecutor {

    public static final CommandCallable CALLABLE = CommandSpec.builder()
            .arguments(onlyOne(string(Text.of("template_id"))))
            .executor(new CCmdSpleefSelect())
            .build();

    private CCmdSpleefSelect() {
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        String templateId = args.<String>getOne("template_id").get();

        CreatorBank bank = CreatorModule.getOrCreateBankOf(src);

        Optional<SpleefStageTemplate> template = CMcGamePlugin.getSpleef().getStageTemplate(templateId);

        if (!template.isPresent()) {
            throw new CommandException(Text.of(TextColors.RED, "✗テンプレートIDが見つかりませんでした"));
        }

        bank.setSpleefSelectedTemplate(template.get());

        src.sendMessage(Text.of(TextColors.GOLD, String.format("✓テンプレート%sを選択しました", templateId)));
        return CommandResult.success();
    }
}
