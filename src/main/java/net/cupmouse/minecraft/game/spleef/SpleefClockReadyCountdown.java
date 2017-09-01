package net.cupmouse.minecraft.game.spleef;

import org.spongepowered.api.text.Text;

public class SpleefClockReadyCountdown implements SpleefClock {

    @Override
    public void clockTick(SpleefMatch match, int ctickLeft) {
        if (ctickLeft > 5) {
            match.messageChannel.send(Text.of("試合開始まであと", ctickLeft, "秒..."));
        } else if (ctickLeft == 5) {
            match.messageChannel.send(Text.of("Get ready..."));
        } else if (ctickLeft == 2) {
            match.messageChannel.send(Text.of("Set..."));
        }

        if (ctickLeft <= 0) {
            match.start();
        }
    }

    @Override
    public int getInitialClockTick() {
        return 10;
    }
}
