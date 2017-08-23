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
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

import static org.spongepowered.api.command.args.GenericArguments.flags;
import static org.spongepowered.api.command.args.GenericArguments.none;

public class CCmdPositionEntity implements CommandExecutor {

    public static final CommandCallable CALLABLE = CommandSpec.builder()
            .arguments(GenericArguments.entity(Text.of("entity")),
                    flags().flag("r", "rot", "rotation").buildWith(none()))
            .executor(new CCmdPositionEntity())
            .build();

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Entity entity = args.<Entity>getOne("entity").get();

        if (args.hasAny("r")) {
            Optional<WorldTagRocation> rocationOptional = WorldTagRocation.fromEntity(entity);

            if (!rocationOptional.isPresent()) {
                throw new CommandException(Text.of(TextColors.RED, "✗エラー、管理人に報告してください"), false);
            }

            CreatorModule.getOrCreateBankOf(src).setPosition(rocationOptional.get());

            src.sendMessage(Text.of(TextColors.GOLD,
                    "✓バンクに", entity, "の回転情報付きポジションをロードしました"));
            return CommandResult.success();
        } else {
            Optional<WorldTagLocation> locationOptional = WorldTagLocation.fromEntity(entity);

            if (!locationOptional.isPresent()) {
                throw new CommandException(Text.of(TextColors.RED, "✗エラー、管理人に報告してください"), false);
            }

            CreatorModule.getOrCreateBankOf(src).setPosition(locationOptional.get());

            src.sendMessage(Text.of(TextColors.GOLD, "✓バンクに", entity, "のポジションをロードしました"));
            return CommandResult.success();
        }
    }
}
