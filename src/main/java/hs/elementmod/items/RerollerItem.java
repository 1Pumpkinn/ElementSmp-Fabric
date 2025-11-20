package hs.elementmod.items;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Factory classes for Reroller items in Fabric
 */
public final class RerollerItem {
    private RerollerItem() {}

    public static ItemStack create() {
        ItemStack stack = new ItemStack(Items.HEART_OF_THE_SEA);

        // Set custom name
        Text name = Text.literal("Element Reroller").formatted(Formatting.LIGHT_PURPLE);
        stack.set(DataComponentTypes.CUSTOM_NAME, name);

        // Set lore
        List<Text> loreLines = new ArrayList<>();
        loreLines.add(Text.literal("Allows you to change your element").formatted(Formatting.GRAY));
        loreLines.add(Text.literal("Right-click to randomly reroll your element").formatted(Formatting.YELLOW));
        stack.set(DataComponentTypes.LORE, new LoreComponent(loreLines));

        // Set NBT data
        NbtCompound compound = new NbtCompound();
        compound.putByte(ItemKeys.KEY_REROLLER, (byte) 1);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(compound));

        return stack;
    }

    /**
     * Check if an ItemStack is a Reroller
     */
    public static boolean isReroller(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;

        NbtComponent nbt = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (nbt == null) return false;

        NbtCompound compound = nbt.copyNbt();
        Optional<Byte> optionalByte = compound.getByte(ItemKeys.KEY_REROLLER);
        return optionalByte.isPresent() && optionalByte.get() == (byte) 1;
    }
}