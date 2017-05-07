package net.cupmouse.minecraft.game.creator.command;

import net.cupmouse.minecraft.game.CMcGamePlugin;
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
import java.util.function.Supplier;

public class CommandElementGameType extends CommandElement {

    // TODO ここをこうしんしよう
    public static final Map<String, Supplier<GameManager<? extends GameRoom>>> GAME_TYPES_MAP = new HashMap<>();
    static {
        GAME_TYPES_MAP.put("spleef", CMcGamePlugin::getSpleef);
    }
    public static final List<String> GAME_TYPES_LIST = new ArrayList<>(GAME_TYPES_MAP.keySet());

    public CommandElementGameType(@Nullable Text key) {
        super(key);
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        String next = args.next();

        return GAME_TYPES_MAP.get(next);
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        return GAME_TYPES_LIST;
    }
}
