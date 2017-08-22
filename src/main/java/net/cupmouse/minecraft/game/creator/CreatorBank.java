package net.cupmouse.minecraft.game.creator;

import net.cupmouse.minecraft.game.manager.GameException;
import net.cupmouse.minecraft.game.spleef.SpleefStage;
import net.cupmouse.minecraft.worlds.*;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;

/**
 * クリエイター（マップメイカー）のセッションで一時的な情報を格納するバンカーというものです。
 * 例えば、選択されたエリアや位置、エリアの作成に必要な２つの位置を保持します。
 * すべての関数はコマンドから呼ばれることを想定しています。
 */
public final class CreatorBank {

    private WorldTagArea area;

    public boolean selectionEnabled;
    private Location<World> firstLoc;
    private Location<World> secondLoc;

    private SpleefStage spleefSelectedTemplate;

    public WorldTagAreaSquare createAreaSquareOrThrow() throws CommandException {
        if (firstLoc.getExtent() != secondLoc.getExtent()) {
            throw new CommandException(Text.of(TextColors.RED,
                            "✗2つの位置が選択されましたが、それぞれ別のワールドにあるので続行できません"),
                    false);
        }

        Optional<WorldTag> worldTagOptional = WorldTagModule.whatIsThisWorld(firstLoc.getExtent());

        if (!worldTagOptional.isPresent()) {
            throw new CommandException(Text.of(TextColors.RED,
                    String.format("✗ワールド'%s'が不明です、管理人にこれを報告してください",
                            firstLoc.getExtent().getName())), false);
        }

        // TODO ブロックの位置!=Entityの位置？なので使うとプレイヤーが中にいるか判定するのに手こずるのではないか？
        return new WorldTagAreaSquare(worldTagOptional.get(),
                firstLoc.getBlockPosition(), secondLoc.getBlockPosition());
    }

    public SpleefStage getSpleefSelectedTemplateOrThrow() throws CommandException {
        if (spleefSelectedTemplate == null) {
            throw new CommandException(Text.of(TextColors.RED, "✗ステージテンプレートが選択されていません"), false);
        }

        return spleefSelectedTemplate;
    }

    public WorldTagArea getAreaOrThrow() throws CommandException {
        if (area == null) {
            throw new CommandException(Text.of(TextColors.RED, "✗バンクにエリアはロードされていません"), false);
        }

        return area;
    }

    public void setArea(WorldTagArea area) {
        this.area = area;
    }
}
