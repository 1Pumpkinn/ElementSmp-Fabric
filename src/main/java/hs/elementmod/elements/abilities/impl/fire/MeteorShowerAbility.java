package hs.elementmod.elements.abilities.impl.fire;
import hs.elementmod.ElementMod;
import hs.elementmod.elements.abilities.BaseAbility;
import hs.elementmod.elements.ElementContext;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MeteorShowerAbility extends BaseAbility {
    private final ElementMod mod;
    private final Random random = new Random();

    public MeteorShowerAbility(ElementMod mod) {
        super("fire_meteor_shower", 75, 30, 2);
        this.mod = mod;
    }

    @Override
    public boolean execute(ElementContext context) {
        ServerPlayerEntity player = context.getPlayer();
        ServerWorld world = player.getServerWorld();
        Vec3d targetLoc = player.getPos();

        world.playSound(null, player.getBlockPos(),
                SoundEvents.ENTITY_ENDER_DRAGON_GROWL, SoundCategory.PLAYERS,
                1.0f, 0.5f);

        // Track meteor spawning
        Map<Integer, Boolean> meteorState = new HashMap<>();
        meteorState.put(0, false); // count

        // Spawn meteors over time
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (meteorState.get(0)) return;

            int count = meteorState.keySet().size() - 1;
            if (count >= 18) {
                meteorState.put(0, true);
                return;
            }

            // Random location around player
            double offsetX = (random.nextDouble() - 0.5) * 6;
            double offsetZ = (random.nextDouble() - 0.5) * 6;
            Vec3d spawnLoc = targetLoc.add(offsetX, 25, offsetZ);

            // Spawn fireball falling downward
            Vec3d direction = new Vec3d(
                    (random.nextDouble() - 0.5) * 0.3,
                    -1,
                    (random.nextDouble() - 0.5) * 0.3
            ).normalize();

            FireballEntity fireball = new FireballEntity(
                    world, player, direction, 1
            );
            fireball.setPosition(spawnLoc);
            world.spawnEntity(fireball);

            // Particles
            world.spawnParticles(ParticleTypes.FLAME,
                    spawnLoc.x, spawnLoc.y, spawnLoc.z,
                    30, 0.8, 0.8, 0.8, 0.15);
            world.spawnParticles(ParticleTypes.LAVA,
                    spawnLoc.x, spawnLoc.y, spawnLoc.z,
                    5, 0.5, 0.5, 0.5, 0.0);

            world.playSound(null, spawnLoc.x, spawnLoc.y, spawnLoc.z,
                    SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.PLAYERS,
                    0.6f, 0.7f);

            meteorState.put(count + 1, false);
        });

        return true;
    }

    @Override
    public String getName() {
        return "Meteor Shower";
    }

    @Override
    public String getDescription() {
        return "Rain down fireballs from the sky around your position. (100 mana)";
    }
}