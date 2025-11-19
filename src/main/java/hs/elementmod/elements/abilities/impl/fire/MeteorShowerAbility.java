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

import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MeteorShowerAbility extends BaseAbility {
    private final ElementMod mod;
    private final Random random = new Random();

    // Track active meteor showers per player
    private static final Map<UUID, MeteorState> activeShowers = new ConcurrentHashMap<>();

    public MeteorShowerAbility(ElementMod mod) {
        super("fire_meteor_shower", 75, 30, 2);
        this.mod = mod;
        registerTickHandler();
    }

    @Override
    public boolean execute(ElementContext context) {
        ServerPlayerEntity player = context.getPlayer();
        ServerWorld world = player.getEntityWorld();
        Vec3d targetLoc = player.getEyePos();

        world.playSound(null, player.getBlockPos(),
                SoundEvents.ENTITY_ENDER_DRAGON_GROWL, SoundCategory.PLAYERS,
                1.0f, 0.5f);

        // Register this meteor shower
        activeShowers.put(player.getUuid(), new MeteorState(player, world, targetLoc));

        return true;
    }

    private void registerTickHandler() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (activeShowers.isEmpty()) return;

            Iterator<Map.Entry<UUID, MeteorState>> iterator = activeShowers.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<UUID, MeteorState> entry = iterator.next();
                MeteorState state = entry.getValue();

                if (!state.player.isAlive() || state.player.isRemoved() || state.meteorsSpawned >= 18) {
                    iterator.remove();
                    continue;
                }

                // Spawn meteors over time (not all at once)
                if (state.tickCounter % 3 == 0 && state.meteorsSpawned < 18) {
                    spawnMeteor(state);
                    state.meteorsSpawned++;
                }

                state.tickCounter++;
            }
        });
    }

    private void spawnMeteor(MeteorState state) {
        // Random location around player
        double offsetX = (random.nextDouble() - 0.5) * 6;
        double offsetZ = (random.nextDouble() - 0.5) * 6;
        Vec3d spawnLoc = state.targetLocation.add(offsetX, 25, offsetZ);

        // Spawn fireball falling downward
        Vec3d direction = new Vec3d(
                (random.nextDouble() - 0.5) * 0.3,
                -1,
                (random.nextDouble() - 0.5) * 0.3
        ).normalize();

        FireballEntity fireball = new FireballEntity(
                state.world, state.player, direction, 1
        );
        fireball.setPosition(spawnLoc);
        state.world.spawnEntity(fireball);

        // Particles
        state.world.spawnParticles(ParticleTypes.FLAME,
                spawnLoc.x, spawnLoc.y, spawnLoc.z,
                30, 0.8, 0.8, 0.8, 0.15);
        state.world.spawnParticles(ParticleTypes.LAVA,
                spawnLoc.x, spawnLoc.y, spawnLoc.z,
                5, 0.5, 0.5, 0.5, 0.0);

        state.world.playSound(null, spawnLoc.x, spawnLoc.y, spawnLoc.z,
                SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.PLAYERS,
                0.6f, 0.7f);
    }

    @Override
    public String getName() {
        return "Meteor Shower";
    }

    @Override
    public String getDescription() {
        return "Rain down fireballs from the sky around your position. (75 mana)";
    }

    private static class MeteorState {
        final ServerPlayerEntity player;
        final ServerWorld world;
        final Vec3d targetLocation;
        int tickCounter = 0;
        int meteorsSpawned = 0;

        MeteorState(ServerPlayerEntity player, ServerWorld world, Vec3d targetLocation) {
            this.player = player;
            this.world = world;
            this.targetLocation = targetLocation;
        }
    }
}