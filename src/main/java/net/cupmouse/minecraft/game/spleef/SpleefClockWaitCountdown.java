package net.cupmouse.minecraft.game.spleef;

import org.spongepowered.api.text.Text;

public final class SpleefClockWaitCountdown implements SpleefClock {

    @Override
    public void clockTick(SpleefRoom room, int ctickLeft) {
        room.messageChannel.send(Text.of("wait_countdown ", ctickLeft));

        if (ctickLeft <= 0) {
            room.ready();
        }
    }

    @Override
    public int getInitialClockTick() {
        return 20;
    }
}
