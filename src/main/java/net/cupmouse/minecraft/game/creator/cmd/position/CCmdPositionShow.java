package net.cupmouse.minecraft.game.creator.cmd.position;

import net.cupmouse.minecraft.CMcCore;
import net.cupmouse.minecraft.game.creator.CreatorBank;
import net.cupmouse.minecraft.game.creator.CreatorModule;
import net.cupmouse.minecraft.worlds.WorldTagModule;
import net.cupmouse.minecraft.worlds.WorldTagPosition;
import net.cupmouse.minecraft.worlds.WorldTagRocation;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.entity.spawn.EntitySpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.World;

import java.util.HashMap;

import static org.spongepowered.api.command.args.GenericArguments.choices;
import static org.spongepowered.api.command.args.GenericArguments.onlyOne;

public final class CCmdPositionShow implements CommandExecutor {

    public static final CommandCallable CALLABLE = CommandSpec.builder()
            .arguments(onlyOne(choices(Text.of("method"), new HashMap<String, String>() {{
                put("info", "info");
                put("i", "info");
                put("teleport", "teleport");
                put("t", "teleport");
                put("armorstand", "armorstand");
                put("a", "armorstand");
            }})))
            .executor(new CCmdPositionShow())
            .build();


    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        CreatorBank bank = CreatorModule.getOrCreateBankOf(src);

        WorldTagPosition position = bank.getPositionOrThrow();

        String method = args.<String>getOne("method").get();

        if (method.equals("info")) {
            src.sendMessage(Text.of(TextColors.GOLD,
                    "===?????????????????????????????????\n",
                    "????????????/",
                    position instanceof WorldTagRocation
                            ? ((WorldTagRocation) position).rotation.toString()
                            : "??????", "\n",
                    "??????/", position.getPosition().toString(), "\n",
                    "?????????????????????/", position.getWorldTag().getTagName(), " ?????????????/",
                    WorldTagModule.getTaggedWorld(position.getWorldTag()).isPresent() ? "??????" : "?????????"

            ));

            return CommandResult.success();
        }

        if (method.equals("teleport")) {

            if (src instanceof Player) {
                position.teleportHere(((Player) src));
            }

            src.sendMessage(Text.of(TextColors.GOLD, "??????????????????????????????"));
            return CommandResult.success();
        } else if (method.equals("armorstand")) {
            World world = WorldTagModule.getTaggedWorld(position.getWorldTag()).get();

            Entity armorEnt = world.createEntity(EntityTypes.ARMOR_STAND, position.getPosition());
            position.teleportHere(armorEnt);

            if (position instanceof WorldTagRocation) {
                // ???????????????????????????????????????????????????????????????
                ((ArmorStand) armorEnt).setHelmet(ItemStack.of(ItemTypes.LEATHER_HELMET, 1));
            }

            EntitySpawnCause spawnCause = EntitySpawnCause.builder().entity(armorEnt).type(SpawnTypes.CUSTOM).build();

            world.spawnEntity(armorEnt, Cause.source(spawnCause).build());

            // 20???????????????
            Sponge.getScheduler().createTaskBuilder()
                    .delayTicks(20 * 20)
                    .execute((Runnable) armorEnt::remove)
                    .submit(CMcCore.getPlugin());

            src.sendMessage(Text.of(TextColors.GOLD, "????????????????????????????????????????????????"));
            return CommandResult.success();
        }

        // ??????????????????
        return CommandResult.empty();
    }
}
