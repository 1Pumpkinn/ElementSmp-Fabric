package hs.elementmod.elements.impl.life;

import hs.elementmod.ElementMod;
import hs.elementmod.elements.BaseElement;
import hs.elementmod.elements.ElementContext;
import hs.elementmod.elements.ElementType;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;

import java.util.UUID;

public class LifeElement extends BaseElement {
    private static final UUID HEALTH_MODIFIER_ID = UUID.fromString("a7c9d8e6-1234-5678-9abc-def012345678");

    public LifeElement(ElementMod mod) {
        super(mod);
    }

    @Override
    public ElementType getType() {
        return ElementType.LIFE;
    }

    @Override
    public void applyUpsides(ServerPlayerEntity player, int upgradeLevel) {
        // Upside 1: 15 hearts (30 HP)
        var attr = player.getAttributeInstance(EntityAttributes.MAX_HEALTH);
        if (attr != null && !attr.hasModifier(HEALTH_MODIFIER_ID)) {
            attr.addTemporaryModifier(new EntityAttributeModifier(
                    HEALTH_MODIFIER_ID,
                    10.0, // +10 hearts (20 HP) to base 20 = 30 total
                    EntityAttributeModifier.Operation.ADD_VALUE
            ));
            player.setHealth(player.getMaxHealth());
        }
    }

    @Override
    protected boolean executeAbility1(ElementContext context) {
        // TODO: Implement Life abilities
        return false;
    }

    @Override
    protected boolean executeAbility2(ElementContext context) {
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