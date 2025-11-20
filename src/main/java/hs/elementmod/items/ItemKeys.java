package hs.elementmod.items;

import hs.elementmod.ElementMod;
import net.minecraft.util.Identifier;

/**
 * Centralized item identification keys for Fabric
 * Converted from Paper's NamespacedKey to Fabric's Identifier
 */
public final class ItemKeys {
    private ItemKeys() {}

    public static final String KEY_UPGRADER_LEVEL = "upgrader_level";
    public static final String KEY_ELEMENT_ITEM = "element_item";
    public static final String KEY_ELEMENT_TYPE = "element_type";
    public static final String KEY_REROLLER = "element_reroller";
    public static final String KEY_ADVANCED_REROLLER = "advanced_reroller";
    public static final String KEY_LIFE_CORE = "life_core";
    public static final String KEY_DEATH_CORE = "death_core";

    public static Identifier namespaced(String key) {
        return Identifier.of(ElementMod.MOD_ID, key);
    }

    public static Identifier upgraderLevel() {
        return namespaced(KEY_UPGRADER_LEVEL);
    }

    public static Identifier elementItem() {
        return namespaced(KEY_ELEMENT_ITEM);
    }

    public static Identifier elementType() {
        return namespaced(KEY_ELEMENT_TYPE);
    }

    public static Identifier reroller() {
        return namespaced(KEY_REROLLER);
    }

    public static Identifier advancedReroller() {
        return namespaced(KEY_ADVANCED_REROLLER);
    }

    public static Identifier lifeCore() {
        return namespaced(KEY_LIFE_CORE);
    }

    public static Identifier deathCore() {
        return namespaced(KEY_DEATH_CORE);
    }
}