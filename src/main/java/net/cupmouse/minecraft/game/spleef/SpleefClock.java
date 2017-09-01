package net.cupmouse.minecraft.game.spleef;

/**
 * 注意！この時計は0になってもctickLeftをどんどん減らしていきます。マイナスになります。
 * 0で次の処理に移りたいときは、0で動作を起こすようにして下さい。
 */
public interface SpleefClock {

    /**
     * この関数では現実世界の時間の経過を気にせずにClock Tickだけで物事をすすめる事ができる。
     */
    void clockTick(SpleefMatch match, int ctickLeft);

    int getInitialClockTick();
}
