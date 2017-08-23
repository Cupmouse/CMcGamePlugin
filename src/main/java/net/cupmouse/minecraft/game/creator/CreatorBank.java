package net.cupmouse.minecraft.game.creator;

import net.cupmouse.minecraft.game.spleef.SpleefStageTemplate;
import net.cupmouse.minecraft.worlds.*;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;

/**
 * クリエイター（マップメイカー）のセッションで一時的な情報を格納するバンカーというものです。
 * 例えば、選択されたエリアや位置、エリアの作成に必要な２つの位置を保持します。
 * すべての関数はコマンドから呼ばれることを想定しています。
 */
public final class CreatorBank {

    private WorldTagPosition position;
    private WorldTagArea area;

    public boolean isSelectionEnabled;
    private Location<World> firstLoc;
    private Location<World> secondLoc;
    private Location<World> relativeBase;

    private SpleefStageTemplate spleefSelectedTemplate;

    public WorldTagAreaSquare createAreaSquareOrThrow() throws CommandException {
        if (firstLoc == null) {
            throw new CommandException(Text.of(TextColors.RED, "✗第1ポイントが選択されていません"));
        }

        if (secondLoc == null) {
            throw new CommandException(Text.of(TextColors.RED, "✗第2ポイントが選択されていません"));
        }

        if (firstLoc.getExtent() != secondLoc.getExtent()) {
            throw new CommandException(Text.of(TextColors.RED,
                    "✗2つの位置が選択されましたが、それぞれ別のワールドにあるので続行できません"),
                    false);
        }

        Optional<WorldTag> worldTagOptional = WorldTagModule.whatIsThisWorld(firstLoc.getExtent());

        if (!worldTagOptional.isPresent()) {
            throw new CommandException(Text.of(TextColors.RED, "✗エラー、管理人に報告してください"));
        }

        // TODO ブロックの位置!=Entityの位置？なので使うとプレイヤーが中にいるか判定するのに手こずるのではないか？
        return new WorldTagAreaSquare(worldTagOptional.get(),
                firstLoc.getBlockPosition(), secondLoc.getBlockPosition());
    }

    public SpleefStageTemplate getSpleefSelectedTemplateOrThrow() throws CommandException {
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

    public void setPosition(WorldTagPosition position) {
        this.position = position;
    }

    public WorldTagPosition getPositionOrThrow() throws CommandException {
        if (position == null) {
            throw new CommandException(Text.of(TextColors.RED,
                    "✗バンクにポジションはロードされていません"), false);
        }

        return position;
    }

    /**
     * ロードされていなくても通ります
     */
    public WorldTagRocation getPositionAsRocationOrThrow() throws CommandException {
        if (!(position instanceof WorldTagRocation)) {
            throw new CommandException(Text.of(TextColors.RED,
                    "✗バンクにロードされているポジションは回転要素を", TextStyles.BOLD, "含んでいません"),
                    false);
        }
// TODO 明示的になるためにしたが、コマンドでバンクのポジションを変換できるようにしなければいけない

        return (WorldTagRocation) position;
    }

    public WorldTagLocation getPositionAsLocationOrThrow() throws CommandException {
        if (position == null) {
            throw new CommandException(Text.of(TextColors.RED, "✗バンクにポジションはロードされていません"),
                    false);
        }
        if (!(position instanceof WorldTagLocation)) {
            throw new CommandException(Text.of(TextColors.RED,
                    "✗バンクにロードされているポジションは回転要素を", TextStyles.BOLD, "含んでいます"), false);
        }


        return (WorldTagLocation) position;
    }

    public void setSpleefSelectedTemplate(SpleefStageTemplate spleefSelectedTemplate) {
        this.spleefSelectedTemplate = spleefSelectedTemplate;
    }

    public Location<World> getFirstLoc() {
        return firstLoc;
    }

    public void setFirstLoc(Location<World> firstLoc) {
        this.firstLoc = firstLoc;
    }

    public Location<World> getSecondLoc() {
        return secondLoc;
    }

    public void setSecondLoc(Location<World> secondLoc) {
        this.secondLoc = secondLoc;
    }

    public Location<World> getRelativeBaseOrThrow() throws CommandException {
        if (relativeBase == null) {
            throw new CommandException(Text.of(TextColors.RED, "✗相対位置基準ポジションがロードされていません"));
        }

        return relativeBase;
    }
}
