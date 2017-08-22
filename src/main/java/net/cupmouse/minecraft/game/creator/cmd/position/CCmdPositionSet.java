package net.cupmouse.minecraft.game.creator.cmd.position;

import net.cupmouse.minecraft.game.GameType;
import net.cupmouse.minecraft.game.creator.cmd.spleef.CCmdSpleefSetpos;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.spec.CommandSpec;

public class CCmdPositionSet {

    public static final CommandCallable CALLABLE = CommandSpec.builder()
            .child(CCmdSpleefSetpos.CALLABLE, GameType.SPLEEF.aliases)
            .build();
}
