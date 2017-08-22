package net.cupmouse.minecraft.game.creator.cmd.spleef;

import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.spec.CommandSpec;

/**
 * Spleefのステージテンプレートを編集するための一連のコマンドです。
 */
public class CCmdSpleef {

    public static final CommandCallable CALLABLE = CommandSpec.builder()
            // 範囲をバンクに読み込む
            .child(, "loadarea", "la")
            // 範囲をテンプレートに設定する
            .child(, "setarea", "sa")
            // 位置をバンクに読み込む
            .child(, "loadloc", "ll")
            // 位置を記録する
            .child(, "setloc", "sl")
            // ステージテンプレートを選択する
            .child(, "select", "s")
            // ステージテンプレートを作成する、作成後自動的に選択される。
            .child(, "new", "n")
            .build();
}
