package hs.elementmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hs.elementmod.commands.*;
import hs.elementmod.config.ConfigManager;
import hs.elementmod.data.DataStore;
import hs.elementmod.elements.ElementRegistry;
import hs.elementmod.elements.abilities.AbilityManager;
import hs.elementmod.listeners.*;
import hs.elementmod.managers.*;

/**
 * Main mod initializer for Element Mod (Fabric)
 * Converted from Paper plugin to Fabric mod
 */
public class ElementMod implements ModInitializer {
    public static final String MOD_ID = "elementmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static ElementMod instance;
    private MinecraftServer server;

    private DataStore dataStore;
    private ConfigManager configManager;
    private ElementManager elementManager;
    private ManaManager manaManager;
    private TrustManager trustManager;
    private ItemManager itemManager;
    private AbilityManager abilityManager;
    private ElementRegistry elementRegistry;

    @Override
    public void onInitialize() {
        instance = this;
        LOGGER.info("Initializing Element Mod (Fabric)...");

        // Register lifecycle events
        ServerLifecycleEvents.SERVER_STARTING.register(this::onServerStarting);
        ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStarted);
        ServerLifecycleEvents.SERVER_STOPPING.register(this::onServerStopping);
        ServerTickEvents.END_SERVER_TICK.register(this::onServerTick);

        // Register commands
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            ElementCommand.register(dispatcher);
            ManaCommand.register(dispatcher);
            TrustCommand.register(dispatcher);
            UtilCommand.register(dispatcher);
            ElementInfoCommand.register(dispatcher);
        });

        LOGGER.info("Element Mod initialized!");
    }

    private void onServerStarting(MinecraftServer server) {
        this.server = server;
        initializeManagers();
        registerAbilities();
        registerListeners();
    }

    private void onServerStarted(MinecraftServer server) {
        if (manaManager != null) {
            manaManager.start();
        }
    }

    private void onServerStopping(MinecraftServer server) {
        if (dataStore != null) {
            dataStore.flushAll();
        }
        if (manaManager != null) {
            manaManager.stop();
        }
    }

    private void onServerTick(MinecraftServer server) {
        if (manaManager != null) {
            manaManager.tick(server);
        }
    }

    private void initializeManagers() {
        this.configManager = new ConfigManager();
        this.dataStore = new DataStore();
        this.trustManager = new TrustManager(dataStore);
        this.manaManager = new ManaManager(dataStore, configManager);
        this.abilityManager = new AbilityManager();
        this.elementRegistry = new ElementRegistry(abilityManager);
        this.elementManager = new ElementManager(dataStore, manaManager, trustManager, configManager, abilityManager);
        this.itemManager = new ItemManager(manaManager, configManager);
    }

    private void registerAbilities() {
        elementRegistry.registerAllAbilities();
    }

    private void registerListeners() {
        PlayerEventListeners.register();
        CombatEventListeners.register();
        AbilityEventListeners.register();
        ItemEventListeners.register();
        GameModeListener.register();
    }

    public static ElementMod getInstance() {
        return instance;
    }

    public MinecraftServer getServer() {
        return server;
    }

    public DataStore getDataStore() {
        return dataStore;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ElementManager getElementManager() {
        return elementManager;
    }

    public ManaManager getManaManager() {
        return manaManager;
    }

    public TrustManager getTrustManager() {
        return trustManager;
    }

    public ItemManager getItemManager() {
        return itemManager;
    }

    public AbilityManager getAbilityManager() {
        return abilityManager;
    }

    public ElementRegistry getElementRegistry() {
        return elementRegistry;
    }
}