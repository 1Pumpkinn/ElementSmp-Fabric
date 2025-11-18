package hs.elementmod.config;

import com.google.gson.*;
import hs.elementmod.ElementMod;
import hs.elementmod.elements.ElementType;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {
    private final Path configPath;
    private JsonObject config;
    private final Gson gson;

    public ConfigManager() {
        this.configPath = FabricLoader.getInstance().getConfigDir().resolve("elementmod/config.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        loadConfig();
    }

    private void loadConfig() {
        try {
            Files.createDirectories(configPath.getParent());

            if (!Files.exists(configPath)) {
                createDefaultConfig();
            }

            String content = Files.readString(configPath);
            config = JsonParser.parseString(content).getAsJsonObject();
        } catch (IOException e) {
            ElementMod.LOGGER.error("Failed to load config", e);
            config = new JsonObject();
        }
    }

    private void createDefaultConfig() throws IOException {
        JsonObject defaultConfig = new JsonObject();

        JsonObject mana = new JsonObject();
        mana.addProperty("max", 100);
        mana.addProperty("regen_per_second", 1);
        defaultConfig.add("mana", mana);

        JsonObject costs = new JsonObject();
        for (ElementType type : ElementType.values()) {
            JsonObject elementCosts = new JsonObject();
            elementCosts.addProperty("ability1", 50);
            elementCosts.addProperty("ability2", 75);
            elementCosts.addProperty("item_use", 75);
            costs.add(type.name().toLowerCase(), elementCosts);
        }
        defaultConfig.add("costs", costs);

        Files.writeString(configPath, gson.toJson(defaultConfig));
        config = defaultConfig;
    }

    public void reload() {
        loadConfig();
    }

    public int getMaxMana() {
        return config.has("mana") ? config.getAsJsonObject("mana").get("max").getAsInt() : 100;
    }

    public int getManaRegenPerSecond() {
        return config.has("mana") ? config.getAsJsonObject("mana").get("regen_per_second").getAsInt() : 1;
    }

    public int getAbility1Cost(ElementType type) {
        return getCost(type, "ability1", 50);
    }

    public int getAbility2Cost(ElementType type) {
        return getCost(type, "ability2", 75);
    }

    public int getItemUseCost(ElementType type) {
        return getCost(type, "item_use", 75);
    }

    private int getCost(ElementType type, String key, int defaultValue) {
        try {
            return config.getAsJsonObject("costs")
                    .getAsJsonObject(type.name().toLowerCase())
                    .get(key).getAsInt();
        } catch (Exception e) {
            return defaultValue;
        }
    }
}