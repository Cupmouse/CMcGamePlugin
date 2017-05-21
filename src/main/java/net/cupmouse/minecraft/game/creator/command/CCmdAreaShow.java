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

public final class CCmdAreaShow {

    public static final CommandCallable CALLABLE = CommandSpec.builder()
            .child(CCmdAreaShowText.CALLABLE, "text", "t")
            // GLOW
            .child(CCmdAreaShowDefault.callable((world, blockLocSequence) -> {
                Set<Entity> spawnedEntities = new HashSet<>();

                for (Vector3i blockLoc : blockLocSequence.blockLocs) {
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
            }), "glow", "g")
            // Armorstand
            .child(CCmdAreaShowDefault.callable((world, blockLocSequence) -> {
                Iterator<Vector3i> iterator = blockLocSequence.blockLocs.iterator();

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


            }), "armorstand", "a")
            // Fireworks
            .child(CCmdAreaShowDefault.callable((world, blockLocSequence) -> {
                for (Vector3i blockLoc : blockLocSequence.blockLocs) {
                    Entity fireworkEnt = world.createEntity(EntityTypes.FIREWORK, blockLoc);
                    FireworkEffect fireworkEffect = FireworkEffect.builder().color(Color.GREEN).trail(true).build();
                    ArrayList<FireworkEffect> effects = new ArrayList<>();
                    effects.add(fireworkEffect);

                    fireworkEnt.offer(Keys.FIREWORK_EFFECTS, effects);
                    EntitySpawnCause spawnCause = EntitySpawnCause.builder().type(SpawnTypes.CUSTOM).build();
                    world.spawnEntity(fireworkEnt, Cause.source(spawnCause).build());
                }
            }), "fireworks", "f")
            // Fakeblocks
            .child(CCmdAreaShowDefault.callable((world, blockLocSequence) -> {
                for (Vector3i blockLoc : blockLocSequence.blockLocs) {
                    world.sendBlockChange(blockLoc, BlockState.builder().blockType(BlockTypes.DEADBUSH).build());
                }
            }), "fakeblocks", "b", "deadbush")
            // ResetFake
            .child(CCmdAreaShowDefault.callable((world, blockLocSequence) -> {
                for (Vector3i blockLoc : blockLocSequence.blockLocs) {
                    world.resetBlockChange(blockLoc);
                }
            }), "resetfake", "rb")
            .build();

    private CCmdAreaShow() {
    }
}
