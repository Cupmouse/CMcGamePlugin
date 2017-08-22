package net.cupmouse.minecraft.game.spleef;

// 今のところプレイヤーがステージのデフォルト設定を変更できない。
public class SpleefStageOptions {

    SpleefStageOptions() {
    }

    int gameTime;
    int minimumPlayerCount;

    public int getGameTime() {
        return gameTime;
    }

    public int getMinimumPlayerCount() {
        return minimumPlayerCount;
    }

}
