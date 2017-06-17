package net.cupmouse.minecraft.game.spleef.stage;

import net.cupmouse.minecraft.game.manager.IntOptionId;
import ninja.leaping.configurate.objectmapping.Setting;

// 今のところプレイヤーがステージのデフォルト設定を変更できない。
public class SpleefStageOptions {

    @IntOptionId({"defaultGameTime", "dgt"})
    @Setting("default_game_time")
    private int defaultGameTime;

    public int getDefaultGameTime() {
        return defaultGameTime;
    }
}
