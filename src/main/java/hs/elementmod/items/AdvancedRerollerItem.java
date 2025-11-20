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
 * Factory class for Advanced Reroller items in Fabric
 */
public final class AdvancedRerollerItem {
    private AdvancedRerollerItem() {}

    public static ItemStack create() {
        ItemStack stack = new ItemStack(Items.RECOVERY_COMPASS);

        // Set custom name
        Text name = Text.literal("Advanced Reroller").formatted(Formatting.DARK_PURPLE);
        stack.set(DataComponentTypes.CUSTOM_NAME, name);

        // Set lore
        List<Text> loreLines = new ArrayList<>();
        loreLines.add(Text.literal("Unlocks advanced elements").formatted(Formatting.GRAY));
        loreLines.add(Text.literal("Right-click to reroll").formatted(Formatting.YELLOW));
        stack.set(DataComponentTypes.LORE, new LoreComponent(loreLines));

        // Set NBT data
        NbtCompound compound = new NbtCompound();
        compound.putByte(ItemKeys.KEY_ADVANCED_REROLLER, (byte) 1);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(compound));

        return stack;
    }

    /**
     * Check if an ItemStack is an Advanced Reroller
     */
    public static boolean isAdvancedReroller(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;

        NbtComponent nbt = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (nbt == null) return false;

        NbtCompound compound = nbt.copyNbt();

        Optional<Byte> optionalByte = compound.getByte(ItemKeys.KEY_ADVANCED_REROLLER);
        return optionalByte.isPresent() && optionalByte.get() == (byte) 1;
    }
}