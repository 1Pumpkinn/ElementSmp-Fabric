package hs.elementmod.listeners;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.util.ActionResult;

public class ItemEventListeners {
    public static void register() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClient()) {
                return ActionResult.PASS;
            }

            // Handle element item usage here

            return ActionResult.PASS;
        });
    }
}
