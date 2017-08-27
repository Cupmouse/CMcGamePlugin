package net.cupmouse.minecraft.game.creator.cmd.position;

import net.cupmouse.minecraft.game.creator.CreatorModule;
import net.cupmouse.minecraft.worlds.WorldTagLocation;
import net.cupmouse.minecraft.worlds.WorldTagRocation;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.blockray.BlockRay;
import org.spongepowered.api.util.blockray.BlockRayHit;
import org.spongepowered.api.world.World;

import java.util.Optional;

import static org.spongepowered.api.command.args.GenericArguments.none;

public class CCmdPositionLookingat implements CommandExecutor {

    public static final CommandCallable CALLABLE = CommandSpec.builder()
            .arguments(none())
            .executor(new CCmdPositionLookingat())
            .build();

    private CCmdPositionLookingat() {
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (!(src instanceof Player)) {
            throw new CommandException(Text.of(TextColors.RED, "✗プレイヤーのみ実行できます"));
        }


        BlockRay<World> blockRay = BlockRay.from(((Player) src)).skipFilter(BlockRay.onlyAirFilter()).distanceLimit(10).build();
        if (blockRay.hasNext()) {
            BlockRayHit<World> next = blockRay.next();

            Optional<WorldTagLocation> locationOptional = WorldTagLocation.fromSponge(next.getLocation());

            if (!locationOptional.isPresent()) {
                throw new CommandException(Text.of(TextColors.RED, "✗エラー、管理人にご報告ください"));
            }

            CreatorModule.getOrCreateBankOf(src).setPosition(locationOptional.get());
            src.sendMessage(Text.of(TextColors.GOLD,
                    "✓バンクにプレイヤーの視線先のブロックのポジションをロードしました"));
            return CommandResult.success();
        } else {
            throw new CommandException(Text.of(TextColors.RED,
                    "✗10ブロック以内に見つめているブロックがありません"));
        }
    }
}
