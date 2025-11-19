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

public class AirBlastAbility extends BaseAbility {
    private final ElementMod mod;

    public AirBlastAbility(ElementMod mod) {
        super("air_blast", 50, 8, 1);
        this.mod = mod;
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
        ServerWorld world = (ServerWorld) player.getWorld();
        Vec3d center = player.getPos();

        // Particle ring
        for (int i = 0; i < 360; i += 10) {
            double rad = Math.toRadians(i);
            double x = Math.cos(rad) * 1.5;
            double z = Math.sin(rad) * 1.5;
            world.spawnParticles(ParticleTypes.CLOUD,
                    center.x + x, center.y + 0.2, center.z + z,
                    2, 0.0, 0.0, 0.0, 0.0);
        }

        // Animated particle ring that shoots outward
        mod.getServer().execute(() -> {
            new Object() {
                int tick = 0;

                void run() {
                    if (tick >= 10) return;

                    double currentRadius = 1.5 + (tick * 0.8);
                    if (currentRadius > 8.0) return;

                    for (int i = 0; i < 360; i += 10) {
                        double rad = Math.toRadians(i);
                        double x = Math.cos(rad) * currentRadius;
                        double z = Math.sin(rad) * currentRadius;

                        int count = Math.max(1, 3 - tick/2);
                        world.spawnParticles(ParticleTypes.CLOUD,
                                center.x + x, center.y + 0.2, center.z + z,
                                count, 0.0, 0.0, 0.0, 0.0);
                    }

                    tick++;
                    if (tick < 10) {
                        mod.getServer().execute(this::run);
                    }
                }
            }.run();
        });

        // Launch nearby entities
        world.getEntitiesByClass(LivingEntity.class,
                        player.getBoundingBox().expand(radius),
                        entity -> entity != player)
                .forEach(entity -> {
                    if (entity instanceof ServerPlayerEntity other) {
                        if (trust.isTrusted(player.getUuid(), other.getUuid())) return;
                    }

                    Vec3d push = entity.getPos().subtract(center).normalize()
                            .multiply(2.25).add(0, 1.5, 0);
                    entity.setVelocity(push);
                    entity.velocityModified = true;
                });

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