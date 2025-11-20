package hs.elementmod.elements.impl.metal;

import hs.elementmod.ElementMod;
import hs.elementmod.elements.BaseElement;
import hs.elementmod.elements.ElementType;
import hs.elementmod.elements.abilities.Ability;
import hs.elementmod.elements.abilities.impl.metal.MetalChainAbility;
import hs.elementmod.elements.abilities.impl.metal.MetalDashAbility;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;

/**
 * Metal element implementation. Creates and holds metal abilities.
 */
public class MetalElement extends BaseElement {
    private final Ability ability1;
    private final Ability ability2;

    public MetalElement(ElementMod mod) {
        super(mod);
        this.ability1 = new MetalChainAbility(mod);
        this.ability2 = new MetalDashAbility(mod);
    }

    @Override
    public ElementType getType() {
        return ElementType.METAL;
    }

    @Override
    protected boolean executeAbility1(hs.elementmod.elements.ElementContext context) {
        return ability1.execute(context);
    }

    @Override
    protected boolean executeAbility2(hs.elementmod.elements.ElementContext context) {
        return ability2.execute(context);
    }

    @Override
    public void applyUpsides(ServerPlayerEntity player, int upgradeLevel) {
        // Implement upsides for metal if required
    }

    @Override
    public void clearEffects(ServerPlayerEntity player) {
        ability1.setActive(player, false);
        ability2.setActive(player, false);
    }

    @Override
    public String getDisplayName() {
        return Formatting.GRAY + "Metal";
    }

    @Override
    public String getDescription() {
        return "Wield metal to control and reel in enemies, or charge forward in a deadly dash.";
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
