package hs.elementmod.elements.impl.life;

import hs.elementmod.ElementMod;
import hs.elementmod.elements.BaseElement;
import hs.elementmod.elements.ElementContext;
import hs.elementmod.elements.ElementType;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class LifeElement extends BaseElement {
    private static final Identifier HEALTH_MODIFIER_ID = Identifier.of("elementmod", "life_health_boost");

    public LifeElement(ElementMod mod) {
        super(mod);
    }

    @Override
    public ElementType getType() {
        return ElementType.LIFE;
    }

    @Override
    public void applyUpsides(ServerPlayerEntity player, int upgradeLevel) {
        var attr = player.getAttributeInstance(EntityAttributes.MAX_HEALTH);
        if (attr != null) {
            if (!attr.hasModifier(HEALTH_MODIFIER_ID)) {
                EntityAttributeModifier healthModifier = new EntityAttributeModifier(
                        HEALTH_MODIFIER_ID,
                        10.0,  // +10 HP
                        EntityAttributeModifier.Operation.ADD_VALUE
                );
                attr.addTemporaryModifier(healthModifier);
                player.setHealth(player.getMaxHealth());
            }
        }
    }

    @Override
    protected boolean executeAbility1(ElementContext context) {
        // TODO: Implement Regeneration Aura
        return false;
    }

    @Override
    protected boolean executeAbility2(ElementContext context) {
        // TODO: Implement Healing Beam
        return false;
    }

    @Override
    public void clearEffects(ServerPlayerEntity player) {
        var attr = player.getAttributeInstance(EntityAttributes.MAX_HEALTH);
        if (attr != null) {
            attr.removeModifier(HEALTH_MODIFIER_ID);
        }
    }

    @Override
    public String getDisplayName() {
        return Formatting.GREEN + "Life";
    }

    @Override
    public String getDescription() {
        return "Masters of healing and growth.";
    }

    @Override
    public String getAbility1Name() {
        return "Regeneration Aura";
    }

    @Override
    public String getAbility1Description() {
        return "Grant regeneration to yourself and trusted allies.";
    }

    @Override
    public String getAbility2Name() {
        return "Healing Beam";
    }

    @Override
    public String getAbility2Description() {
        return "Project a beam of healing energy.";
    }
}
