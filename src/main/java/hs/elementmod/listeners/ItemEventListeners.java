package hs.elementmod.listeners;

import hs.elementmod.ElementMod;
import hs.elementmod.items.CoreConsumptionHandler;
import hs.elementmod.util.ItemUtil;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

public class ItemEventListeners {

    /** Call this during server initialization */
    public static void register() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            // Only run server-side
            if (world.isClient()) {
                return ActionResult.PASS;
            }

            // Ensure we have a ServerPlayerEntity
            if (!(player instanceof ServerPlayerEntity serverPlayer)) {
                return ActionResult.PASS;
            }

            ItemStack stack = player.getStackInHand(hand);

            // Handle core consumption items first
            if (handleCoreConsumption(serverPlayer, stack, hand)) {
                return ActionResult.SUCCESS;
            }

            // Handle element-specific items
            if (handleElementItemUse(serverPlayer, stack, hand)) {
                return ActionResult.SUCCESS;
            }

            // Nothing handled
            return ActionResult.PASS;
        });
    }

    /** Handle core consumption items */
    private static boolean handleCoreConsumption(ServerPlayerEntity player, ItemStack stack, Hand hand) {
        if (stack.isEmpty() || !ItemUtil.isElementItem(stack)) {
            return false;
        }

        return CoreConsumptionHandler.handleCoreConsume(player, stack, hand);
    }

    /** Handle element-related items */
    private static boolean handleElementItemUse(ServerPlayerEntity player, ItemStack stack, Hand hand) {
        if (stack.isEmpty() || !ItemUtil.isElementItem(stack)) {
            return false;
        }

        ElementMod mod = ElementMod.getInstance();
        if (mod.getItemManager() == null) {
            return false;
        }

        return mod.getItemManager().handleItemUse(player, stack);
    }
}
