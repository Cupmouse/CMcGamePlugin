package net.cupmouse.minecraft.game.spleef;

import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.explosive.PrimedTNT;

public interface SpleefItem {

    void init();

    /**
     *
     * @return falseで今後呼んでほしくない、trueで継続
     */
    boolean doTick();

    void clear();

    String getName();
}
