package net.cupmouse.minecraft.game.creator.cmd.spleef;

import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.spec.CommandSpec;

public class CCmdSpleefStage {

    public static final CommandCallable CALLABLE = CommandSpec.builder()
            .child(CCmdSpleefStageOption.CALLABLE, "option", "o")
            .build();
}
