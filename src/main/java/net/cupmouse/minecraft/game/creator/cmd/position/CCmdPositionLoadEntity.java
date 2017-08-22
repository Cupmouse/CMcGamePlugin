package net.cupmouse.minecraft.game.creator.cmd.position;

import net.cupmouse.minecraft.game.creator.CreatorModule;
import net.cupmouse.minecraft.worlds.WorldTagRocation;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import static org.spongepowered.api.command.args.GenericArguments.onlyOne;

public class CCmdPositionLoadEntity implements CommandExecutor {

    public static final CommandCallable CALLABLE = CommandSpec.builder()
            .arguments(onlyOne(GenericArguments.entity(Text.of("entity"))))
            .executor(new CCmdPositionLoadEntity())
            .build();

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Entity entity = args.<Entity>getOne("entity").get();

        CreatorModule.getOrCreateBankOf(src).loadedLoc = WorldTagRocation.fromEntity(entity);

        src.sendMessage(Text.of(TextColors.AQUA, "✓選択された回転情報つきロケーションをロードしました。"));
        return CommandResult.success();
    }
}
