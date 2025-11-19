package hs.elementmod.elements.abilities.impl.earth;

import hs.elementmod.ElementMod;
import hs.elementmod.elements.abilities.BaseAbility;
import hs.elementmod.elements.ElementContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.*;

public class EarthCharmAbility extends BaseAbility {
    public static final String META_CHARM_NEXT = "earth_charm_next";
    private final ElementMod mod;
    private final Map<UUID, Long> charmReadyUntil = new HashMap<>();

    public EarthCharmAbility(ElementMod mod) {
        super("earth_charm", 75, 30, 1);
        this.mod = mod;
    }

    @Override
    public boolean execute(ElementContext context) {
        ServerPlayerEntity player = context.getPlayer();
        long until = System.currentTimeMillis() + 30_000L;

        charmReadyUntil.put(player.getUuid(), until);

        player.sendMessage(Text.literal("Punch a mob to charm it for 30s - it will follow you!")
                .formatted(Formatting.GOLD), false);

        return true;
    }

    public boolean isCharmReady(UUID playerId) {
        Long until = charmReadyUntil.get(playerId);
        if (until == null) return false;
        if (System.currentTimeMillis() > until) {
            charmReadyUntil.remove(playerId);
            return false;
        }
        return true;
    }

    public void consumeCharm(UUID playerId) {
        charmReadyUntil.remove(playerId);
    }

    @Override
    public String getName() {
        return "Mob Charm";
    }

    @Override
    public String getDescription() {
        return "Punch a mob to make it follow you for 30 seconds.";
    }
}