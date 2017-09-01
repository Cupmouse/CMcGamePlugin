package net.cupmouse.minecraft.game.spleef;

import org.spongepowered.api.text.Text;

public class SpleefClockGame implements SpleefClock {

    private int gameTime;

    public SpleefClockGame(int gameTime) {
        this.gameTime = gameTime;
    }

    @Override
    public void clockTick(SpleefMatch match, int ctickLeft) {

        if (ctickLeft <= 0) {
            // 試合を終了する
            match.finish();
        } else if (ctickLeft <= 10) {
            // 十秒以内で毎秒カウントダウン
            match.messageChannel.send(Text.of("あと残り" + ctickLeft + "秒"));

        } else if (ctickLeft <= 30) {
            if (ctickLeft % 10 == 0) {
                // 30秒以内なら10秒づつカウントダウン
                match.messageChannel.send(Text.of("あと残り" + ctickLeft + "秒"));
            }
        } else if (ctickLeft % 60 == 0) {
            // それ以上のときは１分づつカウントダウン
            match.messageChannel.send(Text.of("あと残り" + ctickLeft + "秒"));
        }
    }

    @Override
    public int getInitialClockTick() {
        return 1;
    }
}
