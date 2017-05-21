package net.cupmouse.minecraft.game.creator.cmd.position;

import net.cupmouse.minecraft.CMcCore;
import net.cupmouse.minecraft.game.creator.CreatorModule;
import net.cupmouse.minecraft.game.creator.CreatorSessionInfo;
import net.cupmouse.minecraft.worlds.WorldTagModule;
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
                put("text", "text");
                put("t", "t");
                put("teleport", "teleport");
                put("t", "t");
                put("armorstand", "armorstand");
                put("a", "a");
            }})))
            .executor(new CCmdPositionShow())
            .build();


    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        CreatorSessionInfo session = CreatorModule.getOrCreateSession(src);

        if (session.loadedPos == null) {
            throw new CommandException(Text.of(TextColors.RED, "✗ポジションがロードされていません。"), false);
        }

        String method = args.<String>getOne("method").get();

        if (method.equals("text") || method.equals("t")) {
            src.sendMessage(Text.of(TextColors.AQUA,
                    "===ロードされたポジション\n",
                    "回転情報/",
                    session.loadedPos instanceof WorldTagRocation
                            ? ((WorldTagRocation) session.loadedPos).rotation.toString()
                            : "なし", "\n",
                    "位置/", session.loadedPos.getPosition().toString(), "\n",
                    "ワールドタグ名/", session.loadedPos.getWorldTag().getTagName(), " 存在する?/",
                    WorldTagModule.getTaggedWorld(session.loadedPos.getWorldTag()).isPresent() ? "はい" : "いいえ"

            ));

            return CommandResult.success();
        }

        if (method.equals("teleport") || method.equals("t")) {

            if (src instanceof Player) {
                session.loadedPos.teleportHere(((Player) src));
            }
        } else if (method.equals("armorstand") || method.equals("a")) {

            World world = WorldTagModule.getTaggedWorld(session.loadedPos.getWorldTag()).get();

            Entity armorEnt= world.createEntity(EntityTypes.ARMOR_STAND, session.loadedPos.getPosition());
            session.loadedPos.teleportHere(armorEnt);

            if (session.loadedPos instanceof WorldTagRocation) {
                // 方向もあるなら、革のヘルメットを被らせる。
                ((ArmorStand) armorEnt).setHelmet(ItemStack.of(ItemTypes.LEATHER_HELMET, 1));
            }

            EntitySpawnCause spawnCause = EntitySpawnCause.builder().entity(armorEnt).type(SpawnTypes.CUSTOM).build();

            world.spawnEntity(armorEnt, Cause.source(spawnCause).build());

            // 20秒後に消す
            Sponge.getScheduler().createTaskBuilder()
                    .delayTicks(20 * 20)
                    .execute((Runnable) armorEnt::remove)
                    .submit(CMcCore.getPlugin());
        }


        src.sendMessage(Text.of(TextColors.AQUA, "✓実行しました。"));
        return CommandResult.success();
    }
}
