package net.cupmouse.minecraft.game.spleef;

import org.spongepowered.api.boss.BossBarColors;
import org.spongepowered.api.boss.ServerBossBar;
import org.spongepowered.api.text.Text;

public class SpleefClockResult implements SpleefClock {

    @Override
    public void clockTick(SpleefMatch match, int ctickLeft) {
        if (ctickLeft != 0) {
            ServerBossBar bossBar = match.getBossBar();
            bossBar.setName(Text.of("次の試合まで", ctickLeft, "秒..."));
            bossBar.setColor(BossBarColors.BLUE);
            bossBar.setPercent(ctickLeft / ((float) getInitialClockTick()));
        }

        if (ctickLeft <= 0) {
            match.getRoom().nextMatch();
        }
    }

    @Override
    public int getInitialClockTick() {
        return 5;
    }
}
