package hs.elementmod.elements.impl.air;

import hs.elementmod.ElementMod;
import hs.elementmod.elements.BaseElement;
import hs.elementmod.elements.ElementContext;
import hs.elementmod.elements.ElementType;
import hs.elementmod.elements.abilities.Ability;
import hs.elementmod.elements.abilities.impl.air.AirBlastAbility;
import hs.elementmod.elements.abilities.impl.air.AirDashAbility;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;

public class AirElement extends BaseElement {
    private final ElementMod mod;
    private final Ability ability1;
    private final Ability ability2;

    public AirElement(ElementMod mod) {
        super(mod);
        this.mod = mod;
        this.ability1 = new AirBlastAbility(mod);
        this.ability2 = new AirDashAbility(mod);
    }

    @Override
    public ElementType getType() {
        return ElementType.AIR;
    }

    @Override
    public void applyUpsides(ServerPlayerEntity player, int upgradeLevel) {
        // Upside 1: No fall damage (handled in listener)
        // No potion effects needed
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
        ability1.setActive(player, false);
        ability2.setActive(player, false);
    }

    @Override
    public String getDisplayName() {
        return Formatting.WHITE + "Air";
    }

    @Override
    public String getDescription() {
        return "Master the swift and agile power of air to control movement and push enemies.";
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