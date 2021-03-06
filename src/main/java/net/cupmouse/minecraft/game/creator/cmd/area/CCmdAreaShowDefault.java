package net.cupmouse.minecraft.game.creator.cmd.area;

import net.cupmouse.minecraft.game.creator.CreatorBank;
import net.cupmouse.minecraft.game.creator.CreatorModule;
import net.cupmouse.minecraft.worlds.BlockLocSequence;
import net.cupmouse.minecraft.worlds.WorldTagArea;
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
import java.util.function.BiConsumer;

import static org.spongepowered.api.command.args.GenericArguments.choices;
import static org.spongepowered.api.command.args.GenericArguments.onlyOne;

public class CCmdAreaShowDefault implements CommandExecutor {

    public static CommandCallable callable(BiConsumer<World, BlockLocSequence> shower) {
        return CommandSpec.builder()
                .arguments(
                        onlyOne(choices(Text.of("method"), new HashMap<String, String>() {{
                            put("corner", "corner");
                            put("c", "corner");
                            put("outline", "outline");
                            put("o", "outline");
                        }})))
                .executor(new CCmdAreaShowDefault(shower))
                .build();
    }

    private BiConsumer<World, BlockLocSequence> shower;

    private CCmdAreaShowDefault(BiConsumer<World, BlockLocSequence> shower) {
        this.shower = shower;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        String method = args.<String>getOne("method").get();

        BlockLocSequence sequence;

        CreatorBank bank = CreatorModule.getOrCreateBankOf(src);
        WorldTagArea area = bank.getAreaOrThrow();

        switch (method) {
            case "corner":
                sequence = area.getCornerBlocks();
                break;
            case "outline":
                sequence = area.getOutlineBlocks();
                break;
            default:
                // ?????????????????????
                return CommandResult.empty();
        }

        Optional<World> worldOptional = WorldTagModule.getTaggedWorld(sequence.worldTag);

        if (!worldOptional.isPresent()) {
            throw new CommandException(
                    Text.of(TextColors.RED,
                            "???????????????????????????????????????????????????????????????????????????????????????????????????"));
        }

        World world = worldOptional.get();

        // ?????????????????????
        shower.accept(world, sequence);

        src.sendMessage(Text.of(TextColors.GOLD, "?????????????????????"));
        return CommandResult.success();
    }
}
