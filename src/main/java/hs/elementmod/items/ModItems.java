package hs.elementmod.items;

import hs.elementmod.ElementMod;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public class ModItems {

    // Upgraders
    public static final Item UPGRADER_I = registerItem("upgrader_i", Item::new, new Item.Settings().maxCount(1));
    public static final Item UPGRADER_II = registerItem("upgrader_ii", Item::new, new Item.Settings().maxCount(1));

    // Rerollers
    public static final Item ELEMENT_REROLLER = registerItem("element_reroller", Item::new, new Item.Settings().maxCount(1));
    public static final Item ADVANCED_REROLLER = registerItem("advanced_reroller", Item::new, new Item.Settings().maxCount(1));

    // Element Cores
    public static final Item LIFE_CORE = registerItem("life_core", Item::new, new Item.Settings().maxCount(1));
    public static final Item DEATH_CORE = registerItem("death_core", Item::new, new Item.Settings().maxCount(1));

    /**
     * Generic register helper that mirrors the pattern used in other mods -
     * creates a RegistryKey, applies registry-key to the settings and registers the item.
     */
    public static <I extends Item> I registerItem(String name, Function<Item.Settings, I> factory, Item.Settings settings) {
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(ElementMod.MOD_ID, name));
        I item = factory.apply(settings.registryKey(key));

        if (item instanceof BlockItem blockItem) {
            blockItem.appendBlocks(Item.BLOCK_ITEMS, blockItem);
        }

        return Registry.register(Registries.ITEM, key, item);
    }

    public static Item registerItem(String id, Function<Item.Settings, Item> factory) {
        return registerItem(id, factory, new Item.Settings());
    }

    /**
     * Initialize all mod items
     * Called during mod initialization
     */
    public static void registerModItems() {
        // Registering items — keep a single informative log entry
        ElementMod.LOGGER.info("Registered mod items for {}", ElementMod.MOD_ID);
    }
}