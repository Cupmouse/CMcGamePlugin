package net.cupmouse.minecraft.game.cmd;

import net.cupmouse.minecraft.game.GameType;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.spec.CommandSpec;

public class CmdRoomList {

    public static final CommandCallable CALLABLE = CommandSpec.builder()
            .child(CmdRoomListSpleef.CALLABLE, GameType.SPLEEF.aliases)
            .build();
}
