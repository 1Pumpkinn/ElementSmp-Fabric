package hs.elementmod.elements.abilities.impl.fire;

import hs.elementmod.ElementMod;
import hs.elementmod.elements.abilities.BaseAbility;
import hs.elementmod.elements.ElementContext;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;


public class FireballAbility extends BaseAbility {
    private final ElementMod mod;

    public FireballAbility(ElementMod mod) {
        super("fire_fireball", 50, 10, 1);
        this.mod = mod;
    }

    @Override
    public boolean execute(ElementContext context) {
        ServerPlayerEntity player = context.getPlayer();
        ServerWorld world = player.getEntityWorld();
        Vec3d direction = player.getRotationVec(1.0f).normalize();

        // Launch fireball
        Vec3d spawnPos = player.getEyePos().add(direction.multiply(1.5));
        FireballEntity fireball = new FireballEntity(
                world,
                player,
                direction.multiply(2.0),
                1 // explosion power
        );
        fireball.setPosition(spawnPos);
        world.spawnEntity(fireball);

        // Play sound
        world.playSound(null, player.getBlockPos(),
                SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.PLAYERS,
                1.0f, 1.0f);

        return true;
    }

    @Override
    public String getName() {
        return "Fireball";
    }

    @Override
    public String getDescription() {
        return "Launch a fireball that damages enemies (50 mana)";
    }
}