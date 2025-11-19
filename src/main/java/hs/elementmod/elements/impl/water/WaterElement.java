package hs.elementmod.elements.impl.water;

import hs.elementmod.ElementMod;
import hs.elementmod.elements.BaseElement;
import hs.elementmod.elements.ElementContext;
import hs.elementmod.elements.ElementType;
import hs.elementmod.elements.abilities.Ability;
import hs.elementmod.elements.abilities.impl.water.WaterBeamAbility;
import hs.elementmod.elements.abilities.impl.water.WaterGeyserAbility;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;

/**
 * Complete Water Element implementation for Fabric 1.21.1
 * Demonstrates full conversion from Paper API to Fabric API
 */
public class WaterElement extends BaseElement {
    private final ElementMod mod;
    private final Ability ability1;
    private final Ability ability2;

    public WaterElement(ElementMod mod) {
        super(mod);
        this.mod = mod;
        this.ability1 = new WaterGeyserAbility(mod);
        this.ability2 = new WaterBeamAbility(mod);
    }

    @Override
    public ElementType getType() {
        return ElementType.WATER;
    }

    @Override
    public void applyUpsides(ServerPlayerEntity player, int upgradeLevel) {
        // Upside 1: Infinite conduit power (underwater breathing + vision)
        // Paper version: new PotionEffect(PotionEffectType.CONDUIT_POWER, Integer.MAX_VALUE, 0, true, false)
        player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.CONDUIT_POWER,
                Integer.MAX_VALUE,  // infinite duration
                0,                  // amplifier (0 = level 1)
                true,               // ambient
                false               // show particles
        ));

        if (upgradeLevel >= 2) {
            // Upside 2: Dolphins grace 5 (level 4 = dolphins grace 5)
            // Paper version: new PotionEffect(PotionEffectType.DOLPHINS_GRACE, Integer.MAX_VALUE, 4, true, false)
            player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.DOLPHINS_GRACE,
                    Integer.MAX_VALUE,  // infinite duration
                    4,                  // amplifier (4 = level 5)
                    true,               // ambient
                    false               // show particles
            ));
        }
    }

    @Override
    protected boolean executeAbility1(ElementContext context) {
        return ability1.execute(context);
    }

    @Override
    protected boolean executeAbility2(ElementContext context) {
        return ability2.execute(context);
    }

    @Override
    public void clearEffects(ServerPlayerEntity player) {
        // Remove status effects when switching elements
        // Paper version: player.removePotionEffect(PotionEffectType.CONDUIT_POWER)
        player.removeStatusEffect(StatusEffects.CONDUIT_POWER);
        player.removeStatusEffect(StatusEffects.DOLPHINS_GRACE);

        ability1.setActive(player, false);
        ability2.setActive(player, false);
    }

    @Override
    public String getDisplayName() {
        // Paper version: ChatColor.AQUA + "Water"
        return Formatting.AQUA + "Water";
    }

    @Override
    public String getDescription() {
        return "Harness the flowing power of water to control the battlefield.";
    }

    @Override
    public String getAbility1Name() {
        return ability1.getName();
    }

    @Override
    public String getAbility1Description() {
        return ability1.getDescription();
    }

    @Override
    public String getAbility2Name() {
        return ability2.getName();
    }

    @Override
    public String getAbility2Description() {
        return ability2.getDescription();
    }
}