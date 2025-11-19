package hs.elementmod.elements.impl.earth;

import hs.elementmod.ElementMod;
import hs.elementmod.elements.BaseElement;
import hs.elementmod.elements.ElementContext;
import hs.elementmod.elements.ElementType;
import hs.elementmod.elements.abilities.Ability;
import hs.elementmod.elements.abilities.impl.earth.EarthTunnelAbility;
import hs.elementmod.elements.abilities.impl.earth.EarthCharmAbility;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;

public class EarthElement extends BaseElement {
    private final Ability ability1;
    private final Ability ability2;

    public EarthElement(ElementMod mod) {
        super(mod);
        this.ability1 = new EarthTunnelAbility(mod);
        this.ability2 = new EarthCharmAbility(mod);
    }

    @Override
    public ElementType getType() {
        return ElementType.EARTH;
    }

    @Override
    public void applyUpsides(ServerPlayerEntity player, int upgradeLevel) {
        // Upside 1: Hero of the Village
        player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.HERO_OF_THE_VILLAGE,
                Integer.MAX_VALUE, 0, true, false
        ));
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
        player.removeStatusEffect(StatusEffects.HERO_OF_THE_VILLAGE);
        ability1.setActive(player, false);
        ability2.setActive(player, false);
    }

    @Override
    public String getDisplayName() {
        return Formatting.YELLOW + "Earth";
    }

    @Override
    public String getDescription() {
        return "Masters of stone and earth.";
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
