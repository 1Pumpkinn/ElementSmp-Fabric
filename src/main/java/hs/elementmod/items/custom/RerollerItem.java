package hs.elementmod.items.custom;

import hs.elementmod.items.ItemKeys;
import hs.elementmod.items.ModItems;
import hs.elementmod.util.ItemUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

/**
 * Element Reroller - Rerolls to a random basic element
 */
public final class RerollerItem {
    private RerollerItem() {}

    /**
     * Create a new Element Reroller item
     */
    public static ItemStack create() {
        ItemStack stack = new ItemStack(ModItems.ELEMENT_REROLLER);

        // Set NBT data
        ItemUtil.setNbtByte(stack, ItemKeys.KEY_REROLLER, (byte) 1);

        // Set display name
        stack.set(net.minecraft.component.DataComponentTypes.ITEM_NAME,
                Text.literal("⚡ Element Reroller ⚡")
                        .formatted(Formatting.GOLD, Formatting.BOLD));

        // Set lore
        stack.set(net.minecraft.component.DataComponentTypes.LORE,
                new net.minecraft.component.type.LoreComponent(List.of(
                        Text.literal("Right-click to reroll your element")
                                .formatted(Formatting.GRAY),
                        Text.literal("to a random basic element")
                                .formatted(Formatting.AQUA),
                        Text.empty(),
                        Text.literal("Basic Elements:")
                                .formatted(Formatting.YELLOW),
                        Text.literal("Air, Water, Fire, Earth, Metal, Frost")
                                .formatted(Formatting.WHITE),
                        Text.empty(),
                        Text.literal("⚠ Resets your upgrade level")
                                .formatted(Formatting.RED)
                )));

        // Add enchantment glint
        stack.set(net.minecraft.component.DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);

        return stack;
    }

    /**
     * Check if an ItemStack is a Reroller
     */
    public static boolean isReroller(ItemStack stack) {
        return ItemUtil.isReroller(stack);
    }
}