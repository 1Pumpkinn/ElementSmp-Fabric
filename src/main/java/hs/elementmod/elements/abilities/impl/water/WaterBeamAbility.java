package hs.elementmod.elements.abilities.impl.water;

import hs.elementmod.ElementMod;
import hs.elementmod.elements.ElementContext;
import hs.elementmod.elements.abilities.BaseAbility;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Water Beam Ability - Complete Fabric 1.21.1 conversion
 * Demonstrates raycasting, continuous damage, and particle effects in Fabric
 */
public class WaterBeamAbility extends BaseAbility {
    private final Set<UUID> activeUsers = new HashSet<>();
    private final ElementMod mod;

    public WaterBeamAbility(ElementMod mod) {
        super("water_beam", 50, 15, 2);
        this.mod = mod;
    }

    @Override
    public boolean execute(ElementContext context) {
        ServerPlayerEntity player = context.getPlayer();
        ServerWorld world = (ServerWorld) player.getWorld();

        world.playSound(null, player.getBlockPos(),
                SoundEvents.ITEM_TRIDENT_RIPTIDE_3, SoundCategory.PLAYERS,
                1f, 1.2f);

        setActive(player, true);

        // Schedule repeating task for beam effect
        scheduleBeamEffect(player, world);

        return true;
    }

    private void scheduleBeamEffect(ServerPlayerEntity player, ServerWorld world) {
        new Object() {
            int ticks = 0;
            double totalDamageDealt = 0;

            void run() {
                // Ability ends after 10 seconds or 5 hearts of damage
                if (!player.isAlive() || player.isRemoved() || ticks >= 200 || totalDamageDealt >= 10) {
                    setActive(player, false);
                    return;
                }

                Vec3d direction = player.getRotationVec(1.0f).normalize();

                // Apply damage every 0.25 seconds
                if (ticks % 5 == 0) {
                    Vec3d chestPos = player.getPos().add(0, 1.2, 0);

                    // Check for blocks in the way
                    double maxDistance = 20.0;
                    BlockHitResult blockHit = raycastBlocks(world, chestPos, direction, maxDistance);
                    if (blockHit.getType() != HitResult.Type.MISS) {
                        maxDistance = chestPos.distanceTo(blockHit.getPos());
                    }

                    // Trace entities
                    EntityHitResult entityHit = raycastEntities(world, player, chestPos, direction, maxDistance);
                    if (entityHit != null && entityHit.getEntity() instanceof LivingEntity target) {
                        if (isValidTarget(player, target)) {
                            // Apply knockback
                            Vec3d knockback = target.getPos().subtract(player.getPos()).normalize();
                            knockback = knockback.add(0, 0.2, 0).multiply(0.8);
                            target.setVelocity(knockback);
                            target.velocityModified = true;

                            if (totalDamageDealt < 10) {
                                double damageAmount = Math.min(0.5, 10 - totalDamageDealt);
                                target.damage(world.getDamageSources().magic(), (float) damageAmount);
                                totalDamageDealt += damageAmount;

                                Vec3d hitPos = entityHit.getPos();

                                if (target instanceof ServerPlayerEntity) {
                                    world.spawnParticles(ParticleTypes.SPLASH,
                                            hitPos.x, hitPos.y, hitPos.z,
                                            15, 0.3, 0.3, 0.3, 0.2);
                                    world.spawnParticles(ParticleTypes.BUBBLE_POP,
                                            hitPos.x, hitPos.y, hitPos.z,
                                            10, 0.2, 0.2, 0.2, 0.1);
                                    world.playSound(null, BlockPos.ofFloored(hitPos),
                                            SoundEvents.ENTITY_PLAYER_SPLASH, SoundCategory.PLAYERS,
                                            0.8f, 1.5f);

                                    // Create circular water ring effect
                                    for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 4) {
                                        double x = Math.cos(angle) * 0.5;
                                        double z = Math.sin(angle) * 0.5;
                                        world.spawnParticles(ParticleTypes.BUBBLE,
                                                hitPos.x + x, hitPos.y + 0.1, hitPos.z + z,
                                                1, 0.05, 0.05, 0.05, 0.0);
                                    }
                                } else {
                                    world.spawnParticles(ParticleTypes.BUBBLE_COLUMN_UP,
                                            hitPos.x, hitPos.y, hitPos.z,
                                            3, 0.1, 0.1, 0.1, 0.0);
                                }
                            }
                        }
                    }
                }

                // Render beam particles
                Vec3d eyePos = player.getEyePos();
                double maxBeamDistance = 20.0;
                double particleDistance = 0.5;

                for (double d = 0; d <= maxBeamDistance; d += particleDistance) {
                    Vec3d particlePos = eyePos.add(direction.multiply(d));

                    BlockPos blockPos = BlockPos.ofFloored(particlePos);
                    if (!isPassableBlock(world.getBlockState(blockPos))) {
                        break;
                    }

                    if (ticks % 2 == 0) {
                        world.spawnParticles(ParticleTypes.SPLASH,
                                particlePos.x, particlePos.y, particlePos.z,
                                1, 0.05, 0.05, 0.05, 0.01);

                        if (d % 2 < 0.5) {
                            world.spawnParticles(ParticleTypes.BUBBLE_POP,
                                    particlePos.x, particlePos.y, particlePos.z,
                                    1, 0.05, 0.05, 0.05, 0.01);
                        }
                    }
                }

                ticks++;

                // Schedule next tick
                if (ticks < 200 && totalDamageDealt < 10 && player.isAlive() && !player.isRemoved()) {
                    mod.getServer().execute(this::run);
                } else {
                    setActive(player, false);
                }
            }
        }.run();

        return true;
    }

    private BlockHitResult raycastBlocks(ServerWorld world, Vec3d start, Vec3d direction, double maxDistance) {
        Vec3d end = start.add(direction.multiply(maxDistance));
        return world.raycast(new RaycastContext(
                start,
                end,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                null
        ));
    }

    private EntityHitResult raycastEntities(ServerWorld world, ServerPlayerEntity player,
                                            Vec3d start, Vec3d direction, double maxDistance) {
        Vec3d end = start.add(direction.multiply(maxDistance));
        Box box = player.getBoundingBox().stretch(direction.multiply(maxDistance)).expand(1.0);

        EntityHitResult result = null;
        double closestDistance = maxDistance;

        for (LivingEntity entity : world.getEntitiesByClass(LivingEntity.class, box, e -> e != player)) {
            Box entityBox = entity.getBoundingBox().expand(0.3);
            java.util.Optional<Vec3d> hit = entityBox.raycast(start, end);

            if (hit.isPresent()) {
                double distance = start.distanceTo(hit.get());
                if (distance < closestDistance) {
                    closestDistance = distance;
                    result = new EntityHitResult(entity, hit.get());
                }
            }
        }

        return result;
    }

    private boolean isValidTarget(ServerPlayerEntity player, LivingEntity target) {
        if (target.equals(player)) return false;

        if (target instanceof ServerPlayerEntity targetPlayer) {
            return !mod.getTrustManager().isTrusted(player.getUuid(), targetPlayer.getUuid());
        }

        return true;
    }

    private boolean isPassableBlock(BlockState state) {
        if (state.isAir()) return true;

        Block block = state.getBlock();
        return block == Blocks.SHORT_GRASS ||
                block == Blocks.TALL_GRASS ||
                block == Blocks.FERN ||
                block == Blocks.LARGE_FERN ||
                block == Blocks.DEAD_BUSH ||
                block == Blocks.DANDELION ||
                block == Blocks.POPPY ||
                block == Blocks.TORCH ||
                block == Blocks.REDSTONE_TORCH ||
                block == Blocks.WATER ||
                block == Blocks.LAVA;
    }

    @Override
    public boolean isActiveFor(ServerPlayerEntity player) {
        return activeUsers.contains(player.getUuid());
    }

    @Override
    public void setActive(ServerPlayerEntity player, boolean active) {
        if (active) {
            activeUsers.add(player.getUuid());
        } else {
            activeUsers.remove(player.getUuid());
        }
    }

    public void clearEffects(ServerPlayerEntity player) {
        setActive(player, false);
    }

    @Override
    public String getName() {
        return "Water Beam";
    }

    @Override
    public String getDescription() {
        return "Fire a continuous beam of water that damages and pushes back enemies. (40 mana)";
    }
}