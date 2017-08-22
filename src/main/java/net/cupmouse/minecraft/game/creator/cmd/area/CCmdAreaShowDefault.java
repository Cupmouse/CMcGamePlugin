package net.cupmouse.minecraft.game.creator.cmd.area;

import net.cupmouse.minecraft.game.creator.CreatorBank;
import net.cupmouse.minecraft.game.creator.CreatorModule;
import net.cupmouse.minecraft.util.DualConsumer;
import net.cupmouse.minecraft.worlds.BlockLocSequence;
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
import org.spongepowered.api.world.World;

import java.util.HashMap;
import java.util.Optional;

import static org.spongepowered.api.command.args.GenericArguments.choices;
import static org.spongepowered.api.command.args.GenericArguments.onlyOne;

public class CCmdAreaShowDefault implements CommandExecutor {

    public static CommandCallable callable(DualConsumer<World, BlockLocSequence> shower) {
        return CommandSpec.builder()
                .arguments(
                        onlyOne(choices(Text.of("place"), new HashMap<String, String>() {{
                            put("corner", "corner");
                            put("c", "c");
                            put("outline", "outline");
                            put("o", "o");
                        }})))
                .executor(new CCmdAreaShowDefault(shower))
                .build();
    }

    private DualConsumer<World, BlockLocSequence> shower;

    public CCmdAreaShowDefault(DualConsumer<World, BlockLocSequence> shower) {
        this.shower = shower;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        String place = args.<String>getOne("place").get();

        BlockLocSequence sequence;

        CreatorBank session = CreatorModule.getOrCreateBankOf(src);

        if (session.loadedArea == null) {
            throw new CommandException(Text.of(TextColors.RED, "✗エリアがロードされていません。"), false);
        }

        switch (place) {
            case "corner":
            case "c":
            default:
                sequence = session.loadedArea.getCornerBlocks();
                break;
            case "outline":
            case "o":
                sequence = session.loadedArea.getOutlineBlocks();
                break;
        }

        Optional<World> worldOptional = WorldTagModule.getTaggedWorld(sequence.worldTag);

        if (!worldOptional.isPresent()) {
            throw new CommandException(
                    Text.of(TextColors.RED,
                            "✗エリアに問題があります。エリアに設定されたワールドが存在しません。"));
        }

        World world = worldOptional.get();

        // 実行する、渡す
        shower.accept(world, sequence);

        src.sendMessage(Text.of(TextColors.AQUA, "✓実行しました。"));
        return CommandResult.success();
    }
}
