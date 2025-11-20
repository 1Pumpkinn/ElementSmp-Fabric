package hs.elementmod.items.custom;

import hs.elementmod.items.ItemKeys;
import hs.elementmod.items.ModItems;
import hs.elementmod.util.ItemUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

/**
 * Upgrader II - Upgrades element to level 2
 */
public final class Upgrader2Item {
    private Upgrader2Item() {}

    /**
     * Create a new Upgrader II item
     */
    public static ItemStack create() {
        ItemStack stack = new ItemStack(ModItems.UPGRADER_II);

        // Set NBT data
        ItemUtil.setNbtInt(stack, ItemKeys.KEY_UPGRADER_LEVEL, 2);

        // Set display name
        stack.set(net.minecraft.component.DataComponentTypes.ITEM_NAME,
                Text.literal("✦ Upgrader II ✦")
                        .formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));

        // Set lore
        stack.set(net.minecraft.component.DataComponentTypes.LORE,
                new net.minecraft.component.type.LoreComponent(List.of(
                        Text.literal("Right-click to upgrade your element")
                                .formatted(Formatting.GRAY),
                        Text.literal("to Level II")
                                .formatted(Formatting.AQUA),
                        Text.empty(),
                        Text.literal("Unlocks advanced abilities")
                                .formatted(Formatting.YELLOW),
                        Text.literal("Requires Level I first")
                                .formatted(Formatting.RED)
                )));

        // Add enchantment glint
        stack.set(net.minecraft.component.DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);

        return stack;
    }

    /**
     * Check if an ItemStack is Upgrader II
     */
    public static boolean isUpgrader2(ItemStack stack) {
                if (stack == null || stack.isEmpty()) return false;
                // If the item itself is the registered upgrader item, consider it Upgrader II
                if (stack.getItem() == hs.elementmod.items.ModItems.UPGRADER_II) return true;
                return ItemUtil.getUpgraderLevel(stack) == 2;
    }
}