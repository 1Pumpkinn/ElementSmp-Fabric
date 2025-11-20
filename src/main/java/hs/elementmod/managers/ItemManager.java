package hs.elementmod.managers;

import hs.elementmod.ElementMod;
import hs.elementmod.config.ConfigManager;
import hs.elementmod.elements.ElementType;
import hs.elementmod.items.*;
import hs.elementmod.items.custom.*;
import hs.elementmod.util.ItemUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Manages element items and their usage in Fabric
 * Converted from Paper API
 */
public class ItemManager {
    private final ManaManager manaManager;
    private final ConfigManager configManager;

    public ItemManager(ManaManager manaManager, ConfigManager configManager) {
        this.manaManager = manaManager;
        this.configManager = configManager;
    }

    /**
     * Handle using an element item (right-click)
     */
    public boolean handleItemUse(ServerPlayerEntity player, ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;

        // Check if it's an element item
        if (!ItemUtil.isElementItem(stack)) return false;

        ElementType itemType = ItemUtil.getElementType(stack);
        if (itemType == null) return false;

        // Get mana cost for item use
        int cost = configManager.getItemUseCost(itemType);

        // Check if player has enough mana
        if (!manaManager.hasMana(player, cost)) {
            player.sendMessage(
                    Text.literal("Not enough mana (" + cost + ")")
                            .formatted(Formatting.RED),
                    false
            );
            return false;
        }

        // Spend mana
        manaManager.spend(player, cost);

        // TODO: Implement element-specific item effects
        player.sendMessage(
                Text.literal("Used " + itemType.name() + " item!")
                        .formatted(Formatting.GREEN),
                false
        );

        return true;
    }

    /**
     * Give a player a specific item
     */
    public void giveItem(ServerPlayerEntity player, ItemStack stack) {
        if (stack == null || stack.isEmpty()) return;

        // Add to inventory
        boolean added = player.getInventory().insertStack(stack);

        if (!added) {
            // Drop at player's feet if inventory is full
            player.dropItem(stack, false);
            player.sendMessage(
                    Text.literal("Your inventory is full! Item dropped at your feet.")
                            .formatted(Formatting.YELLOW),
                    false
            );
        }
    }

    /**
     * Give a player an upgrader item
     */
    public void giveUpgrader(ServerPlayerEntity player, int level) {
        ItemStack stack = level == 1 ? Upgrader1Item.create() : Upgrader2Item.create();
        giveItem(player, stack);
    }

    /**
     * Give a player a reroller item
     */
    public void giveReroller(ServerPlayerEntity player, boolean advanced) {
        ItemStack stack = advanced ? AdvancedRerollerItem.create() : RerollerItem.create();
        giveItem(player, stack);
    }

    /**
     * Give a player an element core
     */
    public void giveElementCore(ServerPlayerEntity player, ElementType type) {
        ItemStack stack = ElementCoreItem.createCore(type);
        if (stack != null) {
            giveItem(player, stack);
        }
    }

    /**
     * Check if a player is holding an element item
     */
    public boolean isHoldingElementItem(ServerPlayerEntity player) {
        ItemStack mainHand = player.getMainHandStack();
        ItemStack offHand = player.getOffHandStack();

        return ItemUtil.isElementItem(mainHand) || ItemUtil.isElementItem(offHand);
    }

    /**
     * Get the element item the player is holding (if any)
     */
    public ItemStack getHeldElementItem(ServerPlayerEntity player) {
        ItemStack mainHand = player.getMainHandStack();
        if (ItemUtil.isElementItem(mainHand)) {
            return mainHand;
        }

        ItemStack offHand = player.getOffHandStack();
        if (ItemUtil.isElementItem(offHand)) {
            return offHand;
        }

        return null;
    }
}