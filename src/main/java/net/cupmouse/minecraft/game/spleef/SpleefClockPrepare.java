package net.cupmouse.minecraft.game.spleef;

import org.spongepowered.api.text.Text;

public class SpleefClockPrepare implements SpleefClock {

    @Override
    public void clockTick(SpleefRoom room, int ctickLeft) {
        room.messageChannel.send(Text.of("prepare ", ctickLeft));

        if (ctickLeft <= 0) {
            if (room.tryHoldGame()) {
            }
        }
    }
}
