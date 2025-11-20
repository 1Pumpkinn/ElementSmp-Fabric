package hs.elementmod.items.custom;

import hs.elementmod.items.ItemKeys;
import hs.elementmod.items.ModItems;
import hs.elementmod.util.ItemUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

/**
 * Upgrader I - Upgrades element to level 1
 */
public final class Upgrader1Item {
    private Upgrader1Item() {}

    /**
     * Create a new Upgrader I item
     */
    public static ItemStack create() {
        ItemStack stack = new ItemStack(ModItems.UPGRADER_I);

        // Set NBT data
        ItemUtil.setNbtInt(stack, ItemKeys.KEY_UPGRADER_LEVEL, 1);

        // Set display name
        stack.set(net.minecraft.component.DataComponentTypes.ITEM_NAME,
                Text.literal("✦ Upgrader I ✦")
                        .formatted(Formatting.GOLD, Formatting.BOLD));

        // Set lore
        stack.set(net.minecraft.component.DataComponentTypes.LORE,
                new net.minecraft.component.type.LoreComponent(List.of(
                        Text.literal("Right-click to upgrade your element")
                                .formatted(Formatting.GRAY),
                        Text.literal("to Level I")
                                .formatted(Formatting.AQUA),
                        Text.empty(),
                        Text.literal("Unlocks basic abilities")
                                .formatted(Formatting.YELLOW)
                )));

        // Add enchantment glint
        stack.set(net.minecraft.component.DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);

        return stack;
    }

    /**
     * Check if an ItemStack is Upgrader I
     */
    public static boolean isUpgrader1(ItemStack stack) {
                if (stack == null || stack.isEmpty()) return false;
                // If the item itself is the registered upgrader item, consider it Upgrader I
                if (stack.getItem() == hs.elementmod.items.ModItems.UPGRADER_I) return true;
                return ItemUtil.getUpgraderLevel(stack) == 1;
    }
}