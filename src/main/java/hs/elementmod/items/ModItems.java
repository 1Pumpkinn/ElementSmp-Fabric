package hs.elementmod.items;

import hs.elementmod.ElementMod;
import hs.elementmod.elements.ElementType;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * Registry for all custom items in the mod
 */
public class ModItems {

    // Item instances
    public static final Item UPGRADER_1 = register("upgrader_1", new Upgrader1Item(new Item.Settings()));
    public static final Item UPGRADER_2 = register("upgrader_2", new Upgrader2Item(new Item.Settings()));
    public static final Item REROLLER = register("element_reroller", new RerollerItem(new Item.Settings()));
    public static final Item ADVANCED_REROLLER = register("advanced_reroller", new AdvancedRerollerItem(new Item.Settings()));
    public static final Item LIFE_CORE = register("life_core", new ElementCoreItem(new Item.Settings(), ElementType.LIFE));
    public static final Item DEATH_CORE = register("death_core", new ElementCoreItem(new Item.Settings(), ElementType.DEATH));

    /**
     * Register an item in Fabric
     */
    private static Item register(String name, Item item) {
        return Registry.register(Registries.ITEM, new Identifier(ElementMod.MOD_ID, name), item);
    }

    /**
     * Initialize all items and add them to creative inventory
     */
    public static void initialize() {
        ElementMod.LOGGER.info("Registering mod items...");

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(content -> {
            content.add(UPGRADER_1);
            content.add(UPGRADER_2);
            content.add(REROLLER);
            content.add(ADVANCED_REROLLER);
            content.add(LIFE_CORE);
            content.add(DEATH_CORE);
        });

        ElementMod.LOGGER.info("Mod items registered successfully");
    }
}