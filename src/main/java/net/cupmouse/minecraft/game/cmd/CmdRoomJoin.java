package net.cupmouse.minecraft.game.cmd;

import net.cupmouse.minecraft.game.GameType;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.spec.CommandSpec;

public class CmdRoomJoin {

    public static final CommandCallable CALLABLE = CommandSpec.builder()
            .child(CmdRoomJoinSpleef.CALLABLE, GameType.SPLEEF.aliases)
            .build();
}
