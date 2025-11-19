package hs.elementmod.listeners;

import hs.elementmod.ElementMod;
import hs.elementmod.data.PlayerData;
import hs.elementmod.elements.ElementType;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Handles fall damage cancellation for Air element players
 * Uses Fabric's ServerLivingEntityEvents.ALLOW_DAMAGE event
 */
public class FallDamageListener {

    public static void register() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            // Only handle player fall damage
            if (!(entity instanceof ServerPlayerEntity player)) {
                return true; // Allow damage for non-players
            }

            // Check if damage is from falling
            if (!source.isOf(DamageTypes.FALL)) {
                return true; // Allow non-fall damage
            }

            // Check if player has Air element
            PlayerData pd = ElementMod.getInstance().getElementManager().data(player.getUuid());
            if (pd == null || pd.getCurrentElement() != ElementType.AIR) {
                return true; // Allow fall damage for non-Air players
            }

            // Cancel fall damage for Air element players
            return false; // false = cancel damage
        });
    }
}