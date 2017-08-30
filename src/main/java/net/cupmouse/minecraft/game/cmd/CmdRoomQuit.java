package net.cupmouse.minecraft.game.cmd;

import net.cupmouse.minecraft.game.CMcGamePlugin;
import net.cupmouse.minecraft.game.manager.GameException;
import net.cupmouse.minecraft.game.manager.GameRoom;
import net.cupmouse.minecraft.game.spleef.SpleefRoom;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

import static org.spongepowered.api.command.args.GenericArguments.*;

public class CmdRoomQuit implements CommandExecutor {

    public static final CommandCallable CALLABLE = CommandSpec.builder()
            .arguments(requiringPermission(optional(player(Text.of("player"))),
                    "cmcgame.mod.spleef.cmd.room.quit"))
            .executor(new CmdRoomQuit())
            .build();

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Player player;

        if (args.hasAny("player")) {
            // モデレータによって、プレイヤー名が指定されると、そのプレイヤーに現在入室中のルームを抜けさせる。
            player = args.<Player>getOne("player").get();
        } else if (src instanceof Player) {
            // プレイヤーが実行、モデレータコマンドでない
            player = ((Player) src);
        } else {
            // それ以外はプレイヤーではなく、モデレータコマンドでもないので、これ以上続けられない、終了。
            throw new CommandException(Text.of("プレイヤーのみ実行可能"));
        }

        Optional<GameRoom> roomPlayerJoin = CMcGamePlugin.getRoomPlayerJoin(player);

        if (roomPlayerJoin.isPresent()) {
            GameRoom room = roomPlayerJoin.get();

            try {
                room.tryLeaveRoom(player);
            } catch (GameException e) {
                throw new CommandException(
                        Text.of(TextColors.RED, "✗何らかの原因で退出することができませんでした。")
                        , e, false);
            }

            return CommandResult.success();
        } else {
            throw new CommandException(Text.of(TextColors.RED, "✗どの部屋にも入室していません。"), false);
        }
    }
}
