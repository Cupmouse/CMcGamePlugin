package net.cupmouse.minecraft.game.creator.cmd.area;

import net.cupmouse.minecraft.game.GameType;
import net.cupmouse.minecraft.game.creator.CreatorModule;
import net.cupmouse.minecraft.game.creator.CreatorSessionInfo;
import net.cupmouse.minecraft.worlds.WorldTagAreaSquare;
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

import static org.spongepowered.api.command.args.GenericArguments.none;

public final class CCmdAreaLoad implements CommandExecutor {

    public static final CommandCallable CALLABLE = CommandSpec.builder()
            .arguments(none())
            .child(CCmdAreaLoadSpleef.CALLABLE, GameType.SPLEEF.aliases)
            .executor(new CCmdAreaLoad())
            .build();

    private CCmdAreaLoad() {
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        CreatorSessionInfo session = CreatorModule.getOrCreateSession(src);

        Optional<WorldTagAreaSquare> squareOpt = session.createAreaSquare();
        if (!squareOpt.isPresent()) {
            // これがでる要因は、ワールドタグが設定されていない場合もある。
            throw new CommandException(Text.of(TextColors.RED, "✗エリアが選択されていません。"), false);
        }

        session.loadedArea = squareOpt.get();
        src.sendMessage(Text.of(TextColors.AQUA, "✓選択されたエリアをロードしました。"));
        return CommandResult.success();
    }
}