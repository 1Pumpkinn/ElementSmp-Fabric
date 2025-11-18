package hs.elementmod.managers;

import hs.elementmod.ElementMod;
import hs.elementmod.config.ConfigManager;
import hs.elementmod.data.DataStore;
import hs.elementmod.data.PlayerData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameMode;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ManaManager {
    private final DataStore store;
    private final ConfigManager configManager;
    private final Map<UUID, PlayerData> cache = new HashMap<>();
    private boolean running = false;
    private int tickCounter = 0;

    public ManaManager(DataStore store, ConfigManager configManager) {
        this.store = store;
        this.configManager = configManager;
    }

    public void start() {
        running = true;
        ElementMod.LOGGER.info("Mana manager started");
    }

    public void stop() {
        running = false;
        cache.values().forEach(store::save);
        cache.clear();
    }

    public void tick(MinecraftServer server) {
        if (!running) return;

        tickCounter++;
        if (tickCounter < 20) return; // Every second
        tickCounter = 0;

        int maxMana = configManager.getMaxMana();
        int regenRate = configManager.getManaRegenPerSecond();

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            PlayerData pd = get(player.getUuid());

            if (player.interactionManager.getGameMode() == GameMode.CREATIVE) {
                pd.setMana(maxMana);
            } else {
                if (pd.getMana() < maxMana) {
                    pd.addMana(regenRate);
                    if (pd.getMana() > maxMana) {
                        pd.setMana(maxMana);
                    }
                    store.save(pd);
                }
            }

            String manaDisplay = player.interactionManager.getGameMode() == GameMode.CREATIVE ?
                    "∞" : String.valueOf(pd.getMana());

            player.sendMessage(
                    Text.literal("Ⓜ Mana: ").formatted(Formatting.AQUA)
                            .append(Text.literal(manaDisplay).formatted(Formatting.WHITE))
                            .append(Text.literal("/" + maxMana).formatted(Formatting.GRAY)),
                    true
            );
        }
    }

    public PlayerData get(UUID uuid) {
        return cache.computeIfAbsent(uuid, store::load);
    }

    public void save(UUID uuid) {
        PlayerData pd = cache.get(uuid);
        if (pd != null) store.save(pd);
    }

    public boolean spend(ServerPlayerEntity player, int amount) {
        if (player.interactionManager.getGameMode() == GameMode.CREATIVE) {
            return true;
        }

        PlayerData pd = get(player.getUuid());
        if (pd.getMana() < amount) return false;

        pd.addMana(-amount);
        store.save(pd);
        return true;
    }

    public boolean hasMana(ServerPlayerEntity player, int amount) {
        if (player.interactionManager.getGameMode() == GameMode.CREATIVE) {
            return true;
        }

        PlayerData pd = get(player.getUuid());
        return pd.getMana() >= amount;
    }
}