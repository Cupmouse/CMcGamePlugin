package net.cupmouse.minecraft.game.creator.cmd.spleef;

import net.cupmouse.minecraft.game.CMcGamePlugin;
import net.cupmouse.minecraft.game.manager.GameException;
import net.cupmouse.minecraft.game.spleef.SpleefStageTemplate;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

import static org.spongepowered.api.command.args.GenericArguments.onlyOne;
import static org.spongepowered.api.command.args.GenericArguments.string;
import static org.spongepowered.api.text.Text.of;

public class CCmdSpleefNew implements CommandExecutor {

    public static final CommandCallable CALLABLE = CommandSpec.builder()
            .arguments(onlyOne(string(Text.of("template_id"))))
            .executor(new CCmdSpleefNew())
            .build();

    private CCmdSpleefNew() {
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        String templateId = args.<String>getOne("template_id").get();

        try {
            SpleefStageTemplate template = CMcGamePlugin.getSpleef().newStageTemplate(templateId);
        } catch (GameException e) {
            throw new CommandException(e.getText(), false);
        }

        src.sendMessage(Text.of(TextColors.GOLD,
                String.format("✓新しいテンプレート%sを作成しました", templateId)));
        return CommandResult.success();
    }
}
