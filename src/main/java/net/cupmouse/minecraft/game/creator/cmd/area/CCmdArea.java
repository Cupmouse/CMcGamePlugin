package net.cupmouse.minecraft.game.creator.cmd.area;

import net.cupmouse.minecraft.game.creator.CreatorBank;
import net.cupmouse.minecraft.game.creator.CreatorModule;
import net.cupmouse.minecraft.game.creator.cmd.spleef.CCmdSpleefSetarea;
import net.cupmouse.minecraft.worlds.WorldTagAreaSquare;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import static org.spongepowered.api.command.args.GenericArguments.none;

public final class CCmdArea implements CommandExecutor {

    public static final CommandCallable CALLABLE = CommandSpec.builder()
            .arguments(none())
            .child(CCmdAreaShow.CALLABLE, "show", "s")
            .executor(new CCmdArea())
            .build();

    private CCmdArea() {
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        CreatorBank session = CreatorModule.getOrCreateBankOf(src);

        WorldTagAreaSquare square = session.createAreaSquareOrThrow();

        session.setArea(square);

        src.sendMessage(Text.of(TextColors.GOLD, "✓バンクに選択されたエリアをロードしました"));
        return CommandResult.success();
    }
}
