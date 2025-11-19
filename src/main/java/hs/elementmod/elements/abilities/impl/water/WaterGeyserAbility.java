package hs.elementmod.elements.abilities.impl.water;

import hs.elementmod.ElementMod;
import hs.elementmod.elements.ElementContext;
import hs.elementmod.elements.abilities.BaseAbility;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Water element's geyser ability that launches entities upward
 */
public class WaterGeyserAbility extends BaseAbility {
    private final ElementMod mod;

    // Active geysers tracked per entity UUID
    private final Map<UUID, Integer> activeTicks = new HashMap<>();
    private final Map<UUID, Vec3d> startPositions = new HashMap<>();
    private final Map<UUID, LivingEntity> targets = new HashMap<>();
    private final int maxTicks = 40;

    public WaterGeyserAbility(ElementMod mod) {
        super("water_geyser", 75, 5, 1);
        this.mod = mod;

        // Server tick listener for geyser animation
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            activeTicks.keySet().removeIf(uuid -> {
                LivingEntity target = targets.get(uuid);
                Vec3d startPos = startPositions.get(uuid);
                int tick = activeTicks.get(uuid);

                if (target == null || !target.isAlive() || tick >= maxTicks) {
                    targets.remove(uuid);
                    startPositions.remove(uuid);
                    return true;
                }

                ServerWorld world = (ServerWorld) target.getEntityWorld();
                Vec3d targetPos = new Vec3d(target.getX(), target.getY(), target.getZ());

                // Launch entity upward (max 20 blocks)
                double height = targetPos.y - startPos.y;
                if (height < 20) {
                    target.setVelocity(0, 0.8, 0);
                    target.velocityModified = true;
                }

                // Spawn bubble particles at entity
                world.spawnParticles(
                        ParticleTypes.BUBBLE_COLUMN_UP,
                        targetPos.x, targetPos.y - 0.01, targetPos.z,
                        5,
                        0.2, 0.0, 0.2,
                        0.01
                );

                // Particle column from ground to entity
                double geyserHeight = Math.min(height, 20);
                for (double y = 0; y <= geyserHeight; y += 0.25) {
                    if (y % 0.5 != 0 && y > 1) continue;
                    double spread = 0.15 * (1 - (y / Math.max(geyserHeight, 1)));

                    world.spawnParticles(
                            ParticleTypes.BUBBLE_COLUMN_UP,
                            targetPos.x, startPos.y + y, targetPos.z,
                            1,
                            spread, 0.05, spread,
                            0.01
                    );

                    if (y % 1.5 < 0.25 && y > 0.5) {
                        world.spawnParticles(
                                ParticleTypes.UNDERWATER,
                                targetPos.x, startPos.y + y, targetPos.z,
                                1,
                                spread, 0.05, spread,
                                0.01
                        );
                    }
                }

                activeTicks.put(uuid, tick + 1);
                return false;
            });
        });
    }

    @Override
    public boolean execute(ElementContext context) {
        ServerPlayerEntity player = context.getPlayer();
        ServerWorld world = (ServerWorld) player.getEntityWorld();
        Vec3d playerPos = new Vec3d(player.getX(), player.getY(), player.getZ());
        boolean foundTargets = false;

        for (LivingEntity entity : world.getEntitiesByClass(
                LivingEntity.class,
                player.getBoundingBox().expand(5.0),
                e -> e != player && isValidTarget(context, e)
        )) {
            foundTargets = true;
            Vec3d startPos = new Vec3d(entity.getX(), entity.getY(), entity.getZ());

            // Play water sound
            world.playSound(
                    null,
                    entity.getX(), entity.getY(), entity.getZ(),
                    SoundEvents.BLOCK_WATER_AMBIENT,
                    SoundCategory.PLAYERS,
                    1.0f,
                    0.5f
            );

            // Spawn initial splash particles
            world.spawnParticles(
                    ParticleTypes.SPLASH,
                    entity.getX(), entity.getY(), entity.getZ(),
                    30,
                    0.5, 0.1, 0.5,
                    0.3
            );

            // Register entity for tick-based geyser animation
            UUID uuid = entity.getUuid();
            activeTicks.put(uuid, 0);
            startPositions.put(uuid, startPos);
            targets.put(uuid, entity);
        }

        if (!foundTargets) {
            player.sendMessage(
                    Text.literal("No valid targets found!").formatted(Formatting.RED),
                    false
            );
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return "Water Geyser";
    }

    @Override
    public String getDescription() {
        return "Launches nearby enemies upward with a powerful geyser.";
    }

    @Override
    protected boolean isValidTarget(ElementContext context, LivingEntity entity) {
        if (entity instanceof ServerPlayerEntity targetPlayer) {
            if (context.getMod().getTrustManager().isTrusted(
                    context.getPlayer().getUuid(),
                    targetPlayer.getUuid()
            )) {
                return false;
            }
        }
        return true;
    }
}
