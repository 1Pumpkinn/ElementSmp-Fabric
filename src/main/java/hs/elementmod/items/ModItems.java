package hs.elementmod.items;

import hs.elementmod.ElementMod;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public class ModItems {

    // Simple item with max stack size 1
    public static final Item UPGRADER_I = registerItem(
            "upgrader_i",
            Item::new,
            new Item.Settings().maxCount(1)
    );

    public static <I extends Item> I registerItem(String name, Function<Item.Settings, I> factory, Item.Settings settings) {
        I item = factory.apply(settings); // pass settings directly

        if (item instanceof BlockItem blockItem) {
            blockItem.appendBlocks(Item.BLOCK_ITEMS, blockItem);
        }

        return Registry.register(Registries.ITEM, Identifier.of(ElementMod.MOD_ID, name), item);
    }


    public static void registerModItems() {
        ElementMod.LOGGER.info("Registering Mod Items for " + ElementMod.MOD_ID);
        // Reference the item to avoid "never used" warnings
        UPGRADER_I.getTranslationKey();
    }
}
