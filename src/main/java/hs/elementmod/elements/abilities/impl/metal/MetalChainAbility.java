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
    private static final Map<UUID, Integer> stunnedTicks = new ConcurrentHashMap<>();

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
            // Create chain state
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

                if (state.ticks >= 40) {
                    // finished - apply stun to target
                    stunnedTicks.put(target.getUuid(), 60);
                    it.remove();
                    continue;
                }

                // Pull target toward player's eye
                Vec3d eye = player.getEyePos();
                Vec3d targetPos = new Vec3d(target.getX(), target.getY(), target.getZ());
                Vec3d pull = eye.subtract(targetPos).normalize().multiply(0.6).add(0, 0.05, 0);
                target.setVelocity(pull);
                target.velocityModified = true;

                // chain particles along line
                for (int i = 0; i < 6; i++) {
                    double t = i / 6.0;
                    Vec3d pos = targetPos.lerp(eye, t);
                    world.spawnParticles(ParticleTypes.END_ROD, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
                }

                state.ticks++;
            }

            // Decrease stun ticks and enforce stun
            Iterator<Map.Entry<UUID, Integer>> sit = stunnedTicks.entrySet().iterator();
            while (sit.hasNext()) {
                Map.Entry<UUID, Integer> se = sit.next();
                UUID uuid = se.getKey();
                int remaining = se.getValue() - 1;
                if (remaining <= 0) {
                    sit.remove();
                    continue;
                }
                se.setValue(remaining);

                // Attempt to get player and freeze movement
                ServerPlayerEntity p = world.getServer().getPlayerManager().getPlayer(uuid);
                if (p != null && p.isAlive() && !p.isRemoved()) {
                    p.setVelocity(0, 0, 0);
                    p.velocityModified = true;
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
