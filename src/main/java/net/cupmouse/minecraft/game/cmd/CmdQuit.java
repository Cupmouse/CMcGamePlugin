package net.cupmouse.minecraft.game.cmd;

import net.cupmouse.minecraft.game.CMcGamePlugin;
import net.cupmouse.minecraft.game.manager.GameRoomException;
import net.cupmouse.minecraft.game.spleef.SpleefRoom;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

import static org.spongepowered.api.command.args.GenericArguments.none;

public class CmdQuit implements CommandExecutor {

    public static final CommandCallable CALLABLE = CommandSpec.builder()
            .arguments(none())
            .executor(new CmdQuit())
            .build();

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (!(src instanceof Player)) {
            throw new CommandException(Text.of("プレイヤーのみ実行可能"));
        }

        Player player = (Player) src;

        Optional<SpleefRoom> roomPlayerJoin = CMcGamePlugin.getRoomPlayerJoin(player);

        if (roomPlayerJoin.isPresent()) {
            SpleefRoom spleefRoom = roomPlayerJoin.get();

            try {
                spleefRoom.tryLeaveRoom(player);
            } catch (GameRoomException e) {
                throw new CommandException(
                        Text.of(TextColors.RED, "✗何らかの原因で退出することができませんでした。")
                        , e, false);
            }

            return CommandResult.success();
        } else {
            throw new CommandException(Text.of(TextColors.RED, "✗あなたはどの部屋にも入室していません。"), false);
        }
    }
}
