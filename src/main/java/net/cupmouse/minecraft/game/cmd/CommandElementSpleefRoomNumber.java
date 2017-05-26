package net.cupmouse.minecraft.game.cmd;

import net.cupmouse.minecraft.game.CMcGamePlugin;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import javax.annotation.Nullable;
import java.util.List;

// TODO これいならい
public class CommandElementSpleefRoomNumber extends CommandElement {

    protected CommandElementSpleefRoomNumber(@Nullable Text key) {
        super(key);
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        String next = args.next();

        try {
            int anInt = Integer.parseInt(next);
        } catch (NumberFormatException e) {
            throw new ArgumentParseException(Text.of(TextColors.RED, "✗自然数を入力してください！"), e, next, 0);
        }

//        CMcGamePlugin.getSpleef().getRoom()

        return null;
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        return null;
    }
}
