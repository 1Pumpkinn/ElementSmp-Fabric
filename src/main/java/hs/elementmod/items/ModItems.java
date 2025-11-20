package hs.elementmod.items;

import hs.elementmod.ElementMod;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.item.ItemGroups;
import net.minecraft.util.Identifier;

/**
 * Registry for all custom items in the mod
 */
public class ModItems {

    public static Item UPGRADER_1;
    public static Item UPGRADER_2;
    public static Item REROLLER;
    public static Item ADVANCED_REROLLER;
    public static Item LIFE_CORE;
    public static Item DEATH_CORE;

    private static Item register(String name) {

        Identifier id = Identifier.of(ElementMod.MOD_ID, name);
        RegistryKey<Item> key = RegistryKey.of(Registries.ITEM.getKey(), id);

        Item.Settings settings = new Item.Settings()
                .registryKey(key);

        return Registry.register(Registries.ITEM, key, new Item(settings));
    }

    public static void initialize() {

        ElementMod.LOGGER.info("Registering mod items...");

        UPGRADER_1 = register("upgrader_1");
        UPGRADER_2 = register("upgrader_2");
        REROLLER = register("element_reroller");
        ADVANCED_REROLLER = register("advanced_reroller");
        LIFE_CORE = register("life_core");
        DEATH_CORE = register("death_core");

        // Add to creative tab
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
            entries.add(UPGRADER_1);
            entries.add(UPGRADER_2);
            entries.add(REROLLER);
            entries.add(ADVANCED_REROLLER);
            entries.add(LIFE_CORE);
            entries.add(DEATH_CORE);
        });
    }
}
