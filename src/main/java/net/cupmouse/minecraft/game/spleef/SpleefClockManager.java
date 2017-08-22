package net.cupmouse.minecraft.game.spleef;

import net.cupmouse.minecraft.CMcCore;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;

/**
 * Runnableですが、時計は自分で管理するので、外からSpongeスケジューラに登録しないでください。
 */
public final class SpleefClockManager implements Runnable {

    private final SpleefRoom room;
    // これは実際の時間
    private long prevTickTimeMilli;
    private Task task;

    private SpleefClock clock;
    // これはマインクラフトのティックから生み出される時間、Clock Tickと呼ぶ。よって実際の時間とは少しずれる
    // 現実時間と比べると小数点繰り上げになる
    private int ctickLeft;

    SpleefClockManager(SpleefRoom room) {
        this.room = room;
    }

    public int getClockTickLeft() {
        return ctickLeft;
    }

    /**
     * 時計を作動させます。すでに時計が動いている場合エラーになります。
     */
    void start() {
        if (task != null) {
            throw new IllegalStateException();
        }

        this.ctickLeft = clock.getInitialClockTick();
        // 初回だけ一回実行してほしいので、一秒減らす
        this.prevTickTimeMilli = System.currentTimeMillis() - 1;

        this.task = Sponge.getScheduler().createTaskBuilder()
                .intervalTicks(1)
                .execute(this).submit(CMcCore.getPlugin());
    }

    /**
     * この関数は、時計をリセットするだけです。ゲームには何も影響を与えないので注意してください。
     * また、時計はサーバースレッドで動いているので、時計を更新中にこの関数を実行することはできないので安心してください。
     * 設定された時計は削除されます。
     */
    void cancel() {
        if (task == null) {
            return;
        }

        this.task.cancel();
        this.task = null;
    }

    /**
     * 時計を設定しますが、実行しません。{@link #start()}で実行します。
     * また、今動いている時計は{@link #cancel()}が自動で呼ばれ停止した後、削除されます。
     * @param clock
     */
    void setClock(SpleefClock clock) {
        cancel();
        this.clock = clock;
    }

    public SpleefClock getClock() {
        return clock;
    }

    @Override
    public void run() {
        // ここにクッションを設けるのは、サーバーのティックが追いつかなくてどんどん遅れた場合に、ゲームの実際の時間が
        // 現実世界とずれてしまうので、ここでそれを検出して、現実時間となるべくずれが無いようにするため。

        // 前回からいくら時間が経過したか確認する
        long currentTimeMillis = System.currentTimeMillis();
        long elapsedMilliAfterPrevTick = currentTimeMillis - prevTickTimeMilli;
        CMcCore.getLogger().debug(Long.toString(elapsedMilliAfterPrevTick));

        // 一秒以上の判定をする。
        if (elapsedMilliAfterPrevTick >= 1000) {
            // 経過した秒だけ実行する。
            for (int i = 0; i < elapsedMilliAfterPrevTick / 1000; i++) {
                CMcCore.getLogger().debug("DO/" + i);
                clock.clockTick(room, ctickLeft);

                // 1減らす
                ctickLeft--;
            }

            // 更に、ここで不用意にそのままの時間を設定してはならない。もし、2.5秒経過していてrunが呼ばれたとしたら
            // 0.5秒分はどこへ行くのか。それを修正する。
            this.prevTickTimeMilli = currentTimeMillis - (elapsedMilliAfterPrevTick % 1000);

        } else {
            // 一秒に満たしていなかったらやめる
            return;
        }
    }
}