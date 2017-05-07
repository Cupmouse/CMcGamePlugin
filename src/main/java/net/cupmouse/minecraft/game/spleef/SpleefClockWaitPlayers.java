package net.cupmouse.minecraft.game.spleef;

import org.spongepowered.api.text.Text;

public class SpleefClockWaitPlayers implements SpleefClock {

    @Override
    public void clockTick(SpleefRoom room, int ctickLeft) {
        room.messageChannel.send(Text.of("waitplayers ", ctickLeft));

        if (ctickLeft == 0) {
            room.startCountdown();
        }
    }
}
