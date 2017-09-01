package net.cupmouse.minecraft.game.spleef;

import org.spongepowered.api.text.Text;

public final class SpleefClockWaitCountdown implements SpleefClock {

    @Override
    public void clockTick(SpleefMatch match, int ctickLeft) {
        if ((ctickLeft > 5 && ctickLeft % 5 == 0) || ctickLeft <= 5) {
            match.messageChannel.send(Text.of("プレイヤーの参加を待っています... ", ctickLeft));
        }

        if (ctickLeft <= 0) {
            match.ready();
        }
    }

    @Override
    public int getInitialClockTick() {
        return 10;
    }
}
