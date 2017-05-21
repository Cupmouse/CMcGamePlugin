package net.cupmouse.minecraft.game.cmd;

import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.spec.CommandSpec;

public class CmdSpleef {

    public static final CommandCallable CALLABLE = CommandSpec.builder()
            .child(CmdSpleefJoin.CALLABLE, "join", "j")
            .build();

}
