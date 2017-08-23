package net.cupmouse.minecraft.game.mod;

import net.cupmouse.minecraft.game.CMcGamePlugin;
import net.cupmouse.minecraft.game.creator.CreatorModule;
import net.cupmouse.minecraft.game.manager.GameException;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import static org.spongepowered.api.command.args.GenericArguments.*;

public class CCmdSpleefRoomCreate implements CommandExecutor {

    public static final CommandCallable CALLABLE = CommandSpec.builder()
            .arguments(onlyOne(integer(Text.of("room_number"))),
                    onlyOne(string(Text.of("template_id"))))
            .executor(new CCmdSpleefRoomCreate())
            .build();

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Integer roomNumber = args.<Integer>getOne("room_number").get();
        String templateId = args.<String>getOne("template_id").get();

        Location<World> relativeBase = CreatorModule.getOrCreateBankOf(src).getRelativeBaseOrThrow();

        try {
            CMcGamePlugin.getSpleef().newRoom(roomNumber, templateId, relativeBase);
        } catch (GameException e) {
            throw new CommandException(Text.of(TextColors.RED, "✗ステージを作成できませんでした。", e.getText()),
                    e, false);
        }

        src.sendMessage(Text.of(TextColors.GOLD, "✓ステージID", templateId,"のSpleefステージを作成しました。"));
        return CommandResult.success();
    }
}
