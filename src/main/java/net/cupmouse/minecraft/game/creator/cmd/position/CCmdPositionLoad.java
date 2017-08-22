package net.cupmouse.minecraft.game.creator.cmd.position;

import net.cupmouse.minecraft.game.GameType;
import net.cupmouse.minecraft.game.creator.cmd.spleef.CCmdSpleefLoadpos;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.spec.CommandSpec;

public final class CCmdPositionLoad {

    public static final CommandCallable CALLABLE = CommandSpec.builder()
            .child(CCmdPositionLoadEntity.CALLABLE, "entity", "e")
            .child(CCmdSpleefLoadpos.CALLABLE, GameType.SPLEEF.aliases)
            .build();

    private CCmdPositionLoad() {
    }
}
