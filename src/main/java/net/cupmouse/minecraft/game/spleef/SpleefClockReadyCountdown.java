package net.cupmouse.minecraft.game.spleef;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.boss.BossBarColors;
import org.spongepowered.api.boss.ServerBossBar;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.title.Title;

public class SpleefClockReadyCountdown implements SpleefClock {

    @Override
    public void clockTick(SpleefMatch match, int ctickLeft) {
        if (ctickLeft != 0) {
            ServerBossBar bossBar = match.getBossBar();
            bossBar.setName(Text.of("試合開始まであと", ctickLeft, "秒..."));
            bossBar.setColor(BossBarColors.BLUE);
            bossBar.setPercent(ctickLeft / ((float) getInitialClockTick()));
        }

        if (ctickLeft == 4) {
            for (SpleefPlayer spleefPlayer : match.getPlayers()) {
                Player player = Sponge.getServer().getPlayer(spleefPlayer.playerUUID).get();
                player.sendTitle(Title.of(Text.EMPTY, Text.of(TextColors.GOLD, "Get ready")));
            }

        } else if (ctickLeft == 2) {
            for (SpleefPlayer spleefPlayer : match.getPlayers()) {
                Player player = Sponge.getServer().getPlayer(spleefPlayer.playerUUID).get();
                player.sendTitle(Title.of(Text.EMPTY, Text.of(TextColors.GOLD, "Set")));
            }
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
