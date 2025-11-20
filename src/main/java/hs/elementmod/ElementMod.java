package hs.elementmod;

import hs.elementmod.commands.*;
import hs.elementmod.config.ConfigManager;
import hs.elementmod.data.DataStore;
import hs.elementmod.elements.ElementRegistry;
import hs.elementmod.elements.abilities.AbilityManager;
import hs.elementmod.items.ModItems;
import hs.elementmod.listeners.*;
import hs.elementmod.managers.*;
import hs.elementmod.network.NetworkHandler;
import hs.elementmod.recipes.RecipeManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main mod initializer for Element Mod (Fabric 1.21.10)
 * Handles all server-side registration and initialization
 */
public class ElementMod implements ModInitializer {
    public static final String MOD_ID = "elementmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static ElementMod instance;
    private MinecraftServer server;

    // Managers
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
        LOGGER.info("Initializing Element Mod (Fabric 1.21.10)...");

        // Step 1: Register items FIRST (before anything else uses them)
        LOGGER.info("Registering items...");
        ModItems.initialize();

        // Step 2: Register network packets
        LOGGER.info("Registering network packets...");
        NetworkHandler.registerPackets();

        // Step 3: Register lifecycle events
        LOGGER.info("Registering lifecycle events...");
        registerLifecycleEvents();

        // Step 4: Register commands
        LOGGER.info("Registering commands...");
        registerCommands();

        // Step 5: Register recipes
        LOGGER.info("Registering recipes...");
        RecipeManager.registerRecipes();

        LOGGER.info("Element Mod initialized successfully!");
    }

    /**
     * Register server lifecycle events
     */
    private void registerLifecycleEvents() {
        // Server starting - initialize managers
        ServerLifecycleEvents.SERVER_STARTING.register(this::onServerStarting);

        // Server started - start managers
        ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStarted);

        // Server stopping - cleanup
        ServerLifecycleEvents.SERVER_STOPPING.register(this::onServerStopping);

        // Server tick - mana regeneration
        ServerTickEvents.END_SERVER_TICK.register(this::onServerTick);
    }

    /**
     * Register all commands
     */
    private void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            ElementCommand.register(dispatcher);
            ManaCommand.register(dispatcher);
            TrustCommand.register(dispatcher);
            UtilCommand.register(dispatcher);
            ElementInfoCommand.register(dispatcher);
            LOGGER.info("All commands registered successfully");
        });
    }

    /**
     * Called when server is starting - initialize managers
     */
    private void onServerStarting(MinecraftServer server) {
        this.server = server;
        LOGGER.info("Server starting - initializing managers...");
        initializeManagers();
        registerEventListeners();
    }

    /**
     * Called when server has started - start active managers
     */
    private void onServerStarted(MinecraftServer server) {
        LOGGER.info("Server started - starting active managers...");
        if (manaManager != null) {
            manaManager.start();
        }
    }

    /**
     * Called when server is stopping - save data and cleanup
     */
    private void onServerStopping(MinecraftServer server) {
        LOGGER.info("Server stopping - saving data...");
        if (dataStore != null) {
            dataStore.flushAll();
        }
        if (manaManager != null) {
            manaManager.stop();
        }
    }

    /**
     * Called every server tick - update managers
     */
    private void onServerTick(MinecraftServer server) {
        if (manaManager != null) {
            manaManager.tick(server);
        }
    }

    /**
     * Initialize all managers in the correct order
     */
    private void initializeManagers() {
        LOGGER.info("Initializing managers...");

        // Core managers first
        this.configManager = new ConfigManager();
        this.dataStore = new DataStore();

        // Dependency managers
        this.trustManager = new TrustManager(dataStore);
        this.manaManager = new ManaManager(dataStore, configManager);
        this.abilityManager = new AbilityManager(this);

        // Element system
        this.elementManager = new ElementManager(
                dataStore, manaManager, trustManager,
                configManager, abilityManager, this
        );
        this.elementRegistry = elementManager.getElementRegistry();

        // Item manager
        this.itemManager = new ItemManager(manaManager, configManager);

        LOGGER.info("All managers initialized successfully");
    }

    /**
     * Register all event listeners
     */
    private void registerEventListeners() {
        LOGGER.info("Registering event listeners...");

        // Player events
        PlayerEventListeners.register();

        // Combat events
        CombatEventListeners.register();

        // Item events
        ItemEventListeners.register();

        // Game mode events
        GameModeListener.register();

        // Fall damage cancellation (Air element)
        FallDamageListener.register();

        // Crafting events (requires mixin implementation)
        CraftingEventListeners.register();

        LOGGER.info("All event listeners registered successfully");
    }

    // Getters for managers
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