package hs.elementmod.elements.abilities.impl.metal;

import hs.elementmod.ElementMod;
import hs.elementmod.elements.ElementContext;
import hs.elementmod.elements.abilities.BaseAbility;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Metal chain ability: finds a target in a cone and reels them toward the player.
 */
public class MetalChainAbility extends BaseAbility {
    private static final Map<UUID, ChainState> activeChains = new ConcurrentHashMap<>();
    // store stun expiry in epoch millis for players and entities
    private static final Map<UUID, Long> stunnedExpiry = new ConcurrentHashMap<>();

    public MetalChainAbility(ElementMod mod) {
        super("metal_chain", 50, 10, 1);
    registerTickHandler();
    }

    @Override
    public boolean execute(ElementContext context) {
        ServerPlayerEntity player = context.getPlayer();

        if (!(player.getEntityWorld() instanceof ServerWorld world)) return false;

        // Search for nearest valid living entity within cone
        double range = 20.0;
        double coneDegrees = 25.0;
        double coneCos = Math.cos(Math.toRadians(coneDegrees));

        Vec3d eye = player.getEyePos();
        Vec3d look = player.getRotationVec(1.0f).normalize();

        LivingEntity chosen = null;
        double bestDist = Double.MAX_VALUE;

            for (LivingEntity entity : world.getEntitiesByClass(LivingEntity.class, player.getBoundingBox().expand(range), e -> e != player)) {
            if (!isValidTarget(context, entity)) continue;

            Vec3d toEntity = new Vec3d(entity.getX(), entity.getEyeY(), entity.getZ()).subtract(eye);
            if (toEntity.lengthSquared() > range * range) continue;
            Vec3d dir = toEntity.normalize();
            double dot = look.x * dir.x + look.y * dir.y + look.z * dir.z;
            if (dot >= coneCos) {
                double d = player.squaredDistanceTo(entity);
                if (d < bestDist) {
                    bestDist = d;
                    chosen = entity;
                }
            }
        }

        if (chosen != null) {
            // Create chain state with smoother reel
            activeChains.put(player.getUuid(), new ChainState(player, chosen));
            world.playSound(null, player.getBlockPos(), net.minecraft.sound.SoundEvents.BLOCK_ANVIL_LAND, net.minecraft.sound.SoundCategory.PLAYERS, 1.0f, 1.0f);
            return true;
        }

        return false;
    }

    private void registerTickHandler() {
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            if (activeChains.isEmpty() && stunnedTicks.isEmpty()) return;

            Iterator<Map.Entry<UUID, ChainState>> it = activeChains.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<UUID, ChainState> entry = it.next();
                ChainState state = entry.getValue();
                ServerPlayerEntity player = state.player;
                LivingEntity target = state.target;

                if (!player.isAlive() || player.isRemoved() || !target.isAlive() || target.isRemoved()) {
                    it.remove();
                    continue;
                }

                // Smooth reel: shorten duration as target gets closer
                Vec3d eye = player.getEyePos();
                Vec3d targetPos = new Vec3d(target.getX(), target.getY(), target.getZ());
                double distance = eye.distanceTo(targetPos);


                if (distance < 1.2 || state.ticks >= 60) {
                    // finished - apply stun to target (store expiry in millis)
                    long expiry = System.currentTimeMillis() + 60L * 50L; // 60 ticks -> ~3000ms
                    stunnedExpiry.put(target.getUuid(), expiry);
                    it.remove();
                    continue;
                }

                // Desired attach point slightly in front of player's eye
                Vec3d look = player.getRotationVec(1.0f).normalize();
                Vec3d desired = eye.subtract(look.multiply(0.6));

                // Compute smooth velocity toward desired position
                Vec3d delta = desired.subtract(targetPos);
                // smoothing factor (smaller = smoother), then cap to avoid teleporting
                Vec3d vel = delta.multiply(0.22);
                double maxVel = 0.9; // cap velocity per tick
                double vlen = Math.sqrt(vel.x * vel.x + vel.y * vel.y + vel.z * vel.z);
                if (vlen > maxVel) {
                    vel = vel.multiply(maxVel / vlen);
                }
                // small upward bias so target doesn't clip into player
                vel = vel.add(0, 0.06, 0);
                target.setVelocity(vel);
                target.velocityModified = true;

                // more frequent particles for smoother visual chain
                int steps = (int) Math.min(12, Math.max(6, Math.ceil(distance * 3)));
                for (int i = 0; i <= steps; i++) {
                    double t = (double) i / (double) Math.max(1, steps);
                    Vec3d pos = targetPos.lerp(eye, t);
                    // small metallic spark and smoke
                    world.spawnParticles(ParticleTypes.END_ROD, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
                    if (i % 3 == 0) world.spawnParticles(ParticleTypes.SMOKE, pos.x, pos.y, pos.z, 1, 0.01, 0.01, 0.01, 0.0);
                }

                state.ticks++;
            }

            // Enforce stun by expiry timestamp; freeze movement and cancel knockback by zeroing velocity
            long now = System.currentTimeMillis();
            Iterator<Map.Entry<UUID, Long>> sit = stunnedExpiry.entrySet().iterator();
            while (sit.hasNext()) {
                Map.Entry<UUID, Long> se = sit.next();
                UUID uuid = se.getKey();
                long expiry = se.getValue();
                if (now >= expiry) {
                    sit.remove();
                    continue;
                }

                // Attempt to get an entity first (players and mobs)
                ServerPlayerEntity p = world.getServer().getPlayerManager().getPlayer(uuid);
                if (p != null && p.isAlive() && !p.isRemoved()) {
                    p.setVelocity(0, 0, 0);
                    p.velocityModified = true;
                    // also teleport slightly to avoid small drift due to server corrections
                    // keep position unchanged to prevent sneak-like drift
                } else {
                    // try as generic entity
                    var maybe = world.getEntity(uuid);
                    if (maybe instanceof LivingEntity ent && ent.isAlive() && !ent.isRemoved()) {
                        ent.setVelocity(0, 0, 0);
                        ent.velocityModified = true;
                    }
                }
            }
        });
    }

    @Override
    protected boolean isValidTarget(ElementContext context, LivingEntity target) {
        if (target.equals(context.getPlayer())) return false;
        if (target instanceof ServerPlayerEntity targetPlayer) {
            return !context.getTrustManager().isTrusted(context.getPlayer().getUuid(), targetPlayer.getUuid());
        }
        return true;
    }

    @Override
    public String getName() {
        return Formatting.GRAY + "Metal Chain";
    }

    @Override
    public String getDescription() {
        return "Reel in an enemy with magnetic chains, briefly stunning them.";
    }

    private static class ChainState {
        final ServerPlayerEntity player;
        final LivingEntity target;
    // no context stored here
        int ticks = 0;

        ChainState(ServerPlayerEntity player, LivingEntity target) {
            this.player = player;
            this.target = target;
        }
    }
}
