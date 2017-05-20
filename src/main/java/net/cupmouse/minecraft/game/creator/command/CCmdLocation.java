package net.cupmouse.minecraft.game.creator.command;

import net.cupmouse.minecraft.game.CMcGamePlugin;
import net.cupmouse.minecraft.game.GameType;
import net.cupmouse.minecraft.game.spleef.SpleefRoom;
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

import java.util.Collection;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CCmdLocation implements CommandExecutor {

    public static CommandCallable CALLABLE = CommandSpec.builder()
            .arguments(GenericArguments.onlyOne(CCmdArguments.gameType(Text.of("game_type"))),
                    GenericArguments.onlyOne(GenericArguments.string(Text.of("lookup_id"))))
            .executor(new CCmdLocation())
            .build();

    private static final Pattern SPLEEF_LOOKUP_ID_REGEX
            = Pattern.compile("^([a-zA-Z\\d]+)[.](a-zA-Z\\d+)$");

    private CCmdLocation() {
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        GameType gameType = args.<GameType>getOne("game_type").get();
        String lookupId = args.<String>getOne("lookup_id").get();

        switch (gameType) {
            case SPLEEF:
                Matcher matcher = SPLEEF_LOOKUP_ID_REGEX.matcher(lookupId);
                if (!matcher.find()) {
                    break;
                }
                String stageName = matcher.group(1);

                Optional<SpleefRoom> roomOptional = CMcGamePlugin.getSpleef().getRoomOfStageId(stageName);

                if (roomOptional.isPresent()) {
                    src.sendMessage(Text.of(TextColors.RED, "✗そのようなステージIDは見つかりませんでした。"));
                    return CommandResult.empty();
                }

                SpleefRoom spleefRoom = roomOptional.get();


                String locationId = matcher.group(2);

                if (locationId.equals("spawn")) {
                    spleefRoom.stageSettings.spawnLocations.get()
                }

                spleefRoom.stageSettings.spawnLocations


                break;
        }

        return CommandResult.success();
    }
}
