package net.cupmouse.minecraft.game.creator.cmd.position;

import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.spec.CommandSpec;

public final class CCmdPosition {

    public static final CommandCallable CALLABLE = CommandSpec.builder()
            .child(CCmdPositionLoad.CALLABLE, "load", "l")
            .child(CCmdPositionShow.CALLABLE, "show", "s")
            .child(CCmdPositionSet.CALLABLE, "set")
            .build();

    private CCmdPosition() {
    }
}
