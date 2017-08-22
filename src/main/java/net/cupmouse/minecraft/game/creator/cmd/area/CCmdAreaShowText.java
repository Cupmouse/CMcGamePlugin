package net.cupmouse.minecraft.game.creator.cmd.area;

import net.cupmouse.minecraft.game.creator.CreatorBank;
import net.cupmouse.minecraft.game.creator.CreatorModule;
import net.cupmouse.minecraft.worlds.WorldTagAreaSquare;
import net.cupmouse.minecraft.worlds.WorldTagModule;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import static org.spongepowered.api.command.args.GenericArguments.none;

public class CCmdAreaShowText implements CommandExecutor {

    public static final CommandCallable CALLABLE = CommandSpec.builder()
            .arguments(none())
            .executor(new CCmdAreaShowText())
            .build();


    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        CreatorBank session = CreatorModule.getOrCreateBankOf(src);

        if (session.loadedArea == null) {
            throw new CommandException(Text.of(TextColors.RED, "✗エリアがロードされていません。"), false);
        }

        if (session.loadedArea instanceof WorldTagAreaSquare) {
            WorldTagAreaSquare areaSquare = (WorldTagAreaSquare) session.loadedArea;

            src.sendMessage(
                    Text.of(TextColors.AQUA,
                            "===ロードされたエリアの種類/直方体\n",
                            "位置小/", areaSquare.minPos.toString(),
                            " 位置大/", areaSquare.maxPos.toString(), "\n",
                            "大きさ/", areaSquare.maxPos.sub(areaSquare.minPos), "\n",
                            "ワールドタグ名/", areaSquare.worldTag.getTagName(), " 存在する?/",
                            WorldTagModule.getTaggedWorld(areaSquare.worldTag).isPresent() ? "はい" : "いいえ"
                    )
            );
        } else {
            // TODO
        }

        return CommandResult.success();
    }
}
