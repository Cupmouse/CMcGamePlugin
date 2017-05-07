package net.cupmouse.minecraft.game.spleef;

import org.spongepowered.api.text.Text;

public class SpleefClockGame implements SpleefClock {

    @Override
    public void clockTick(SpleefRoom room, int ctickLeft) {

        if (ctickLeft <= 0) {
            // ゲームを終了する
            room.finishGame();
        } else if (ctickLeft <= 10) {
            // 十秒以内で毎秒カウントダウン
            room.messageChannel.send(Text.of("あと残り" + ctickLeft + "秒"));

        } else if (ctickLeft <= 30) {
            if (ctickLeft % 10 == 0) {
                // 30秒以内なら10秒づつカウントダウン
                room.messageChannel.send(Text.of("あと残り" + ctickLeft + "秒"));
            }
        } else if (ctickLeft % 60 == 0) {
            // それ以上のときは１分づつカウントダウン
            room.messageChannel.send(Text.of("あと残り" + ctickLeft + "秒"));
        }
    }
}
