package hs.elementmod.managers;

import hs.elementmod.ElementMod;
import hs.elementmod.config.ConfigManager;
import hs.elementmod.data.DataStore;
import hs.elementmod.data.PlayerData;
import hs.elementmod.elements.*;
import hs.elementmod.elements.abilities.AbilityManager;
import hs.elementmod.elements.impl.air.AirElement;
import hs.elementmod.elements.impl.water.WaterElement;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.*;

/**
 * Manages all elements in the mod
 * Converted from Paper to Fabric 1.21.1
 */
public class ElementManager {
    private final DataStore store;
    private final ManaManager manaManager;
    private final TrustManager trustManager;
    private final ConfigManager configManager;
    private final AbilityManager abilityManager;
    private final ElementMod mod;
    private final Map<ElementType, Element> registry = new EnumMap<>(ElementType.class);
    private final Set<UUID> currentlyRolling = new HashSet<>();
    private final Random random = new Random();

    public ElementManager(DataStore store, ManaManager manaManager, TrustManager trustManager,
                          ConfigManager configManager, AbilityManager abilityManager, ElementMod mod) {
        this.store = store;
        this.manaManager = manaManager;
        this.trustManager = trustManager;
        this.configManager = configManager;
        this.abilityManager = abilityManager;
        this.mod = mod;
        registerAllElements();
    }

    private void registerAllElements() {
        // Register implemented elements
        registry.put(ElementType.AIR, new AirElement(mod));
        registry.put(ElementType.WATER, new WaterElement(mod));

        // TODO: Register other elements as they are implemented
        // registry.put(ElementType.FIRE, new FireElement(mod));
        // registry.put(ElementType.EARTH, new EarthElement(mod));
        // registry.put(ElementType.LIFE, new LifeElement(mod));
        // registry.put(ElementType.DEATH, new DeathElement(mod));
        // registry.put(ElementType.METAL, new MetalElement(mod));
        // registry.put(ElementType.FROST, new FrostElement(mod));

        ElementMod.LOGGER.info("Registered all elements");
    }

    public PlayerData data(UUID uuid) {
        return store.getPlayerData(uuid);
    }

    public Element get(ElementType type) {
        return registry.get(type);
    }

    public ElementType getPlayerElement(ServerPlayerEntity player) {
        return Optional.ofNullable(data(player.getUuid()))
                .map(PlayerData::getElementType)
                .orElse(null);
    }

    public void setElement(ServerPlayerEntity player, ElementType type) {
        PlayerData pd = data(player.getUuid());
        ElementType old = pd.getCurrentElement();

        if (old != null && old != type) {
            clearOldElementEffects(player, old);
        }

        pd.setCurrentElement(type);
        store.save(pd);

        player.sendMessage(Text.literal("Your element is now ")
                .formatted(Formatting.GOLD)
                .append(Text.literal(type.name()).formatted(Formatting.AQUA)), false);

        applyUpsides(player);
    }

    public void applyUpsides(ServerPlayerEntity player) {
        PlayerData pd = data(player.getUuid());
        ElementType type = pd.getCurrentElement();

        if (type == null) return;

        Element element = registry.get(type);
        if (element != null) {
            element.applyUpsides(player, pd.getUpgradeLevel(type));
        }
    }

    private void clearOldElementEffects(ServerPlayerEntity player, ElementType oldElement) {
        if (oldElement == null) return;

        Element element = registry.get(oldElement);
        if (element != null) {
            element.clearEffects(player);
        }
    }

    public boolean useAbility1(ServerPlayerEntity player) {
        return useAbility(player, 1);
    }

    public boolean useAbility2(ServerPlayerEntity player) {
        return useAbility(player, 2);
    }

    private boolean useAbility(ServerPlayerEntity player, int number) {
        PlayerData pd = data(player.getUuid());
        ElementType type = pd.getCurrentElement();
        Element element = registry.get(type);

        if (element == null) return false;

        ElementContext ctx = ElementContext.builder()
                .player(player)
                .upgradeLevel(pd.getUpgradeLevel(type))
                .elementType(type)
                .manaManager(manaManager)
                .trustManager(trustManager)
                .configManager(configManager)
                .mod(mod)
                .build();

        return number == 1 ? element.ability1(ctx) : element.ability2(ctx);
    }

    public boolean isCurrentlyRolling(ServerPlayerEntity player) {
        return currentlyRolling.contains(player.getUuid());
    }

    public void cancelRolling(ServerPlayerEntity player) {
        currentlyRolling.remove(player.getUuid());
    }
}