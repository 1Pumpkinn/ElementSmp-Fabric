package hs.elementmod.elements;

import hs.elementmod.ElementMod;
import hs.elementmod.elements.abilities.Ability;
import hs.elementmod.elements.abilities.AbilityManager;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Registry for all elements in the mod
 */
public class ElementRegistry {
    private final AbilityManager abilityManager;
    private final Map<ElementType, Supplier<Element>> elementSuppliers = new HashMap<>();

    public ElementRegistry(AbilityManager abilityManager) {
        this.abilityManager = abilityManager;
        initializeElements();
    }

    /**
     * Initialize all element registrations
     */
    private void initializeElements() {
        // Elements will be registered here as they are implemented
        ElementMod.LOGGER.info("Element registry initialized");
    }

    /**
     * Register an element
     *
     * @param type The element type
     * @param supplier The supplier that creates the element
     */
    public void registerElement(ElementType type, Supplier<Element> supplier) {
        elementSuppliers.put(type, supplier);
        ElementMod.LOGGER.info("Registered element: " + type.name());
    }

    /**
     * Register all abilities for all elements
     */
    public void registerAllAbilities() {
        // Abilities will be registered here as they are implemented
        ElementMod.LOGGER.info("All abilities registered");
    }

    /**
     * Register an ability for an element
     *
     * @param type The element type
     * @param abilityNumber The ability number (1 or 2)
     * @param ability The ability
     */
    public void registerAbility(ElementType type, int abilityNumber, Ability ability) {
        abilityManager.registerAbility(type, abilityNumber, ability);
    }

    /**
     * Create an element
     *
     * @param type The element type
     * @return The created element, or null if the type is not registered
     */
    public Element createElement(ElementType type) {
        Supplier<Element> supplier = elementSuppliers.get(type);
        if (supplier == null) return null;
        return supplier.get();
    }

    /**
     * Check if an element type is registered
     *
     * @param type The element type
     * @return true if the element type is registered, false otherwise
     */
    public boolean isRegistered(ElementType type) {
        return elementSuppliers.containsKey(type);
    }

    /**
     * Get the ability manager
     * @return The ability manager
     */
    public AbilityManager getAbilityManager() {
        return abilityManager;
    }
}
