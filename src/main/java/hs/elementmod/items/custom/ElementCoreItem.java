package hs.elementmod.items.custom;

import hs.elementmod.elements.ElementType;
import hs.elementmod.items.ItemKeys;
import hs.elementmod.items.ModItems;
import hs.elementmod.util.ItemUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

/**
 * Element Cores - Special consumable items for Life and Death elements
 */
public final class ElementCoreItem {
    private ElementCoreItem() {}

    /**
     * Create a Life or Death core item
     */
    public static ItemStack createCore(ElementType type) {
        if (type != ElementType.LIFE && type != ElementType.DEATH) {
            return null;
        }

        ItemStack stack = type == ElementType.LIFE
                ? new ItemStack(ModItems.LIFE_CORE)
                : new ItemStack(ModItems.DEATH_CORE);

        // Set NBT data
        ItemUtil.setNbtByte(stack, ItemKeys.KEY_ELEMENT_ITEM, (byte) 1);
        ItemUtil.setNbtString(stack, ItemKeys.KEY_ELEMENT_TYPE, type.name());

        if (type == ElementType.LIFE) {
            // Set display name
            stack.set(net.minecraft.component.DataComponentTypes.ITEM_NAME,
                    Text.literal("💚 Life Core 💚")
                            .formatted(Formatting.GREEN, Formatting.BOLD));

            // Set lore
            stack.set(net.minecraft.component.DataComponentTypes.LORE,
                    new net.minecraft.component.type.LoreComponent(List.of(
                            Text.literal("A core of pure life energy")
                                    .formatted(Formatting.GRAY),
                            Text.empty(),
                            Text.literal("Right-click to consume and gain")
                                    .formatted(Formatting.GREEN),
                            Text.literal("the Life element")
                                    .formatted(Formatting.GREEN),
                            Text.empty(),
                            Text.literal("⚠ Can only be crafted once per server")
                                    .formatted(Formatting.RED),
                            Text.literal("⚠ Permanently unlocks Life element")
                                    .formatted(Formatting.YELLOW)
                    )));
        } else {
            // Set display name
            stack.set(net.minecraft.component.DataComponentTypes.ITEM_NAME,
                    Text.literal("💀 Death Core 💀")
                            .formatted(Formatting.DARK_GRAY, Formatting.BOLD));

            // Set lore
            stack.set(net.minecraft.component.DataComponentTypes.LORE,
                    new net.minecraft.component.type.LoreComponent(List.of(
                            Text.literal("A core of pure death energy")
                                    .formatted(Formatting.GRAY),
                            Text.empty(),
                            Text.literal("Right-click to consume and gain")
                                    .formatted(Formatting.DARK_GRAY),
                            Text.literal("the Death element")
                                    .formatted(Formatting.DARK_GRAY),
                            Text.empty(),
                            Text.literal("⚠ Can only be crafted once per server")
                                    .formatted(Formatting.RED),
                            Text.literal("⚠ Permanently unlocks Death element")
                                    .formatted(Formatting.YELLOW)
                    )));
        }

        // Add enchantment glint
        stack.set(net.minecraft.component.DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);

        return stack;
    }

    /**
     * Check if an ItemStack is a Life or Death core
     */
    public static boolean isCore(ItemStack stack, ElementType type) {
        if (stack == null || stack.isEmpty()) return false;
        if (!ItemUtil.isElementItem(stack)) return false;

        ElementType stackType = ItemUtil.getElementType(stack);
        return stackType == type;
    }

    /**
     * Get the display name for an element type
     */
    public static String getDisplayName(ElementType type) {
        return switch (type) {
            case LIFE -> "Life Core";
            case DEATH -> "Death Core";
            default -> type.name() + " Core";
        };
    }
}