package net.cupmouse.minecraft.game.creator.cmd.spleef;

import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.spec.CommandSpec;

/**
 * Spleefのステージテンプレートを編集するための一連のコマンドです。
 */
public class CCmdSpleef {

    public static final CommandCallable CALLABLE = CommandSpec.builder()
            // 範囲をバンクに読み込む
            .child(CCmdSpleefLoadarea.CALLABLE, "loadarea", "la")
            // 範囲をテンプレートに設定する
            .child(CCmdSpleefSetarea.CALLABLE, "setarea", "sa")
            // 位置をバンクに読み込む
            .child(CCmdSpleefLoadpos.CALLABLE, "loadpos", "lp")
            // 位置を記録する
            .child(CCmdSpleefSetpos.CALLABLE, "setpos", "sp")
            // ステージテンプレートを選択する
            .child(CCmdSpleefSelect.CALLABLE, "select", "s")
            // ステージテンプレートを作成する、作成後自動的に選択される。
            .child(CCmdSpleefNew.CALLABLE, "new", "n")
            .build();
}

