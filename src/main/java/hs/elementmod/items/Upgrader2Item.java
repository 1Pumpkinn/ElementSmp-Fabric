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
 * Factory class for Upgrader II items in Fabric
 */
public final class Upgrader2Item {
    public Upgrader2Item() {}

    public static ItemStack create() {
        ItemStack stack = new ItemStack(Items.ECHO_SHARD);

        // Set custom name
        Text name = Text.literal("Upgrader II").formatted(Formatting.AQUA);
        stack.set(DataComponentTypes.CUSTOM_NAME, name);

        // Set lore
        List<Text> loreLines = new ArrayList<>();
        loreLines.add(Text.literal("Use by crafting to unlock").formatted(Formatting.GRAY));
        loreLines.add(Text.literal("Ability 2 + Upside 2 for your element").formatted(Formatting.GRAY));
        stack.set(DataComponentTypes.LORE, new LoreComponent(loreLines));

        // Set NBT data
        NbtCompound compound = new NbtCompound();
        compound.putInt(ItemKeys.KEY_UPGRADER_LEVEL, 2);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(compound));

        return stack;
    }

    /**
     * Check if an ItemStack is an Upgrader II
     */
    public static boolean isUpgrader2(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;

        NbtComponent nbt = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (nbt == null) return false;

        NbtCompound compound = nbt.copyNbt();
        Optional<Integer> optionalLevel = compound.getInt(ItemKeys.KEY_UPGRADER_LEVEL);
        return optionalLevel.isPresent() && optionalLevel.get() == 2;
    }
}