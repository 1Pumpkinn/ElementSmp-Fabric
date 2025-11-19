package hs.elementmod.elements.impl.fire;

import hs.elementmod.ElementMod;
import hs.elementmod.elements.BaseElement;
import hs.elementmod.elements.ElementContext;
import hs.elementmod.elements.ElementType;
import hs.elementmod.elements.abilities.Ability;
import hs.elementmod.elements.abilities.impl.fire.FireballAbility;
import hs.elementmod.elements.abilities.impl.fire.MeteorShowerAbility;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;

public class FireElement extends BaseElement {
    private final Ability ability1;
    private final Ability ability2;

    public FireElement(ElementMod mod) {
        super(mod);
        this.ability1 = new FireballAbility(mod);
        this.ability2 = new MeteorShowerAbility(mod);
    }

    @Override
    public ElementType getType() {
        return ElementType.FIRE;
    }

    @Override
    public void applyUpsides(ServerPlayerEntity player, int upgradeLevel) {
        // Upside 1: Infinite Fire Resistance
        player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.FIRE_RESISTANCE,
                Integer.MAX_VALUE, 0, true, false
        ));

        // Upside 2: Fire Aspect on hits (handled in listener)
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
        player.removeStatusEffect(StatusEffects.FIRE_RESISTANCE);
        ability1.setActive(player, false);
        ability2.setActive(player, false);
    }

    @Override
    public String getDisplayName() {
        return Formatting.RED + "Fire";
    }

    @Override
    public String getDescription() {
        return "Masters of flame and destruction.";
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
