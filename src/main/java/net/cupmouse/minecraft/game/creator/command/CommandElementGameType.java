package net.cupmouse.minecraft.game.creator.command;

import net.cupmouse.minecraft.game.manager.GameManager;
import net.cupmouse.minecraft.game.manager.GameRoom;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;
import java.util.*;

public class CommandElementGameType extends CommandElement {

    // TODO ここをこうしんしよう
    public static final Map<String, GameManager<? extends GameRoom>> GAME_TYPES_MAP = new HashMap<>();
    static {
        GAME_TYPES_MAP.put("", );
    }

    protected CommandElementGameType(@Nullable Text key) {
        super(key);
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        String next = args.next();




        return ;
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        return GAME_TYPES_MAP;
    }
}
