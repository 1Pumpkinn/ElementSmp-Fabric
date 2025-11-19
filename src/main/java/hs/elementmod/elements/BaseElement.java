package hs.elementmod.elements;

import hs.elementmod.ElementMod;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Abstract base class for all elements - Fabric version
 */
public abstract class BaseElement implements Element {
    protected final ElementMod mod;
    private final java.util.Set<java.util.UUID> activeAbility1 = new java.util.HashSet<>();
    private final java.util.Set<java.util.UUID> activeAbility2 = new java.util.HashSet<>();

    // Abstract methods that must be implemented by subclasses
    public abstract void clearEffects(ServerPlayerEntity player);
    public abstract String getDisplayName();
    public abstract String getDescription();
    public abstract String getAbility1Name();
    public abstract String getAbility1Description();
    public abstract String getAbility2Name();
    public abstract String getAbility2Description();

    public BaseElement(ElementMod mod) {
        this.mod = mod;
    }

    public ElementMod getMod() {
        return mod;
    }

    @Override
    public boolean ability1(ElementContext context) {
        if (!checkUpgradeLevel(context.getPlayer(), context.getUpgradeLevel(), 1)) return false;

        // Check if ability can be cancelled
        boolean shouldCheckCosts = !canCancelAbility1(context);

        if (!shouldCheckCosts) {
            executeAbility1(context);
            return true;
        }

        // Normal activation flow - check mana only (NO COOLDOWN)
        int cost = context.getConfigManager().getAbility1Cost(getType());
        if (!hasMana(context.getPlayer(), context.getManaManager(), cost)) return false;

        // Execute ability first, only consume mana if successful
        if (executeAbility1(context)) {
            context.getManaManager().spend(context.getPlayer(), cost);
            return true;
        }
        return false;
    }

    @Override
    public boolean ability2(ElementContext context) {
        // Require upgrade level 1 before allowing upgrade level 2
        if (context.getUpgradeLevel() < 1) {
            context.getPlayer().sendMessage(
                    Text.literal("You need Upgrade I before you can use Upgrade II abilities.")
                            .formatted(Formatting.RED),
                    false
            );
            return false;
        }

        if (!checkUpgradeLevel(context.getPlayer(), context.getUpgradeLevel(), 2)) return false;

        // Check if ability can be cancelled
        boolean shouldCheckCosts = !canCancelAbility2(context);

        if (!shouldCheckCosts) {
            executeAbility2(context);
            return true;
        }

        // Normal activation flow - check mana only (NO COOLDOWN)
        int cost = context.getConfigManager().getAbility2Cost(getType());
        if (!hasMana(context.getPlayer(), context.getManaManager(), cost)) return false;

        // Execute ability first, only consume mana if successful
        if (executeAbility2(context)) {
            context.getManaManager().spend(context.getPlayer(), cost);
            return true;
        }
        return false;
    }

    /**
     * Check if player has required upgrade level for ability
     */
    protected boolean checkUpgradeLevel(ServerPlayerEntity player, int upgradeLevel, int requiredLevel) {
        if (upgradeLevel < requiredLevel) {
            player.sendMessage(
                    Text.literal("You need Upgrade " + (requiredLevel == 1 ? "I" : "II") + " to use this ability.")
                            .formatted(Formatting.RED),
                    false
            );
            return false;
        }
        return true;
    }

    /**
     * Check if player has enough mana (without spending it)
     */
    protected boolean hasMana(ServerPlayerEntity player, hs.elementmod.managers.ManaManager mana, int cost) {
        if (mana.get(player.getUuid()).getMana() < cost) {
            player.sendMessage(
                    Text.literal("Not enough mana (" + cost + ")").formatted(Formatting.RED),
                    false
            );
            return false;
        }
        return true;
    }

    /**
     * Template methods to be implemented by concrete elements
     */
    protected abstract boolean executeAbility1(ElementContext context);
    protected abstract boolean executeAbility2(ElementContext context);

    /**
     * Check if ability1 can be cancelled (override in subclasses if needed)
     * @return true if the ability is active and can be cancelled
     */
    protected boolean canCancelAbility1(ElementContext context) {
        return false; // Default: no cancellation support
    }

    /**
     * Check if ability2 can be cancelled (override in subclasses if needed)
     * @return true if the ability is active and can be cancelled
     */
    protected boolean canCancelAbility2(ElementContext context) {
        return false; // Default: no cancellation support
    }

    /**
     * Helper method to check if target is valid (not player or not trusted)
     */
    protected boolean isValidTarget(ServerPlayerEntity player, LivingEntity target, hs.elementmod.managers.TrustManager trust) {
        if (target.equals(player)) return false;
        if (target instanceof ServerPlayerEntity other && trust.isTrusted(player.getUuid(), other.getUuid())) {
            return false;
        }
        return true;
    }

    /**
     * Helper method to check if target is valid using ElementContext
     */
    protected boolean isValidTarget(ElementContext context, LivingEntity target) {
        return isValidTarget(context.getPlayer(), target, context.getTrustManager());
    }

    /**
     * Ability cooldown management methods
     */
    protected boolean isAbility1Active(ServerPlayerEntity player) {
        return activeAbility1.contains(player.getUuid());
    }

    public boolean isAbility2Active(ServerPlayerEntity player) {
        return activeAbility2.contains(player.getUuid());
    }

    protected void setAbility1Active(ServerPlayerEntity player, boolean active) {
        if (active) {
            activeAbility1.add(player.getUuid());
        } else {
            activeAbility1.remove(player.getUuid());
        }
    }

    protected void setAbility2Active(ServerPlayerEntity player, boolean active) {
        if (active) {
            activeAbility2.add(player.getUuid());
        } else {
            activeAbility2.remove(player.getUuid());
        }
    }
}