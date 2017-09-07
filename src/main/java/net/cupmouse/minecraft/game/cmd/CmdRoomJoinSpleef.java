package net.cupmouse.minecraft.game.cmd;

import net.cupmouse.minecraft.game.CMcGamePlugin;
import net.cupmouse.minecraft.game.manager.GameException;
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

import static org.spongepowered.api.command.args.GenericArguments.integer;
import static org.spongepowered.api.command.args.GenericArguments.onlyOne;

public class CmdRoomJoinSpleef implements CommandExecutor {

    // TODO おすすめ一発参加とかしたい(占有率が60％以下の部屋に送る)
    public static final CommandCallable CALLABLE = CommandSpec.builder()
            .arguments(onlyOne(integer(Text.of("room_number"))))
            .executor(new CmdRoomJoinSpleef())
            .build();

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Player player;

        // TODO
        if (args.hasAny("player")) {
            // モデレータ権限でプレイヤーが指定された場合はそのプレイヤーを指定のルームに強制収容する。
            player = args.<Player>getOne("player").get();
        } else if (src instanceof Player) {
            // モデレータコマンドでもなくてプレイヤーが実行したならば、そのプレイヤーを指定のルームに入れる。
            player = (Player) src;
        } else {
            // モデレータコマンドでもなくプレイヤーが実行したのでもないなら終了
            throw new CommandException(Text.of("プレイヤーのみ実行可能"));
        }

        Integer roomNumber = args.<Integer>getOne("room_number").get();

        Optional<SpleefRoom> roomOptional = CMcGamePlugin.getSpleef().getRoomsByNumber(roomNumber);

        if (roomOptional.isPresent()) {
            SpleefRoom spleefRoom = roomOptional.get();

            try {
                spleefRoom.tryJoinRoom(player);
            } catch (GameException e) {
                throw new CommandException(Text.of(TextColors.RED, "✗参加できませんでした。", e.getText()), e, false);
            }

            return CommandResult.success();
        } else {
            throw new CommandException(
                    Text.of(TextColors.RED, "✗入力された部屋番号を探しましたが、見つかりませんでした"), false);
        }
    }
}
