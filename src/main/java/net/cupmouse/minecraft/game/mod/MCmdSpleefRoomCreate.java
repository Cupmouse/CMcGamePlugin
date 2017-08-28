package net.cupmouse.minecraft.game.mod;

import net.cupmouse.minecraft.game.CMcGamePlugin;
import net.cupmouse.minecraft.game.creator.CreatorModule;
import net.cupmouse.minecraft.game.manager.GameException;
import net.cupmouse.minecraft.worlds.WorldTagLocation;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import static org.spongepowered.api.command.args.GenericArguments.*;

public class MCmdSpleefRoomCreate implements CommandExecutor {

    public static final CommandCallable CALLABLE = CommandSpec.builder()
            .arguments(onlyOne(integer(Text.of("room_number"))),
                    onlyOne(string(Text.of("template_id"))))
            .executor(new MCmdSpleefRoomCreate())
            .build();

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Integer roomNumber = args.<Integer>getOne("room_number").get();
        String templateId = args.<String>getOne("template_id").get();

        src.sendMessage(Text.of(TextColors.AQUA, "⚠基準点を選んでいないと続行しません"));
        WorldTagLocation relativeBaseLocation = CreatorModule.getOrCreateBankOf(src).getPositionAsLocationOrThrow();

        try {
            CMcGamePlugin.getSpleef().newRoom(roomNumber, templateId, relativeBaseLocation);
        } catch (GameException e) {
            throw new CommandException(Text.of(TextColors.RED, "✗ステージを作成できませんでした。", e.getText()),
                    e, false);
        }

        src.sendMessage(Text.of(TextColors.GOLD,
                String.format("✓テンプレート%sのSpleef部屋%dを作成しました。", templateId, roomNumber)));
        return CommandResult.success();
    }
}
