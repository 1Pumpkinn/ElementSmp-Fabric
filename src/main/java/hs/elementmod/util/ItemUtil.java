package hs.elementmod.util;

import hs.elementmod.elements.ElementType;
import hs.elementmod.items.ItemKeys;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

import java.util.Optional;

/**
 * Utility class for item operations in Fabric
 * Converted from Paper's PersistentDataContainer to Fabric's NBT components
 */
public final class ItemUtil {
    private ItemUtil() {}

    /** Check if an ItemStack is an element item */
    public static boolean isElementItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;

        NbtComponent nbt = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (nbt == null) return false;

        Optional<Byte> value = nbt.copyNbt().getByte(ItemKeys.KEY_ELEMENT_ITEM);
        return value.orElse((byte)0) == 1;
    }

    /** Get the ElementType from an ItemStack */
    public static ElementType getElementType(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;

        NbtComponent nbt = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (nbt == null) return null;

        Optional<String> typeOpt = nbt.copyNbt().getString(ItemKeys.KEY_ELEMENT_TYPE);
        if (typeOpt.isEmpty()) return null;

        try {
            return ElementType.valueOf(typeOpt.get());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /** Check if an ItemStack is a reroller item */
    public static boolean isReroller(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;

        NbtComponent nbt = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (nbt == null) return false;

        Optional<Byte> value = nbt.copyNbt().getByte(ItemKeys.KEY_REROLLER);
        return value.orElse((byte)0) == 1;
    }

    /** Check if an ItemStack is an advanced reroller item */
    public static boolean isAdvancedReroller(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;

        NbtComponent nbt = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (nbt == null) return false;

        Optional<Byte> value = nbt.copyNbt().getByte(ItemKeys.KEY_ADVANCED_REROLLER);
        return value.orElse((byte)0) == 1;
    }

    /** Get upgrader level from an ItemStack */
    public static int getUpgraderLevel(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return 0;

        NbtComponent nbt = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (nbt == null) return 0;

        Optional<Integer> value = nbt.copyNbt().getInt(ItemKeys.KEY_UPGRADER_LEVEL);
        return value.orElse(0);
    }

    /** Set NBT string on an ItemStack */
    public static void setNbtString(ItemStack stack, String key, String value) {
        NbtCompound compound = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
        compound.putString(key, value);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(compound));
    }

    /** Set NBT byte on an ItemStack */
    public static void setNbtByte(ItemStack stack, String key, byte value) {
        NbtCompound compound = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
        compound.putByte(key, value);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(compound));
    }

    /** Set NBT int on an ItemStack */
    public static void setNbtInt(ItemStack stack, String key, int value) {
        NbtCompound compound = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
        compound.putInt(key, value);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(compound));
    }
}
