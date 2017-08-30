package net.cupmouse.minecraft.game.creator.cmd.position;

import net.cupmouse.minecraft.game.creator.CreatorBank;
import net.cupmouse.minecraft.game.creator.CreatorModule;
import net.cupmouse.minecraft.util.UnknownWorldException;
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
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;

import static org.spongepowered.api.command.args.GenericArguments.flags;
import static org.spongepowered.api.command.args.GenericArguments.none;
import static org.spongepowered.api.command.args.GenericArguments.onlyOne;

public final class CCmdPosition implements CommandExecutor {

    public static final CommandCallable CALLABLE = CommandSpec.builder()
            .description(Text.of(CreatorModule.TEXT_DEFAULT_DESCRIPTION))
            .permission("cmc.game.creator")
            .arguments(flags().flag("r", "rot", "rotation").buildWith(none()))
            .child(CCmdPositionLookingat.CALLABLE, "lookat", "l")
            .child(CCmdPositionShow.CALLABLE, "show", "s")
            .child(CCmdPositionEntity.CALLABLE, "entity", "ent", "e")
            .child(CCmdPositionClear.CALLABLE, "clear", "c")
            .executor(new CCmdPosition())
            .build();

    private CCmdPosition() {
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        // プレイヤーの現在位置をバンクにロードする
        if (!(src instanceof Player)) {
            throw new CommandException(Text.of(TextColors.RED, "✗プレイヤーのみが実行できます"), false);
        }

        Player player = (Player) src;
        Location<World> location = player.getLocation();

        CreatorBank bank = CreatorModule.getOrCreateBankOf(player);

        if (args.hasAny("r")) {
            // 回転情報付きRocationをロードする

            Optional<WorldTagRocation> rocationOptional = WorldTagRocation.fromEntity(player);

            if (!rocationOptional.isPresent()) {
                throw new CommandException(Text.of(TextColors.RED, "✗エラー、管理人に報告してください"), false);
            }

            bank.setPosition(rocationOptional.get());
            src.sendMessage(Text.of(TextColors.GOLD,
                    "✓バンクにプレイヤー現在位置の回転情報つきポジションをロードしました"));
            return CommandResult.success();
        } else {
            // 回転情報がついていないLocationをロードする

            Optional<WorldTagLocation> locationOptional = WorldTagLocation.fromEntity(player);

            if (!locationOptional.isPresent()) {
                throw new CommandException(Text.of(TextColors.RED,
                        String.format("✗ワールド'%s'が不明です、管理人にこれを報告してください",
                                location.getExtent().getName())), false);
            }

            bank.setPosition(locationOptional.get());
            src.sendMessage(Text.of(TextColors.GOLD,
                    "✓バンクにプレイヤー現在位置のポジションをロードしました"));
            return CommandResult.success();
        }
    }
}
