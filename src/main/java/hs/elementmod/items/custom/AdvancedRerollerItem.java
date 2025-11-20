package hs.elementmod.items.custom;

import hs.elementmod.items.ItemKeys;
import hs.elementmod.items.ModItems;
import hs.elementmod.util.ItemUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

/**
 * Advanced Reroller - Rerolls to any element including Life/Death (if unlocked)
 */
public final class AdvancedRerollerItem {
    private AdvancedRerollerItem() {}

    /**
     * Create a new Advanced Reroller item
     */
    public static ItemStack create() {
        ItemStack stack = new ItemStack(ModItems.ADVANCED_REROLLER);

        // Set NBT data
        ItemUtil.setNbtByte(stack, ItemKeys.KEY_ADVANCED_REROLLER, (byte) 1);

        // Set display name
        stack.set(net.minecraft.component.DataComponentTypes.ITEM_NAME,
                Text.literal("⚡ Advanced Reroller ⚡")
                        .formatted(Formatting.DARK_PURPLE, Formatting.BOLD));

        // Set lore
        stack.set(net.minecraft.component.DataComponentTypes.LORE,
                new net.minecraft.component.type.LoreComponent(List.of(
                        Text.literal("Right-click to reroll your element")
                                .formatted(Formatting.GRAY),
                        Text.literal("to ANY element")
                                .formatted(Formatting.LIGHT_PURPLE),
                        Text.empty(),
                        Text.literal("Includes Life/Death if you've")
                                .formatted(Formatting.YELLOW),
                        Text.literal("already unlocked them")
                                .formatted(Formatting.YELLOW),
                        Text.empty(),
                        Text.literal("⚠ Resets your upgrade level")
                                .formatted(Formatting.RED)
                )));

        // Add enchantment glint
        stack.set(net.minecraft.component.DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);

        return stack;
    }

    /**
     * Check if an ItemStack is an Advanced Reroller
     */
    public static boolean isAdvancedReroller(ItemStack stack) {
        return ItemUtil.isAdvancedReroller(stack);
    }
}