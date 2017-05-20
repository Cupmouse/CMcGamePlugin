package net.cupmouse.minecraft.game.creator.command;

import net.cupmouse.minecraft.CMcCore;
import net.cupmouse.minecraft.game.CMcGamePlugin;
import net.cupmouse.minecraft.game.creator.CreatorModule;
import net.cupmouse.minecraft.game.creator.CreatorSessionInfo;
import net.cupmouse.minecraft.worlds.WorldTag;
import net.cupmouse.minecraft.worlds.WorldTagModule;
import net.cupmouse.minecraft.worlds.WorldTagRocation;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.entity.spawn.EntitySpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.World;

import java.util.HashMap;

import static org.spongepowered.api.command.args.GenericArguments.choices;
import static org.spongepowered.api.command.args.GenericArguments.onlyOne;

public class CCmdLocationShow implements CommandExecutor {

    public static CommandCallable CALLABLE = CommandSpec.builder()
            .arguments(onlyOne(choices(Text.of("method"), new HashMap<String, String>() {{
                put("teleport", "teleport");
                put("t", "t");
                put("armorstand", "armorstand");
                put("a", "a");
            }})))
            .build();


    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        CreatorSessionInfo session = CreatorModule.getOrCreateSession(src);

        if (session.worldTagLoc == null) {
            src.sendMessage(Text.of(TextColors.RED, "✗ロケーションがロードされていません。"));
            return CommandResult.empty();
        }

        String method = args.<String>getOne("method").get();

        if (method.equals("teleport") || method.equals("t")) {

            if (src instanceof Player) {
                session.worldTagLoc.teleportHere(((Player) src));
            }
        } else if (method.equals("armorstand") || method.equals("a")) {

            World world = WorldTagModule.getTaggedWorld(session.worldTagLoc.worldTag).get();

            Entity armorEnt= world.createEntity(EntityTypes.ARMOR_STAND, session.worldTagLoc.position);
            session.worldTagLoc.teleportHere(armorEnt);
            EntitySpawnCause spawnCause = EntitySpawnCause.builder().type(SpawnTypes.CUSTOM).build();

            world.spawnEntity(armorEnt, Cause.source(spawnCause).build());

            // 20秒後にすべて消す
            Sponge.getScheduler().createTaskBuilder()
                    .delayTicks(20 * 20)
                    .execute((Runnable) armorEnt::remove)
                    .submit(CMcCore.getPlugin());
        }


        src.sendMessage(Text.of(TextColors.AQUA, "✓実行しました。"));
        return CommandResult.success();
    }
}
