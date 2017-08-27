package net.cupmouse.minecraft.game.creator.cmd.spleef;

import net.cupmouse.minecraft.game.creator.CreatorModule;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

/**
 * Spleefのステージテンプレートを編集するための一連のコマンドです。
 */
public class CCmdSpleef {

    public static final CommandCallable CALLABLE = CommandSpec.builder()
            .description(Text.of(CreatorModule.TEXT_DEFAULT_DESCRIPTION))
            .permission("cmc.game.creator")
            // 範囲をバンクに読み込む
            .child(CCmdSpleefLoadarea.CALLABLE, "loadarea", "la")
            // 範囲をテンプレートに設定する
            .child(CCmdSpleefSetarea.CALLABLE, "setarea", "sa")
            // 位置をバンクに読み込む
            .child(CCmdSpleefLoadpos.CALLABLE, "loadpos", "lp")
            // 位置を記録する
            .child(CCmdSpleefSetpos.CALLABLE, "setpos", "sp")
            // ステージテンプレートを選択する
            .child(CCmdSpleefSelect.CALLABLE, "select", "sel", "s")
            // ステージテンプレートを作成する、作成後自動的に選択される。
            .child(CCmdSpleefNew.CALLABLE, "new", "n")
            // 設定を変更/確認できる
            .child(CCmdSpleefOption.CALLABLE, "option", "opt", "o")
            .build();
}

