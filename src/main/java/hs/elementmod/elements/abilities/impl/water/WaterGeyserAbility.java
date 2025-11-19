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

/**
 * Water element's geyser ability that launches entities upward
 * Complete Fabric conversion showing particle and sound handling
 */
public class WaterGeyserAbility extends BaseAbility {
    private final ElementMod mod;

    public WaterGeyserAbility(ElementMod mod) {
        super("water_geyser", 75, 5, 1);
        this.mod = mod;
    }

    @Override
    public boolean execute(ElementContext context) {
        ServerPlayerEntity player = context.getPlayer();
        ServerWorld world = (ServerWorld) player.getWorld();
        Vec3d playerPos = player.getPos();
        boolean foundTargets = false;

        // Find nearby entities to launch
        for (LivingEntity entity : world.getEntitiesByClass(
                LivingEntity.class,
                player.getBoundingBox().expand(5.0),
                e -> e != player && isValidTarget(context, e)
        )) {
            foundTargets = true;
            final LivingEntity target = entity;
            final double startY = target.getY();

            // Play water sound
            world.playSound(
                    null,
                    target.getX(), startY, target.getZ(),
                    SoundEvents.BLOCK_WATER_AMBIENT,
                    SoundCategory.PLAYERS,
                    1.0f,
                    0.5f
            );

            // Spawn initial splash particles
            world.spawnParticles(
                    ParticleTypes.SPLASH,
                    target.getX(), startY, target.getZ(),
                    30,           // count
                    0.5, 0.1, 0.5,  // spread
                    0.3           // speed
            );

            // Start geyser animation
            scheduleGeyserAnimation(target, startY, world);
        }

        if (!foundTargets) {
            player.sendMessage(
                    Text.literal("No valid targets found!")
                            .formatted(Formatting.RED),
                    false
            );
            return false;
        }

        return true;
    }

    private void scheduleGeyserAnimation(LivingEntity target, double startY, ServerWorld world) {
        new Object() {
            int ticks = 0;
            double geyserHeight = 0;
            double lastGeyserHeight = 0;

            void run() {
                if (!target.isAlive() || target.isRemoved() || ticks >= 40) {
                    return;
                }

                Vec3d targetPos = target.getPos();
                double currentHeight = targetPos.y - startY;

                // Launch entity upward (max 20 blocks)
                if (currentHeight < 20) {
                    target.setVelocity(0, 0.8, 0);
                    target.velocityModified = true;
                } else {
                    target.setVelocity(0, 0, 0);
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

                // Calculate smooth geyser height transition
                double targetHeight = Math.min(currentHeight, 20);
                double heightDiff = targetHeight - lastGeyserHeight;
                geyserHeight = lastGeyserHeight + Math.min(heightDiff, 0.5);
                lastGeyserHeight = geyserHeight;

                // Create particle column from ground to entity
                for (double y = 0; y <= geyserHeight; y += 0.25) {
                    if (y % 0.5 != 0 && y > 1) continue;

                    double spread = 0.15 * (1 - (y / Math.max(geyserHeight, 1)));

                    world.spawnParticles(
                            ParticleTypes.BUBBLE_COLUMN_UP,
                            targetPos.x, startY + y, targetPos.z,
                            1,
                            spread, 0.05, spread,
                            0.01
                    );

                    // Add underwater particles occasionally
                    if (y % 1.5 < 0.25 && y > 0.5) {
                        world.spawnParticles(
                                ParticleTypes.UNDERWATER,
                                targetPos.x, startY + y, targetPos.z,
                                1,
                                spread, 0.05, spread,
                                0.01
                        );
                    }
                }

                ticks++;

                // Schedule next tick
                if (ticks < 40 && target.isAlive() && !target.isRemoved()) {
                    mod.getServer().execute(this::run);
                }
            }
        }.run();
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
        // Check if entity is a valid target (not a friendly player)
        if (entity instanceof ServerPlayerEntity targetPlayer) {
            // Don't target trusted players
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