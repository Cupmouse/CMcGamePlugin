package net.cupmouse.minecraft.game.mod;

import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.spec.CommandSpec;

public class MCmdSpleefRoom {

    public static final CommandCallable CALLABLE = CommandSpec.builder()
            .child(MCmdSpleefRoomCreate.CALLABLE, "create", "c")
            .child(MCmdSpleefRoomRemove.CALLABLE, "remove", "r")
            .build();
}
