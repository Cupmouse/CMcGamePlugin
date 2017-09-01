package net.cupmouse.minecraft.game.spleef;

import net.cupmouse.minecraft.CMcCore;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * Runnableですが、時計は自分で管理するので、外からSpongeスケジューラに登録しないでください。
 */
public final class SpleefClockManager {

    private final SpleefMatch match;
    private SpleefClockExecutor executor;
    private Task task;

    SpleefClockManager(SpleefMatch match) {
        this.match = match;
    }

    /**
     * 時計を作動させます。すでに時計が動いている場合エラーになります。
     */
    void start() {
        if (task != null) {
            throw new IllegalStateException();
        }

        this.executor.init();

        this.task = Sponge.getScheduler().createTaskBuilder()
                .intervalTicks(1)
                .execute(executor)
                .submit(CMcCore.getPlugin());
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
        this.executor.cancelled = true;
        this.task = null;
    }

    /**
     * 時計を設定しますが、実行しません。{@link #start()}で実行します。
     * また、今動いている時計は{@link #cancel()}が自動で呼ばれ停止した後、削除されます。
     * @param clock
     */
    void setClock(SpleefClock clock) {
        cancel();
        this.executor = new SpleefClockExecutor(match, clock);
    }

    public static class SpleefClockExecutor implements Consumer<Task> {
        // これは実際の時間
        private long prevTickTimeMilli;
        // これはマインクラフトのティックから生み出される時間、Clock Tickと呼ぶ。よって実際の時間とは少しずれる
        // 現実時間と比べると小数点繰り上げになる
        private int ctickLeft;

        private SpleefMatch match;
        private SpleefClock clock;
        private boolean cancelled;

        private SpleefClockExecutor(SpleefMatch match, SpleefClock clock) {
            this.match = match;
            this.clock = clock;
            this.ctickLeft = clock.getInitialClockTick();
        }

        public void init() {
            // 初回だけ一回実行してほしいので、一秒減らす
            this.prevTickTimeMilli = System.currentTimeMillis() - 1000;
        }

        @Override
        public void accept(Task task) {
            // ここにクッションを設けるのは、サーバーのティックが追いつかなくてどんどん遅れた場合に、ゲームの実際の時間が
            // 現実世界とずれてしまうので、ここでそれを検出して、現実時間となるべくずれが無いようにするため。

            // 前回からいくら時間が経過したか確認する
            long currentTimeMillis = System.currentTimeMillis();
            long elapsedMilliAfterPrevTick = currentTimeMillis - prevTickTimeMilli;

            // 一秒以上の判定をする。
            if (elapsedMilliAfterPrevTick >= 1000) {
                // 経過した秒だけ実行する。
                for (int i = 0; i < elapsedMilliAfterPrevTick / 1000; i++) {
                    clock.clockTick(match, ctickLeft);

                    // clockTickを実行中にこのエグゼキューターがキャンセルされる場合があるのでチェックして、
                    // キャンセルされた場合はこれ以上実行されないようにこの関数を抜ける。抜けると二度と戻ってこない。
                    if (cancelled) {
                        return;
                    }

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
}