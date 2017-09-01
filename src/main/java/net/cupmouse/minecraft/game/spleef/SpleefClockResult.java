package net.cupmouse.minecraft.game.spleef;

import org.spongepowered.api.text.Text;

public class SpleefClockResult implements SpleefClock {

    @Override
    public void clockTick(SpleefMatch match, int ctickLeft) {
        match.messageChannel.send(Text.of("次の試合まで", ctickLeft, "秒..."));

        if (ctickLeft <= 0) {
            match.getRoom().nextMatch();
        }
    }

    @Override
    public int getInitialClockTick() {
        return 10;
    }
}
