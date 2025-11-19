package hs.elementmod.elements.abilities.impl.air;

import hs.elementmod.ElementMod;
import hs.elementmod.elements.abilities.BaseAbility;
import hs.elementmod.elements.ElementContext;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AirDashAbility extends BaseAbility {
    private final ElementMod mod;

    // Thread-safe map to track active dashes for multiple players
    private static final Map<UUID, DashData> activeDashes = new ConcurrentHashMap<>();

    public AirDashAbility(ElementMod mod) {
        super("air_dash", 75, 5, 1);
        this.mod = mod;
        registerTickHandler();
    }

    @Override
    public boolean execute(ElementContext context) {
        ServerPlayerEntity player = context.getPlayer();
        ServerWorld world = (ServerWorld) player.getEntityWorld();

        Vec3d direction = player.getRotationVec(1.0f);
        direction = new Vec3d(
                direction.x,
                Math.max(direction.y, 0.5),
                direction.z
        );

        player.setVelocity(direction.multiply(2.5));
        player.velocityModified = true;

        // Play dash sound
        world.playSound(null, player.getBlockPos(),
                SoundEvents.ENTITY_BAT_TAKEOFF, SoundCategory.PLAYERS,
                1.0f, 1.5f);

        // Add this dash to the active dashes map
        activeDashes.put(player.getUuid(), new DashData(player, context));

        return true;
    }

    private void registerTickHandler() {
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            if (activeDashes.isEmpty()) return;

            Iterator<Map.Entry<UUID, DashData>> iterator = activeDashes.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<UUID, DashData> entry = iterator.next();
                DashData dash = entry.getValue();
                ServerPlayerEntity player = dash.player;

                if (!player.isAlive() || player.isRemoved() || dash.ticks >= 20) {
                    iterator.remove();
                    continue;
                }

                // Corrected: use getX/Y/Z to construct Vec3d
                Vec3d loc = new Vec3d(player.getX(), player.getY(), player.getZ());
                world.spawnParticles(ParticleTypes.CLOUD,
                        loc.x, loc.y, loc.z,
                        5, 0.3, 0.3, 0.3, 0.05);

                if (dash.ticks % 5 == 0) {
                    for (LivingEntity entity : world.getEntitiesByClass(
                            LivingEntity.class,
                            player.getBoundingBox().expand(3.0),
                            e -> e != player && isValidTarget(dash.context, e))) {

                        Vec3d entityPos = new Vec3d(entity.getX(), entity.getY(), entity.getZ());
                        Vec3d knockback = entityPos.subtract(loc).normalize();
                        knockback = new Vec3d(knockback.x, 0.2, knockback.z).multiply(1.0);
                        entity.setVelocity(knockback);
                        entity.velocityModified = true;

                        world.spawnParticles(ParticleTypes.CLOUD,
                                entity.getX(), entity.getY(), entity.getZ(),
                                10, 0.3, 0.3, 0.3, 0.05);
                    }
                }

                dash.ticks++;
            }
        });
    }

    @Override
    protected boolean isValidTarget(ElementContext context, LivingEntity entity) {
        if (entity.equals(context.getPlayer())) return false;

        if (entity instanceof ServerPlayerEntity targetPlayer) {
            return !context.getTrustManager().isTrusted(
                    context.getPlayer().getUuid(),
                    targetPlayer.getUuid()
            );
        }

        return true;
    }

    @Override
    public String getName() {
        return Formatting.WHITE + "Air Dash";
    }

    @Override
    public String getDescription() {
        return "Dash forward with incredible speed, pushing away any enemies in your path.";
    }

    // Inner class to track dash data per player
    private static class DashData {
        final ServerPlayerEntity player;
        final ElementContext context;
        int ticks;

        DashData(ServerPlayerEntity player, ElementContext context) {
            this.player = player;
            this.context = context;
            this.ticks = 0;
        }
    }
}
