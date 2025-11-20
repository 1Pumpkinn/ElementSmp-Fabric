package hs.elementmod.elements;

import hs.elementmod.ElementMod;
import hs.elementmod.elements.abilities.Ability;
import hs.elementmod.elements.abilities.AbilityManager;
import hs.elementmod.elements.impl.air.AirElement;
import hs.elementmod.elements.impl.water.WaterElement;
import hs.elementmod.elements.impl.fire.FireElement;
import hs.elementmod.elements.impl.earth.EarthElement;
import hs.elementmod.elements.impl.life.LifeElement;
// Import others as implemented

import java.util.EnumMap;
import java.util.Map;

/**
 * Registry for all elements in the mod - Fabric version
 * Now auto-registers all implemented elements
 */
public class ElementRegistry {
    private final ElementMod mod;
    private final AbilityManager abilityManager;
    private final Map<ElementType, Element> elements = new EnumMap<>(ElementType.class);

    public ElementRegistry(AbilityManager abilityManager, ElementMod mod) {
        this.abilityManager = abilityManager;
        this.mod = mod;
        initializeElements();
    }

    /**
     * Initialize all element registrations
     */
    private void initializeElements() {
        // Register all implemented elements
        registerElement(ElementType.AIR, new AirElement(mod));
        registerElement(ElementType.WATER, new WaterElement(mod));
        registerElement(ElementType.FIRE, new FireElement(mod));
        registerElement(ElementType.EARTH, new EarthElement(mod));
        registerElement(ElementType.LIFE, new LifeElement(mod));

        // Additional implemented elements
        // Death element may be added later
        try {
            // Metal element implemented below - load if available
            registerElement(ElementType.METAL, new hs.elementmod.elements.impl.metal.MetalElement(mod));
        } catch (NoClassDefFoundError | Exception e) {
            ElementMod.LOGGER.debug("Metal element not present or failed to load: {}", e.toString());
        }

        ElementMod.LOGGER.info("Element registry initialized with {} elements", elements.size());
    }

    /**
     * Register an element
     *
     * @param type The element type
     * @param element The element instance
     */
    public void registerElement(ElementType type, Element element) {
        elements.put(type, element);
        ElementMod.LOGGER.info("Registered element: {}", type.name());
    }

    /**
     * Register all abilities for all elements
     */
    public void registerAllAbilities() {
        // Abilities are now registered via the element classes themselves
        // This method can be used for any additional setup if needed
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
     * Get an element by type
     *
     * @param type The element type
     * @return The element, or null if not registered
     */
    public Element getElement(ElementType type) {
        return elements.get(type);
    }

    /**
     * Check if an element type is registered
     *
     * @param type The element type
     * @return true if the element type is registered, false otherwise
     */
    public boolean isRegistered(ElementType type) {
        return elements.containsKey(type);
    }

    /**
     * Get the ability manager
     * @return The ability manager
     */
    public AbilityManager getAbilityManager() {
        return abilityManager;
    }
}