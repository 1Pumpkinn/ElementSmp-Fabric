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
 * Metal dash: a fast dash that damages and knocks back entities; if it hits no one the user is briefly stunned.
 */
public class MetalDashAbility extends BaseAbility {
    private static final Map<UUID, DashState> activeDashes = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> stunnedTicks = new ConcurrentHashMap<>();

    public MetalDashAbility(ElementMod mod) {
        super("metal_dash", 80, 8, 1);
        registerTickHandler();
    }

    @Override
    public boolean execute(ElementContext context) {
        ServerPlayerEntity player = context.getPlayer();

        if (!(player.getEntityWorld() instanceof ServerWorld world)) return false;

        Vec3d dir = player.getRotationVec(1.0f);
        dir = new Vec3d(dir.x, Math.max(dir.y, 0.1), dir.z);
        player.setVelocity(dir.multiply(3.0));
        player.velocityModified = true;

        world.playSound(null, player.getBlockPos(), net.minecraft.sound.SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, net.minecraft.sound.SoundCategory.PLAYERS, 1.0f, 1.2f);

        activeDashes.put(player.getUuid(), new DashState(player, context));
        return true;
    }

    private void registerTickHandler() {
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            if (activeDashes.isEmpty() && stunnedTicks.isEmpty()) return;

            Iterator<Map.Entry<UUID, DashState>> iterator = activeDashes.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<UUID, DashState> entry = iterator.next();
                DashState dash = entry.getValue();
                ServerPlayerEntity player = dash.player;

                if (!player.isAlive() || player.isRemoved() || dash.ticks >= 20) {
                    // end dash
                    iterator.remove();
                    if (!dash.hitSomeone) {
                        stunnedTicks.put(player.getUuid(), 40);
                    }
                    continue;
                }

                Vec3d loc = new Vec3d(player.getX(), player.getY(), player.getZ());
                world.spawnParticles(ParticleTypes.SMOKE, loc.x, loc.y + 1.0, loc.z, 6, 0.3, 0.3, 0.3, 0.02);

                // Check for entities to hit
                if (dash.ticks % 2 == 0) {
                    for (LivingEntity entity : world.getEntitiesByClass(LivingEntity.class, player.getBoundingBox().expand(3.0), e -> e != player && isValidTarget(dash.context, e))) {
                        Vec3d entityPos = new Vec3d(entity.getX(), entity.getY(), entity.getZ());
                        Vec3d knock = entityPos.subtract(loc).normalize().add(0, 0.2, 0).multiply(1.0);
                        entity.setVelocity(knock);
                        entity.velocityModified = true;

                        // Damage
                        entity.damage(world, player.getDamageSources().magic(), 4.0f);

                        world.spawnParticles(ParticleTypes.CLOUD, entity.getX(), entity.getY(), entity.getZ(), 10, 0.3, 0.3, 0.3, 0.05);
                        dash.hitSomeone = true;
                    }
                }

                dash.ticks++;
            }

            // decrease stun ticks and freeze affected players
            Iterator<Map.Entry<UUID, Integer>> sit = stunnedTicks.entrySet().iterator();
            while (sit.hasNext()) {
                Map.Entry<UUID, Integer> se = sit.next();
                int remaining = se.getValue() - 1;
                if (remaining <= 0) {
                    sit.remove();
                    continue;
                }
                se.setValue(remaining);
                ServerPlayerEntity p = world.getServer().getPlayerManager().getPlayer(se.getKey());
                if (p != null && p.isAlive() && !p.isRemoved()) {
                    p.setVelocity(0, 0, 0);
                    p.velocityModified = true;
                }
            }
        });
    }

    @Override
    protected boolean isValidTarget(ElementContext context, LivingEntity entity) {
        if (entity.equals(context.getPlayer())) return false;
        if (entity instanceof ServerPlayerEntity targetPlayer) {
            return !context.getTrustManager().isTrusted(context.getPlayer().getUuid(), targetPlayer.getUuid());
        }
        return true;
    }

    @Override
    public String getName() {
        return Formatting.GRAY + "Metal Dash";
    }

    @Override
    public String getDescription() {
        return "Charge forward in a metallic rush, damaging enemies in your path. If you hit nobody you will be stunned.";
    }

    private static class DashState {
        final ServerPlayerEntity player;
        int ticks = 0;
        boolean hitSomeone = false;
        final ElementContext context;

        DashState(ServerPlayerEntity player, ElementContext context) {
            this.player = player;
            this.context = context;
        }
    }
}
