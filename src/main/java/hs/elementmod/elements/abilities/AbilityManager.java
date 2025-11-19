package hs.elementmod.elements.abilities;

import hs.elementmod.ElementMod;
import hs.elementmod.elements.ElementContext;
import hs.elementmod.elements.ElementType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages all abilities in the mod
 */
public class AbilityManager {
    private final ElementMod mod;
    private final Map<String, Ability> abilities = new HashMap<>();
    private final Map<ElementType, Map<Integer, Ability>> elementAbilities = new HashMap<>();

    public AbilityManager(ElementMod mod) {
        this.mod = mod;

        // Initialize maps for each element type
        for (ElementType type : ElementType.values()) {
            elementAbilities.put(type, new HashMap<>());
        }
    }

    /**
     * Register an ability
     *
     * @param elementType The element type this ability belongs to
     * @param abilityNumber The ability number (1 or 2)
     * @param ability The ability to register
     */
    public void registerAbility(ElementType elementType, int abilityNumber, Ability ability) {
        abilities.put(ability.getAbilityId(), ability);
        elementAbilities.get(elementType).put(abilityNumber, ability);
    }

    /**
     * Execute an ability for a player (NO COOLDOWN CHECK)
     *
     * @param context The element context
     * @param abilityNumber The ability number (1 or 2)
     * @return true if the ability was executed successfully, false otherwise
     */
    public boolean executeAbility(ElementContext context, int abilityNumber) {
        ServerPlayerEntity player = context.getPlayer();
        ElementType elementType = context.getElementType();

        // Get the ability
        Ability ability = elementAbilities.get(elementType).get(abilityNumber);
        if (ability == null) {
            player.sendMessage(Text.literal("This ability doesn't exist!")
                    .formatted(Formatting.RED), false);
            return false;
        }

        // Check upgrade level
        if (context.getUpgradeLevel() < ability.getRequiredUpgradeLevel()) {
            player.sendMessage(Text.literal("You need Upgrade " +
                            (ability.getRequiredUpgradeLevel() == 1 ? "I" : "II") + " to use this ability.")
                    .formatted(Formatting.RED), false);
            return false;
        }

        // Check if ability is already active
        if (ability.isActiveFor(player)) {
            player.sendMessage(Text.literal("This ability is already active!")
                    .formatted(Formatting.YELLOW), false);
            return false;
        }

        // Check mana (NO COOLDOWN CHECK)
        int cost = ability.getManaCost();
        if (context.getManaManager().get(player.getUuid()).getMana() < cost) {
            player.sendMessage(Text.literal("Not enough mana (" + cost + ")")
                    .formatted(Formatting.RED), false);
            return false;
        }

        // Execute ability
        if (ability.execute(context)) {
            context.getManaManager().spend(player, cost);
            return true;
        }

        return false;
    }

    /**
     * Get an ability by its ID
     *
     * @param abilityId The ability ID
     * @return The ability, or null if not found
     */
    public Ability getAbility(String abilityId) {
        return abilities.get(abilityId);
    }
}