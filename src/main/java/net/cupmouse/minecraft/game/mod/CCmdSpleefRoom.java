package net.cupmouse.minecraft.game.mod;

import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.spec.CommandSpec;

public class CCmdSpleefRoom {

    public static final CommandCallable CALLABLE = CommandSpec.builder()
            .child(CCmdSpleefRoomCreate.CALLABLE, "create", "c")
            .child(CCmdSpleefRoomRemove.CALLABLE, "remove", "r")
            .build();
}
