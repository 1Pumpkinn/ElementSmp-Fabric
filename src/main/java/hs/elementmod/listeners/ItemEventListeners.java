package hs.elementmod.listeners;

import net.fabricmc.fabric.api.event.player.UseItemCallback;

public class ItemEventListeners {
    public static void register() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClient) {
                return TypedActionResult.pass(player.getStackInHand(hand));
            }

            // Handle element item usage here

            return TypedActionResult.pass(player.getStackInHand(hand));
        });
    }
}