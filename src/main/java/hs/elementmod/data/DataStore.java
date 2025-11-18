package hs.elementmod.data;

import com.google.gson.*;
import hs.elementmod.ElementMod;
import hs.elementmod.elements.ElementType;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class DataStore {
    private final Path dataDir;
    private final Path playerFile;
    private final Path serverFile;
    private final Gson gson;

    private final Map<UUID, PlayerData> playerDataCache = new HashMap<>();
    private JsonObject serverData;

    public DataStore() {
        this.dataDir = FabricLoader.getInstance().getConfigDir().resolve("elementmod").resolve("data");
        this.playerFile = dataDir.resolve("players.json");
        this.serverFile = dataDir.resolve("server.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();

        initializeFiles();
        loadServerData();
    }

    private void initializeFiles() {
        try {
            Files.createDirectories(dataDir);

            if (!Files.exists(playerFile)) {
                Files.createFile(playerFile);
                Files.writeString(playerFile, "{}");
            }

            if (!Files.exists(serverFile)) {
                Files.createFile(serverFile);
                Files.writeString(serverFile, "{}");
            }
        } catch (IOException e) {
            ElementMod.LOGGER.error("Failed to initialize data files", e);
        }
    }

    public PlayerData getPlayerData(UUID uuid) {
        if (playerDataCache.containsKey(uuid)) {
            return playerDataCache.get(uuid);
        }

        PlayerData data = loadPlayerDataFromFile(uuid);
        playerDataCache.put(uuid, data);
        return data;
    }

    private PlayerData loadPlayerDataFromFile(UUID uuid) {
        try {
            String content = Files.readString(playerFile);
            JsonObject root = JsonParser.parseString(content).getAsJsonObject();

            if (root.has(uuid.toString())) {
                JsonObject playerObj = root.getAsJsonObject(uuid.toString());
                return PlayerData.fromJson(uuid, playerObj);
            }
        } catch (Exception e) {
            ElementMod.LOGGER.error("Failed to load player data for " + uuid, e);
        }

        return new PlayerData(uuid);
    }

    public synchronized PlayerData load(UUID uuid) {
        return getPlayerData(uuid);
    }

    public synchronized void save(PlayerData pd) {
        try {
            String content = Files.readString(playerFile);
            JsonObject root = JsonParser.parseString(content).getAsJsonObject();

            root.add(pd.getUuid().toString(), pd.toJson());

            playerDataCache.put(pd.getUuid(), pd);
            Files.writeString(playerFile, gson.toJson(root));
        } catch (Exception e) {
            ElementMod.LOGGER.error("Failed to save player data for " + pd.getUuid(), e);
        }
    }

    public synchronized void invalidateCache(UUID uuid) {
        playerDataCache.remove(uuid);
    }

    public synchronized void flushAll() {
        // All data is already saved to disk synchronously
        ElementMod.LOGGER.info("Data flushed successfully");
    }

    // Trust store
    public synchronized Set<UUID> getTrusted(UUID owner) {
        PlayerData pd = getPlayerData(owner);
        return pd.getTrustedPlayers();
    }

    public synchronized void setTrusted(UUID owner, Set<UUID> trusted) {
        PlayerData pd = getPlayerData(owner);
        pd.setTrustedPlayers(trusted);
        save(pd);
    }

    // Server data
    private void loadServerData() {
        try {
            String content = Files.readString(serverFile);
            serverData = JsonParser.parseString(content).getAsJsonObject();
        } catch (Exception e) {
            serverData = new JsonObject();
        }
    }

    private void saveServerData() {
        try {
            Files.writeString(serverFile, gson.toJson(serverData));
        } catch (IOException e) {
            ElementMod.LOGGER.error("Failed to save server data", e);
        }
    }

    public synchronized boolean isLifeElementCrafted() {
        return serverData.has("life_crafted") && serverData.get("life_crafted").getAsBoolean();
    }

    public synchronized void setLifeElementCrafted(boolean crafted) {
        serverData.addProperty("life_crafted", crafted);
        saveServerData();
    }

    public synchronized boolean isDeathElementCrafted() {
        return serverData.has("death_crafted") && serverData.get("death_crafted").getAsBoolean();
    }

    public synchronized void setDeathElementCrafted(boolean crafted) {
        serverData.addProperty("death_crafted", crafted);
        saveServerData();
    }
}