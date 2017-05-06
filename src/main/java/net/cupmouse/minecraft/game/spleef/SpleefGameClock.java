package net.cupmouse.minecraft.game.spleef;

import net.cupmouse.minecraft.CMcCore;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;

import java.util.concurrent.TimeUnit;

/**
 * Runnableですが、時計は自分で管理するので、外からSpongeスケジューラに登録しないでください。
 */
public final class SpleefGameClock implements Runnable {

    private final SpleefRoom room;
    private final int gameTime;
    // これは実際の時間
    private long prevTickTimeMilli;
    // これはマインクラフトのティックから生み出される時間、Clock Tickと呼ぶ。よって実際の時間とは少しずれる
    private int ctickLeft;
    private Task task;

    SpleefGameClock(SpleefRoom room, int gameTime) {
        this.room = room;
        this.gameTime = gameTime;
    }

    /**
     * 時計を作動させます。すでに時計が動いている場合エラーになります。
     */
    void start() {
        if (task != null) {
            throw new IllegalStateException();
        }

        this.ctickLeft = gameTime;
        this.prevTickTimeMilli = System.currentTimeMillis();
        this.task = Sponge.getScheduler().createTaskBuilder()
                .interval(1, TimeUnit.SECONDS)
                .execute(this).submit(CMcCore.getPlugin());
    }

    /**
     * この関数は、時計をリセットするだけです。ゲームには何も影響を与えないので注意してください。
     * また、時計はサーバースレッドで動いているので、時計を更新中にこの関数を実行することはできないので安心してください。
     */
    void reset() {
        this.task.cancel();
        this.task = null;
    }

    @Override
    public void run() {
        // ここにクッションを設けるのは、サーバーのティックが追いつかなくてどんどん遅れた場合に、ゲームの実際の時間が
        // 現実世界とずれてしまうので、ここでそれを検出して、現実時間となるべくずれが無いようにするため。

        // 前回からいくら時間が経過したか確認する
        long currentTimeMillis = System.currentTimeMillis();
        long elapsedMilliAfterPrevTick = prevTickTimeMilli - currentTimeMillis;
        CMcCore.getLogger().debug(Long.toString(elapsedMilliAfterPrevTick));

        // 一秒以上の判定をする。
        if (elapsedMilliAfterPrevTick >= 1000) {
            // 経過した秒だけ実行する。
            for (int i = 0; i < elapsedMilliAfterPrevTick / 1000; i++) {
                clockTick();
            }

            // 更に、ここで不用意にそのままの時間を設定してはならない。もし、2.5秒経過していてrunが呼ばれたとしたら
            // 0.5秒分はどこへ行くのか。それを修正する。
            this.prevTickTimeMilli = currentTimeMillis - (elapsedMilliAfterPrevTick % 1000);

        } else {
            // 一秒に満たしていなかったらやめる
            return;
        }
    }

    /**
     * ↑の処理によってこの関数では現実世界の時間の経過を気にせずにClock Tickだけで物事をすすめる事ができる。
     */
    private void clockTick() {
        ctickLeft--;

        if (ctickLeft <= 0) {
            // ゲームを終了する
            room.finishGame();
            // 時計をリセット
            reset();
        } else if (ctickLeft <= 10) {
            // 十秒以内で毎秒カウントダウン

        } else if (ctickLeft <= 30) {
            if (ctickLeft % 10 == 0) {
                // 30秒以内なら10秒づつカウントダウン
            }
        } else if (ctickLeft % 60 == 0) {
            // それ以上のときは１分づつカウントダウン
        }
    }
}