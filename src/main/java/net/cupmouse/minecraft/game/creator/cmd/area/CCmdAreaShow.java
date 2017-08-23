package net.cupmouse.minecraft.game.creator.cmd.area;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import net.cupmouse.minecraft.CMcCore;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.entity.spawn.EntitySpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.item.FireworkEffect;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.Color;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public final class CCmdAreaShow {

    private static final Vector3d V3D_HALF = new Vector3d(.5, .5, .5);

    public static final CommandCallable CALLABLE = CommandSpec.builder()
            .child(CCmdAreaShowText.CALLABLE, "text", "t")
            // GLOW
            .child(CCmdAreaShowDefault.callable((world, blockLocSequence) -> {
                Set<Entity> spawnedEntities = new HashSet<>();

                for (Vector3i blockLoc : blockLocSequence.blockLocs) {
                    Entity shulker = world.createEntity(EntityTypes.SHULKER, blockLoc.toDouble().add(V3D_HALF));

                    shulker.offer(Keys.GLOWING, true);
                    shulker.offer(Keys.INVISIBLE, true);
                    shulker.offer(Keys.INVULNERABILITY_TICKS, 100000);
                    shulker.offer(Keys.AI_ENABLED, false);

                    EntitySpawnCause spawnCause = EntitySpawnCause.builder().entity(shulker)
                            .type(SpawnTypes.CUSTOM).build();
                    world.spawnEntity(shulker, Cause.source(spawnCause).build());
                    spawnedEntities.add(shulker);
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

                Entity armorEnt = world.createEntity(EntityTypes.ARMOR_STAND,
                        iterator.next().toDouble().add(V3D_HALF));

                armorEnt.offer(Keys.HAS_GRAVITY, false);

                EntitySpawnCause spawnCause = EntitySpawnCause.builder().entity(armorEnt)
                        .type(SpawnTypes.CUSTOM).build();
                world.spawnEntity(armorEnt, Cause.source(spawnCause).build());

                Task armorStandTask = Sponge.getScheduler().createTaskBuilder()
                        .delayTicks(5)
                        .intervalTicks(5)
                        .execute((task) -> {
                            if (!iterator.hasNext()) {
                                armorEnt.remove();
                                task.cancel();
                                return;
                            }

                            armorEnt.setLocation(new Location<World>(world, iterator.next()));
                        })
                        .submit(CMcCore.getPlugin());


            }), "armorstand", "a")
            // Fireworks
            .child(CCmdAreaShowDefault.callable((world, blockLocSequence) -> {
                for (Vector3i position : blockLocSequence.blockLocs) {
                    Entity fireworkEnt = world.createEntity(EntityTypes.FIREWORK, position.toDouble().add(V3D_HALF));
                    FireworkEffect fireworkEffect = FireworkEffect.builder().color(Color.GREEN).trail(true).build();
                    ArrayList<FireworkEffect> effects = new ArrayList<>();
                    effects.add(fireworkEffect);

                    fireworkEnt.offer(Keys.FIREWORK_EFFECTS, effects);
                    EntitySpawnCause spawnCause = EntitySpawnCause.builder().entity(fireworkEnt)
                            .type(SpawnTypes.CUSTOM).build();
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
            }), "resetfake", "rf", "rb", "c")
            .build();

    private CCmdAreaShow() {
    }
}
