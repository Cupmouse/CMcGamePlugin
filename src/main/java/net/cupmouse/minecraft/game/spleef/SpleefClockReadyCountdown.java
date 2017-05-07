package net.cupmouse.minecraft.game.spleef;

import net.cupmouse.minecraft.game.manager.GameRoomState;
import org.spongepowered.api.text.Text;

public class SpleefClockReadyCountdown implements SpleefClock {

    @Override
    public void clockTick(SpleefRoom room, int ctickLeft) {
        room.messageChannel.send(Text.of("ready_countdown ", ctickLeft));

        if (ctickLeft <= 0) {
            room.startGame();
        }
    }

    @Override
    public int getInitialClockTick() {
        return 10;
    }
}
