package hs.elementmod.elements.abilities.impl.air;

import hs.elementmod.ElementMod;
import hs.elementmod.elements.abilities.BaseAbility;
import hs.elementmod.elements.ElementContext;
import hs.elementmod.managers.TrustManager;
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

public class AirBlastAbility extends BaseAbility {
    private final ElementMod mod;

    // Track active blasts: player UUID → tick count
    private final Map<UUID, Integer> activeBlasts = new HashMap<>();
    private final Map<UUID, Vec3d> blastCenters = new HashMap<>();
    private final Map<UUID, ServerWorld> blastWorlds = new HashMap<>();

    public AirBlastAbility(ElementMod mod) {
        super("air_blast", 50, 8, 1);
        this.mod = mod;

        // Register server tick listener for particle animation
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            activeBlasts.keySet().removeIf(uuid -> {
                int tick = activeBlasts.get(uuid);
                ServerWorld world = blastWorlds.get(uuid);
                Vec3d center = blastCenters.get(uuid);

                if (world == null || center == null) return true;

                double currentRadius = 1.5 + (tick * 0.8);
                if (currentRadius > 8.0) return true;

                for (int i = 0; i < 360; i += 10) {
                    double rad = Math.toRadians(i);
                    double x = Math.cos(rad) * currentRadius;
                    double z = Math.sin(rad) * currentRadius;

                    int count = Math.max(1, 3 - tick / 2);
                    world.spawnParticles(ParticleTypes.CLOUD,
                            center.x + x, center.y + 0.2, center.z + z,
                            count, 0.0, 0.0, 0.0, 0.0);
                }

                tick++;
                if (tick >= 10) {
                    blastCenters.remove(uuid);
                    blastWorlds.remove(uuid);
                    return true;
                } else {
                    activeBlasts.put(uuid, tick);
                    return false;
                }
            });
        });
    }

    @Override
    public boolean execute(ElementContext context) {
        ServerPlayerEntity player = context.getPlayer();
        TrustManager trust = context.getTrustManager();
        int cost = 20;

        if (!context.getManaManager().hasMana(player, cost)) {
            player.sendMessage(Text.literal("Not enough mana (" + cost + ")")
                    .formatted(Formatting.RED), false);
            return false;
        }

        double radius = 6.0;
        ServerWorld world = player.getEntityWorld();
        Vec3d center = new Vec3d(player.getX(), player.getY(), player.getZ());

        // Spawn initial particle ring
        for (int i = 0; i < 360; i += 10) {
            double rad = Math.toRadians(i);
            double x = Math.cos(rad) * 1.5;
            double z = Math.sin(rad) * 1.5;
            world.spawnParticles(ParticleTypes.CLOUD,
                    center.x + x, center.y + 0.2, center.z + z,
                    2, 0.0, 0.0, 0.0, 0.0);
        }

        // Register this blast for tick-based animation
        UUID uuid = player.getUuid();
        activeBlasts.put(uuid, 0);
        blastCenters.put(uuid, center);
        blastWorlds.put(uuid, world);

        // Launch nearby entities
        world.getEntitiesByClass(LivingEntity.class,
                        player.getBoundingBox().expand(radius),
                        entity -> entity != player)
                .forEach(entity -> {
                    if (entity instanceof ServerPlayerEntity other) {
                        if (trust.isTrusted(player.getUuid(), other.getUuid())) return;
                    }

                    Vec3d push = new Vec3d(entity.getX(), entity.getY(), entity.getZ())
                            .subtract(center)
                            .normalize()
                            .multiply(2.25)
                            .add(0, 1.5, 0);

                    entity.setVelocity(push);
                    entity.velocityModified = true;
                });

        // Play sound effect
        world.playSound(null, player.getBlockPos(),
                SoundEvents.ENTITY_ENDER_DRAGON_FLAP, SoundCategory.PLAYERS,
                1f, 1.5f);

        return true;
    }

    @Override
    public String getName() {
        return "Air Blast";
    }

    @Override
    public String getDescription() {
        return "Create a powerful blast of air that pushes enemies away from you.";
    }
}
