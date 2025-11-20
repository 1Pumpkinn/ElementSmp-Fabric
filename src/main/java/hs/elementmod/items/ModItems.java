package hs.elementmod.items;

import hs.elementmod.ElementMod;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {

    // Item instances
    public static Item UPGRADER_1;
    public static Item UPGRADER_2;
    public static Item REROLLER;
    public static Item ADVANCED_REROLLER;
    public static Item LIFE_CORE;
    public static Item DEATH_CORE;

    // Call this during mod initialization
    public static void registerModItems() {
        UPGRADER_1 = registerItem("upgrader_1", new Item.Settings().maxCount(16));
        UPGRADER_2 = registerItem("upgrader_2", new Item.Settings().maxCount(16));
        REROLLER = registerItem("reroller", new Item.Settings().maxCount(1));
        ADVANCED_REROLLER = registerItem("advanced_reroller", new Item.Settings().maxCount(1));
        LIFE_CORE = registerItem("life_core", new Item.Settings().maxCount(64));
        DEATH_CORE = registerItem("death_core", new Item.Settings().maxCount(64));
    }

    private static Item registerItem(String name, Item.Settings settings) {
        Identifier id = Identifier.of(ElementMod.MOD_ID, name);
        return Registry.register(Registries.ITEM, id, new Item(settings));
    }
}