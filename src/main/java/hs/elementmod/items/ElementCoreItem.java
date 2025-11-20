package hs.elementmod.items;

import hs.elementmod.elements.ElementType;
import hs.elementmod.util.ItemUtil;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Factory for creating Element Core items in Fabric
 * Converted from Paper's ItemStack/ItemMeta to Fabric's ItemStack/Components
 */
public final class ElementCoreItem {
    private ElementCoreItem() {}

    private record ElementCoreProperties(
            Item material,
            Formatting color,
            String displayName,
            List<String> lore
    ) {
        ElementCoreProperties(Item material, Formatting color, String displayName) {
            this(material, color, displayName, null);
        }
    }

    private static ElementCoreProperties properties(ElementType type) {
        return switch (type) {
            case LIFE -> new ElementCoreProperties(
                    Items.REDSTONE_BLOCK,
                    Formatting.RED,
                    "Life Element"
            );
            case DEATH -> new ElementCoreProperties(
                    Items.WITHER_SKELETON_SKULL,
                    Formatting.DARK_GRAY,
                    "Death Element"
            );
            // Add more as elements are implemented
            default -> null;
        };
    }

    /**
     * Create a core item for the given element type
     */
    public static ItemStack createCore(ElementType type) {
        ElementCoreProperties props = properties(type);
        if (props == null) return null;

        ItemStack stack = new ItemStack(props.material());

        // Set custom name
        Text name = Text.literal(props.displayName()).formatted(props.color());
        stack.set(DataComponentTypes.CUSTOM_NAME, name);

        // Set lore if present
        if (props.lore() != null && !props.lore().isEmpty()) {
            List<Text> loreLines = new ArrayList<>();
            for (String line : props.lore()) {
                loreLines.add(Text.literal(line).formatted(Formatting.GRAY));
            }
            stack.set(DataComponentTypes.LORE, new net.minecraft.component.type.LoreComponent(loreLines));
        }

        // Set NBT data for identification
        NbtCompound compound = new NbtCompound();
        compound.putString(ItemKeys.KEY_ELEMENT_TYPE, type.name());
        compound.putByte(ItemKeys.KEY_ELEMENT_ITEM, (byte) 1);

        // Add specific core identifier based on type
        if (type == ElementType.LIFE) {
            compound.putByte(ItemKeys.KEY_LIFE_CORE, (byte) 1);
        } else if (type == ElementType.DEATH) {
            compound.putByte(ItemKeys.KEY_DEATH_CORE, (byte) 1);
        }

        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(compound));

        return stack;
    }

    /**
     * Get the display name for an element type
     */
    public static String getDisplayName(ElementType type) {
        ElementCoreProperties props = properties(type);
        if (props != null) {
            return props.color() + props.displayName();
        }
        return type.name();
    }

    /**
     * Get the lore for an element type
     */
    public static List<String> getLore(ElementType type) {
        ElementCoreProperties props = properties(type);
        if (props != null && props.lore() != null) {
            return props.lore();
        }
        return Collections.emptyList();
    }

    /**
     * Check if an ItemStack is a specific element core
     */
    public static boolean isCore(ItemStack stack, ElementType type) {
        if (stack == null || stack.isEmpty()) return false;

        NbtComponent nbt = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (nbt == null) return false;

        NbtCompound compound = nbt.copyNbt();

        // Get element type as Optional
        Optional<String> optionalTypeStr = compound.getString(ItemKeys.KEY_ELEMENT_TYPE);
        if (optionalTypeStr.isEmpty()) return false;

        try {
            ElementType stackType = ElementType.valueOf(optionalTypeStr.get());
            return stackType == type;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

}