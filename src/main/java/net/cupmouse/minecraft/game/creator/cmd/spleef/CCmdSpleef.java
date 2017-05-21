package net.cupmouse.minecraft.game.creator.cmd.spleef;

import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.spec.CommandSpec;

public class CCmdSpleef {

    public static final CommandCallable CALLABLE = CommandSpec.builder()
            .child(CCmdSpleefStage.CALLABLE, "stage", "s")
            .build();
}
