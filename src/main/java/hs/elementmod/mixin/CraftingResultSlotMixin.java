package hs.elementmod.mixin;

import hs.elementmod.listeners.CraftingEventListeners;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.CraftingResultSlot;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to intercept crafting events
 * Allows us to handle custom item crafting logic
 */
@Mixin(CraftingResultSlot.class)
public class CraftingResultSlotMixin {

    /**
     * Inject into onTakeItem to handle custom crafting logic
     */
    @Inject(
            method = "onTakeItem",
            at = @At("HEAD"),
            cancellable = false
    )
    private void onCraftItem(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        // Only run on server side
        if (player.getEntityWorld().isClient()) return;
        if (player instanceof ServerPlayerEntity serverPlayer) {
            CraftingResultSlot slot = (CraftingResultSlot) (Object) this;

            // Call our custom crafting handler
            CraftingEventListeners.handleCrafting(
                    serverPlayer,
                    stack,
                    serverPlayer.currentScreenHandler
            );
        }
    }
}