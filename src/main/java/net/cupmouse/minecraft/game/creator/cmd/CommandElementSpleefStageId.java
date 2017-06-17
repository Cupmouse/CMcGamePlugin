package net.cupmouse.minecraft.game.creator.cmd;

import net.cupmouse.minecraft.game.CMcGamePlugin;
import net.cupmouse.minecraft.game.spleef.stage.SpleefStage;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CommandElementSpleefStageId extends CommandElement {

    protected CommandElementSpleefStageId(@Nullable Text key) {
        super(key);
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        String next = args.next();

        Optional<SpleefStage> stageOptional = CMcGamePlugin.getSpleef().getStage(next);

        if (!stageOptional.isPresent()) {
            throw new ArgumentParseException(
                    Text.of(TextColors.RED, "✗Spleefステージが見つかりませんでした。"), next, 0);
        }

        return stageOptional.get();
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        return new ArrayList<>(CMcGamePlugin.getSpleef().getStageIds());
    }
}
