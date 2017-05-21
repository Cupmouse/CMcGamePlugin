package net.cupmouse.minecraft.game.creator.cmd.area;

import net.cupmouse.minecraft.game.GameType;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.spec.CommandSpec;

public final class CCmdAreaLoad {

    public static final CommandCallable CALLABLE = CommandSpec.builder()
            .child(CCmdAreaLoadSpleef.CALLABLE, GameType.SPLEEF.aliases)
            .build();

    private CCmdAreaLoad() {
    }
}