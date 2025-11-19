package hs.elementmod.managers;

import hs.elementmod.ElementMod;
import hs.elementmod.config.ConfigManager;
import hs.elementmod.data.DataStore;
import hs.elementmod.data.PlayerData;
import hs.elementmod.elements.Element;
import hs.elementmod.elements.ElementContext;
import hs.elementmod.elements.ElementRegistry;
import hs.elementmod.elements.ElementType;
import hs.elementmod.elements.abilities.AbilityManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashSet;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * Manages all elements in the mod - Fabric 1.21.10 compatible
 */
public class ElementManager {
    private final DataStore store;
    private final ManaManager manaManager;
    private final TrustManager trustManager;
    private final ConfigManager configManager;
    private final AbilityManager abilityManager;
    private final ElementMod mod;
    private final ElementRegistry elementRegistry;
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

        this.elementRegistry = new ElementRegistry(abilityManager, mod);

        ElementMod.LOGGER.info("ElementManager initialized with registry");
    }

    // Access player data
    public PlayerData data(UUID uuid) {
        return store.getPlayerData(uuid);
    }

    // Get the ElementType of a player
    public ElementType getPlayerElement(ServerPlayerEntity player) {
        return Optional.ofNullable(data(player.getUuid()))
                .map(PlayerData::getElementType)
                .orElse(null);
    }

    // Get Element object by type
    public Element get(ElementType type) {
        return elementRegistry.getElement(type);
    }

    // Set a player's element
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

    // Apply upsides for the current element
    public void applyUpsides(ServerPlayerEntity player) {
        PlayerData pd = data(player.getUuid());
        ElementType type = pd.getCurrentElement();

        if (type == null) return;

        Element element = elementRegistry.getElement(type);
        if (element != null) {
            element.applyUpsides(player, pd.getUpgradeLevel(type));
        }
    }

    private void clearOldElementEffects(ServerPlayerEntity player, ElementType oldElement) {
        if (oldElement == null) return;

        Element element = elementRegistry.getElement(oldElement);
        if (element != null) {
            element.clearEffects(player);
        }
    }

    // Use abilities
    public boolean useAbility1(ServerPlayerEntity player) { return useAbility(player, 1); }
    public boolean useAbility2(ServerPlayerEntity player) { return useAbility(player, 2); }

    private boolean useAbility(ServerPlayerEntity player, int number) {
        PlayerData pd = data(player.getUuid());
        ElementType type = getPlayerElement(player);
        if (type == null) return false;

        Element element = elementRegistry.getElement(type);
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

    public ElementRegistry getElementRegistry() {
        return elementRegistry;
    }
}
