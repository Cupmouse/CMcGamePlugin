package net.cupmouse.minecraft.game.creator.command;

import com.flowpowered.math.vector.Vector3i;
import net.cupmouse.minecraft.CMcCore;
import net.cupmouse.minecraft.game.creator.CreatorModule;
import net.cupmouse.minecraft.game.creator.CreatorSessionInfo;
import net.cupmouse.minecraft.worlds.BlockLocSequence;
import net.cupmouse.minecraft.worlds.WorldTagModule;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.entity.spawn.EntitySpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.item.FireworkEffect;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Color;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.*;

import static org.spongepowered.api.command.args.GenericArguments.choices;
import static org.spongepowered.api.command.args.GenericArguments.onlyOne;

public final class CCmdAreaShow implements CommandExecutor {

    public static final CommandCallable CALLABLE = CommandSpec.builder()
            .arguments(
                    onlyOne(choices(Text.of("method"), new HashMap<String, String>() {{
                        put("glow", "glow");
                        put("g", "g");
                        put("armorstand", "armorstand");
                        put("a", "a");
                        put("fireworks", "fireworks");
                        put("f", "f");
                        put("fakeblocks", "fakeblocks");
                        put("b", "b");
                        put("resetfake", "resetfake");
                        put("rb", "rb");
                    }})),
                    onlyOne(choices(Text.of("place"), new HashMap<String, String>() {{
                        put("corner", "corner");
                        put("c", "c");
                        put("outline", "outline");
                        put("o", "o");
                    }}))
            )
            .executor(new CCmdAreaShow())
            .build();

    private CCmdAreaShow() {
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        String method = args.<String>getOne("method").get();
        String place = args.<String>getOne("place").get();

        BlockLocSequence sequence;

        CreatorSessionInfo session = CreatorModule.getOrCreateSession(src);
        if (session.loadedArea == null) {
            src.sendMessage(Text.of(TextColors.RED, "✗エリアがロードされていません。"));
            return CommandResult.empty();
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
            src.sendMessage(
                    Text.of("✗エリアに問題があります。エリアに設定されたワールドが存在しません。"));
            return CommandResult.empty();
        }

        World world = worldOptional.get();

        if (method.equals("glow") || method.equals("g")) {
            Set<Entity> spawnedEntities = new HashSet<>();

            for (Vector3i blockLoc : sequence.blockLocs) {
                Entity fallingBlockEnt = world.createEntity(EntityTypes.FALLING_BLOCK, blockLoc);
                ArrayList<PotionEffect> potionEffects = new ArrayList<>();

                potionEffects.add(PotionEffect.builder()
                        .potionType(PotionEffectTypes.GLOWING)
                        .duration(100000)
                        .particles(false)
                        .build());

                fallingBlockEnt.offer(Keys.POTION_EFFECTS, potionEffects);
                fallingBlockEnt.offer(Keys.HAS_GRAVITY, false);

                EntitySpawnCause spawnCause = EntitySpawnCause.builder().type(SpawnTypes.CUSTOM).build();
                world.spawnEntity(fallingBlockEnt, Cause.source(spawnCause).build());
                spawnedEntities.add(fallingBlockEnt);
            }

            // ２０秒後にすべて削除する
            Sponge.getScheduler().createTaskBuilder().delayTicks(20 * 20).execute(() -> {
                for (Entity spawnedEntity : spawnedEntities) {
                    spawnedEntity.remove();
                }
            }).submit(CMcCore.getPlugin());
        } else if (method.equals("armorstand") || method.equals("a")) {
            Iterator<Vector3i> iterator = sequence.blockLocs.iterator();

            Entity armorEnt = world.createEntity(EntityTypes.ARMOR_STAND, iterator.next());

            EntitySpawnCause spawnCause = EntitySpawnCause.builder().type(SpawnTypes.CUSTOM).build();
            world.spawnEntity(armorEnt, Cause.source(spawnCause).build());

            Task armorStandTask = Sponge.getScheduler().createTaskBuilder()
                    .delayTicks(5)
                    .intervalTicks(5)
                    .execute((task) -> {
                        if (!iterator.hasNext()) {
                            armorEnt.remove();
                            task.cancel();
                        }

                        armorEnt.setLocation(new Location<>(world, iterator.next()));
                    })
                    .submit(CMcCore.getPlugin());
        } else if (method.equals("fireworks") || method.equals("f")) {
            for (Vector3i blockLoc : sequence.blockLocs) {
                Entity fireworkEnt = world.createEntity(EntityTypes.FIREWORK, blockLoc);
                FireworkEffect fireworkEffect = FireworkEffect.builder().color(Color.GREEN).trail(true).build();
                ArrayList<FireworkEffect> effects = new ArrayList<>();
                effects.add(fireworkEffect);

                fireworkEnt.offer(Keys.FIREWORK_EFFECTS, effects);
                EntitySpawnCause spawnCause = EntitySpawnCause.builder().type(SpawnTypes.CUSTOM).build();
                world.spawnEntity(fireworkEnt, Cause.source(spawnCause).build());
            }
        } else if (method.equals("fakeblocks") || method.equals("b")) {
            for (Vector3i blockLoc : sequence.blockLocs) {
                world.sendBlockChange(blockLoc, BlockState.builder().blockType(BlockTypes.DEADBUSH).build());
            }
        } else if (method.equals("resetfake") || method.equals("rb")) {
            for (Vector3i blockLoc : sequence.blockLocs) {
                world.resetBlockChange(blockLoc);
            }
        }

        src.sendMessage(Text.of(TextColors.AQUA, "✓実行しました。"));
        return CommandResult.success();
    }
}
