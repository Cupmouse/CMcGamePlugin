package net.cupmouse.minecraft.game.spleef;

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
