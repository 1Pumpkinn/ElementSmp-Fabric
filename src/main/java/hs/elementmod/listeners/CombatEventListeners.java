package hs.elementmod.listeners;

import hs.elementmod.ElementMod;
import hs.elementmod.managers.TrustManager;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;

public class CombatEventListeners {
    public static void register() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient) return ActionResult.PASS;

            if (player instanceof ServerPlayerEntity attacker &&
                    entity instanceof ServerPlayerEntity victim) {

                TrustManager trust = ElementMod.getInstance().getTrustManager();

                if (trust.isTrusted(victim.getUuid(), attacker.getUuid()) ||
                        trust.isTrusted(attacker.getUuid(), victim.getUuid())) {
                    return ActionResult.FAIL; // Cancel attack
                }
            }

            return ActionResult.PASS;
        });
    }
}