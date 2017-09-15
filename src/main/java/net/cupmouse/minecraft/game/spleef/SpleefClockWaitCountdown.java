package net.cupmouse.minecraft.game.spleef;

import org.spongepowered.api.boss.BossBarColors;
import org.spongepowered.api.boss.ServerBossBar;
import org.spongepowered.api.text.Text;

public final class SpleefClockWaitCountdown implements SpleefClock {

    @Override
    public void clockTick(SpleefMatch match, int ctickLeft) {
        if (ctickLeft != 0) {
            ServerBossBar bossBar = match.getBossBar();
            bossBar.setName(Text.of("プレイヤーの参加を待っています... ", ctickLeft));
            bossBar.setColor(BossBarColors.BLUE);
            bossBar.setPercent(ctickLeft / ((float) getInitialClockTick()));
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
