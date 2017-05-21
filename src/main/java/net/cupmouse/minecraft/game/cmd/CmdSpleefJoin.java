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
import org.spongepowered.api.text.format.TextColors;
import sun.security.pkcs.SignerInfo;

import java.util.Optional;

import static org.spongepowered.api.command.args.GenericArguments.integer;
import static org.spongepowered.api.command.args.GenericArguments.onlyOne;

public class CmdSpleefJoin implements CommandExecutor {

    // TODO おすすめ一発参加とかしたい(占有率が60％以下の部屋に送る)
    public static final CommandCallable CALLABLE = CommandSpec.builder()
            .arguments(onlyOne(integer(Text.of("room_number"))))
            .executor(new CmdSpleefJoin())
            .build();

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (!(src instanceof Player)) {
            throw new CommandException(Text.of("プレイヤーのみ実行可能"));
        }

        Player player = (Player) src;

        Integer roomNumber = args.<Integer>getOne("room_number").get();

        Optional<SpleefRoom> roomOptional = CMcGamePlugin.getSpleef().getRoom(roomNumber);

        if (roomOptional.isPresent()) {
            SpleefRoom spleefRoom = roomOptional.get();

            try {
                spleefRoom.tryJoinRoom(player);
            } catch (GameRoomException e) {
                throw new CommandException(Text.of(TextColors.RED, "✗参加できませんでした。"), e, false);
            }

            return CommandResult.success();
        } else {
            throw new CommandException(
                    Text.of(TextColors.RED, "✗入力された部屋番号を探しましたが、見つかりませんでした。"), false);
        }
    }
}
