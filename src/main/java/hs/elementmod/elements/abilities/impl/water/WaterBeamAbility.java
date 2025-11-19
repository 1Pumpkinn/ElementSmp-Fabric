package hs.elementmod.elements.abilities.impl.water;

import hs.elementmod.ElementMod;
import hs.elementmod.elements.ElementContext;
import hs.elementmod.elements.abilities.BaseAbility;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WaterBeamAbility extends BaseAbility {
    private final ElementMod mod;
    private final Set<UUID> activeUsers = ConcurrentHashMap.newKeySet();
    private final Map<UUID, BeamState> activeBeams = new ConcurrentHashMap<>();

    public WaterBeamAbility(ElementMod mod) {
        super("water_beam", 50, 15, 2);
        this.mod = mod;

        // Server tick listener for beam updates
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (BeamState state : activeBeams.values()) {
                updateBeam(state);
            }
        });
    }

    @Override
    public boolean execute(ElementContext context) {
        ServerPlayerEntity player = context.getPlayer();

        if (!(player.getEntityWorld() instanceof ServerWorld world)) {
            return false; // Only run on server
        }

        // Play sound
        world.playSound(
                null,
                player.getX(), player.getY(), player.getZ(),
                SoundEvents.ITEM_TRIDENT_RIPTIDE_3,
                SoundCategory.PLAYERS,
                1f, 1.2f
        );

        setActive(player, true);

        // Add beam to active map
        activeBeams.put(player.getUuid(), new BeamState(player));

        return true;
    }

    private void updateBeam(BeamState state) {
        ServerPlayerEntity player = state.player;

        if (!(player.getEntityWorld() instanceof ServerWorld world)) return;

        if (!player.isAlive() || player.isRemoved() || state.ticks >= 200 || state.totalDamage >= 10) {
            activeBeams.remove(player.getUuid());
            setActive(player, false);
            return;
        }

        Vec3d direction = player.getRotationVec(1.0f).normalize();
        Vec3d chestPos = new Vec3d(player.getX(), player.getY() + 1.2, player.getZ());

        // Raycast blocks
        double maxDistance = 20.0;
        BlockHitResult blockHit = world.raycast(new RaycastContext(
                chestPos,
                chestPos.add(direction.multiply(maxDistance)),
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                player
        ));
        if (blockHit.getType() != HitResult.Type.MISS) {
            maxDistance = chestPos.distanceTo(blockHit.getPos());
        }

        // Raycast entities
        EntityHitResult entityHit = raycastEntities(world, player, chestPos, direction, maxDistance);
        if (entityHit != null && entityHit.getEntity() instanceof LivingEntity target) {
            if (isValidTarget(player, target)) {
                Vec3d knockback = new Vec3d(target.getX(), target.getY(), target.getZ())
                        .subtract(new Vec3d(player.getX(), player.getY(), player.getZ()))
                        .normalize().add(0, 0.2, 0).multiply(0.8);
                target.setVelocity(knockback);
                target.velocityModified = true;

                // Deal damage
                if (state.totalDamage < 10) {
                    double damageAmount = Math.min(0.5, 10 - state.totalDamage);
                    target.damage(world, player.getDamageSources().magic(), (float) damageAmount);
                    state.totalDamage += damageAmount;

                    Vec3d hitPos = new Vec3d(target.getX(), target.getY(), target.getZ());

                    // Spawn particles
                    world.spawnParticles(
                            ParticleTypes.SPLASH,
                            hitPos.x, hitPos.y, hitPos.z,
                            10, 0.2, 0.2, 0.2, 0.1
                    );
                    world.spawnParticles(
                            ParticleTypes.BUBBLE_POP,
                            hitPos.x, hitPos.y, hitPos.z,
                            5, 0.1, 0.1, 0.1, 0.05
                    );
                }
            }
        }

        // Beam particles along line
        Vec3d eyePos = player.getEyePos();
        double particleDistance = 0.5;
        for (double d = 0; d <= maxDistance; d += particleDistance) {
            Vec3d particlePos = eyePos.add(direction.multiply(d));
            BlockPos blockPos = BlockPos.ofFloored(particlePos);
            if (!isPassableBlock(world.getBlockState(blockPos))) break;

            if (state.ticks % 2 == 0) {
                world.spawnParticles(
                        ParticleTypes.SPLASH,
                        particlePos.x, particlePos.y, particlePos.z,
                        1, 0.05, 0.05, 0.05, 0.01
                );

                world.spawnParticles(
                        ParticleTypes.BUBBLE_POP,
                        particlePos.x, particlePos.y, particlePos.z,
                        1, 0.05, 0.05, 0.05, 0.01
                );
            }
        }

        state.ticks++;
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
        if (active) activeUsers.add(player.getUuid());
        else activeUsers.remove(player.getUuid());
    }

    @Override
    public String getName() {
        return "Water Beam";
    }

    @Override
    public String getDescription() {
        return "Fire a continuous beam of water that damages and pushes back enemies. (40 mana)";
    }

    private static class BeamState {
        public final ServerPlayerEntity player;
        public int ticks;
        public double totalDamage;

        public BeamState(ServerPlayerEntity player) {
            this.player = player;
            this.ticks = 0;
            this.totalDamage = 0;
        }
    }
}
