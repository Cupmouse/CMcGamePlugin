package net.cupmouse.minecraft.game.creator.command;

import net.cupmouse.minecraft.game.GameType;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;
import java.util.*;

public final class CommandElementGameType extends CommandElement {

    private ArrayList<String> gametypeStringList;

    public CommandElementGameType(@Nullable Text key) {
        super(key);
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        String next = args.next();

        return GameType.fromString(next);
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        if (gametypeStringList == null) {
            this.gametypeStringList = new ArrayList<>();
            GameType[] values = GameType.values();

            for (GameType value : values) {
                this.gametypeStringList.add(value.name());
            }
        }

        return gametypeStringList;
    }
}
