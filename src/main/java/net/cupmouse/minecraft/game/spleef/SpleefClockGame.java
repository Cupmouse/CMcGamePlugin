package net.cupmouse.minecraft.game.spleef;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.boss.BossBarColor;
import org.spongepowered.api.boss.BossBarColors;
import org.spongepowered.api.boss.ServerBossBar;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.title.Title;

public class SpleefClockGame implements SpleefClock {

    private int gameTime;

    public SpleefClockGame(int gameTime) {
        this.gameTime = gameTime;
    }

    @Override
    public void clockTick(SpleefMatch match, int ctickLeft) {
        if (ctickLeft <= 0) {
            // 試合を終了する
            match.finish();
            return;
        } else {
            BossBarColor color;

            if (ctickLeft <= 10) {
                color = BossBarColors.RED;
            } else if (ctickLeft <= 30) {
                color = BossBarColors.YELLOW;
            } else {
                color = BossBarColors.GREEN;
            }

            ServerBossBar bossBar = match.getBossBar();
            bossBar.setName(Text.of("あと残り" + ctickLeft + "秒"));
            bossBar.setColor(color);
            bossBar.setPercent(ctickLeft / ((float) gameTime));
        }

        match.doItemTick(ctickLeft);

    }

    @Override
    public int getInitialClockTick() {
        return gameTime;
    }
}
