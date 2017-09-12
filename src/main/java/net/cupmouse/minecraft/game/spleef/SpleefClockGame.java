package net.cupmouse.minecraft.game.spleef;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
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
        } else if (ctickLeft <= 10) {
            // 十秒以内で毎秒カウントダウン
            match.messageChannel.send(Text.of("あと残り" + ctickLeft + "秒"));

        } else if (ctickLeft <= 30) {
            if (ctickLeft % 10 == 0) {
                // 30秒以内なら10秒づつカウントダウン
                match.messageChannel.send(Text.of("あと残り" + ctickLeft + "秒"));
            }
        } else if (ctickLeft % 60 == 0) {
            // それ以上のときは１分づつカウントダウン
            match.messageChannel.send(Text.of("あと残り" + ctickLeft + "秒"));
        } else if (ctickLeft == gameTime) {
            for (SpleefPlayer spleefPlayer : match.getPlayers()) {
                Player player = Sponge.getServer().getPlayer(spleefPlayer.playerUUID).get();
                player.sendTitle(Title.of(Text.EMPTY, Text.of(TextColors.LIGHT_PURPLE, "Go!")));
            }
        }

        match.doItemTick(ctickLeft);
    }

    @Override
    public int getInitialClockTick() {
        return gameTime;
    }
}
