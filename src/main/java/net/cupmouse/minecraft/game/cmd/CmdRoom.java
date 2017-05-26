package net.cupmouse.minecraft.game.cmd;

import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;

import static org.spongepowered.api.command.args.GenericArguments.onlyOne;

public class CmdRoom {

    public static final CommandCallable CALLABLE = CommandSpec.builder()
            .child(CmdRoomList.CALLABLE, "list", "l")
            .child(CmdRoomJoin.CALLABLE, "join", "j")
            .child(CmdRoomQuit.CALLABLE, "quit", "q")
            .build();
}
