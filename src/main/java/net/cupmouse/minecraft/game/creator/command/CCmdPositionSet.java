package net.cupmouse.minecraft.game.creator.command;

import net.cupmouse.minecraft.game.GameType;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.spec.CommandSpec;

public class CCmdPositionSet {

    public static final CommandCallable CALLABLE = CommandSpec.builder()
            .child(CCmdPositionSetSpleef.CALLABLE, GameType.SPLEEF.aliases)
            .build();
}
