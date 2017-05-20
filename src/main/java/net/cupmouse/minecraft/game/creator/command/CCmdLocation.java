package net.cupmouse.minecraft.game.creator.command;

import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.spec.CommandSpec;

public final class CCmdLocation {

    public static final CommandCallable CALLABLE = CommandSpec.builder()
            .child(CCmdLocationLoad.CALLABLE, "load", "l")
            .child(CCmdLocationShow.CALLABLE, "show", "show")
            .build();

    private CCmdLocation() {
    }
}
