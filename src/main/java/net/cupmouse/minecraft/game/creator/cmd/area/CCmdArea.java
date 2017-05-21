package net.cupmouse.minecraft.game.creator.cmd.area;

import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.spec.CommandSpec;

public final class CCmdArea {

    public static final CommandCallable CALLABLE = CommandSpec.builder()
            .child(CCmdAreaLoad.CALLABLE, "load", "l")
            .child(CCmdAreaShow.CALLABLE, "show", "s")
            .child(CCmdAreaSet.CALLABLE, "set")
            .build();

    private CCmdArea() {
    }
}
