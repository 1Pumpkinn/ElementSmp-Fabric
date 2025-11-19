package hs.elementmod.elements.abilities.impl.air;

import hs.elementmod.ElementMod;
import hs.elementmod.elements.abilities.BaseAbility;
import hs.elementmod.elements.ElementContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

public class AirDashAbility extends BaseAbility {
    private final ElementMod mod;

    public AirDashAbility(ElementMod mod) {
        super("air_dash", 75, 5, 1);
        this.mod = mod;
    }

    @Override
    public boolean execute(ElementContext context) {
        ServerPlayerEntity player = context.getPlayer();
        ServerWorld world = (ServerWorld) player.getWorld();

        Vec3d direction = player.getRotationVec(1.0f);
        direction = new Vec3d(
                direction.x,
                Math.max(direction.y, 0.5),
                direction.z
        );

        player.setVelocity(direction.multiply(2.5));
        player.velocityModified = true;

        // Schedule particle and knockback effects
        scheduleDashEffects(player, world, context);

        world.playSound(null, player.getBlockPos(),
                SoundEvents.ENTITY_BAT_TAKEOFF, SoundCategory.PLAYERS,
                1.0f, 1.5f);

        return true;
    }

    private void scheduleDashEffects(ServerPlayerEntity player, ServerWorld world, ElementContext context) {
        new Object() {
            int ticks = 0;

            void run() {
                if (ticks >= 20 || !player.isAlive() || player.isRemoved()) {
                    return;
                }

                Vec3d loc = player.getPos();
                world.spawnParticles(ParticleTypes.CLOUD,
                        loc.x, loc.y, loc.z,
                        5, 0.3, 0.3, 0.3, 0.05);

                if (ticks % 5 == 0) {
                    for (LivingEntity entity : world.getEntitiesByClass(
                            LivingEntity.class,
                            player.getBoundingBox().expand(3.0),
                            e -> e != player && isValidTarget(context, e))) {

                        Vec3d knockback = entity.getPos().subtract(loc).normalize();
                        knockback = new Vec3d(knockback.x, 0.2, knockback.z).multiply(1.0);
                        entity.setVelocity(knockback);
                        entity.velocityModified = true;

                        world.spawnParticles(ParticleTypes.CLOUD,
                                entity.getX(), entity.getY(), entity.getZ(),
                                10, 0.3, 0.3, 0.3, 0.05);
                    }
                }

                ticks++;

                // Schedule next tick
                if (ticks < 20 && player.isAlive() && !player.isRemoved()) {
                    mod.getServer().execute(this::run);
                }
            }
        }.run();
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
}