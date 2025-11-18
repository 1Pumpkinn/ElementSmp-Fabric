package hs.elementmod.data;

import com.google.gson.*;
import hs.elementmod.elements.ElementType;

import java.util.*;

public class PlayerData {
    private final UUID uuid;
    private ElementType currentElement;
    private final EnumSet<ElementType> ownedItems = EnumSet.noneOf(ElementType.class);
    private int mana = 100;
    private int currentElementUpgradeLevel = 0;
    private final Set<UUID> trustedPlayers = new HashSet<>();

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
    }

    public static PlayerData fromJson(UUID uuid, JsonObject json) {
        PlayerData data = new PlayerData(uuid);

        if (json.has("element") && !json.get("element").isJsonNull()) {
            try {
                data.currentElement = ElementType.valueOf(json.get("element").getAsString());
            } catch (IllegalArgumentException ignored) {}
        }

        data.mana = json.has("mana") ? json.get("mana").getAsInt() : 100;
        data.currentElementUpgradeLevel = json.has("currentUpgradeLevel") ?
                json.get("currentUpgradeLevel").getAsInt() : 0;

        if (json.has("items") && json.get("items").isJsonArray()) {
            JsonArray items = json.getAsJsonArray("items");
            for (JsonElement elem : items) {
                try {
                    data.ownedItems.add(ElementType.valueOf(elem.getAsString()));
                } catch (IllegalArgumentException ignored) {}
            }
        }

        if (json.has("trust") && json.get("trust").isJsonArray()) {
            JsonArray trust = json.getAsJsonArray("trust");
            for (JsonElement elem : trust) {
                try {
                    data.trustedPlayers.add(UUID.fromString(elem.getAsString()));
                } catch (IllegalArgumentException ignored) {}
            }
        }

        return data;
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();

        json.addProperty("element", currentElement != null ? currentElement.name() : null);
        json.addProperty("mana", mana);
        json.addProperty("currentUpgradeLevel", currentElementUpgradeLevel);

        JsonArray items = new JsonArray();
        for (ElementType type : ownedItems) {
            items.add(type.name());
        }
        json.add("items", items);

        JsonArray trust = new JsonArray();
        for (UUID uuid : trustedPlayers) {
            trust.add(uuid.toString());
        }
        json.add("trust", trust);

        return json;
    }

    // Getters and setters
    public UUID getUuid() { return uuid; }

    public int getCurrentElementUpgradeLevel() { return currentElementUpgradeLevel; }

    public void setCurrentElementUpgradeLevel(int level) {
        this.currentElementUpgradeLevel = Math.max(0, Math.min(2, level));
    }

    public ElementType getCurrentElement() { return currentElement; }

    public ElementType getElementType() { return currentElement; }

    public void setCurrentElement(ElementType currentElement) {
        this.currentElement = currentElement;
        if (currentElement != null) {
            this.currentElementUpgradeLevel = 0;
        }
    }

    public void setCurrentElementWithoutReset(ElementType currentElement) {
        this.currentElement = currentElement;
    }

    public int getUpgradeLevel(ElementType type) {
        if (type != null && type.equals(currentElement)) {
            return currentElementUpgradeLevel;
        }
        return 0;
    }

    public void setUpgradeLevel(ElementType type, int level) {
        if (type != null && type.equals(currentElement)) {
            setCurrentElementUpgradeLevel(level);
        }
    }

    public Map<ElementType, Integer> getUpgradesView() {
        Map<ElementType, Integer> map = new EnumMap<>(ElementType.class);
        if (currentElement != null) {
            map.put(currentElement, currentElementUpgradeLevel);
        }
        return map;
    }

    public Set<ElementType> getOwnedItems() { return ownedItems; }

    public boolean hasElementItem(ElementType type) { return ownedItems.contains(type); }

    public void addElementItem(ElementType type) { ownedItems.add(type); }

    public void removeElementItem(ElementType type) { ownedItems.remove(type); }

    public int getMana() { return mana; }

    public void setMana(int mana) { this.mana = Math.max(0, mana); }

    public void addMana(int delta) { setMana(this.mana + delta); }

    // Trust methods
    public Set<UUID> getTrustedPlayers() {
        return new HashSet<>(trustedPlayers);
    }

    public void addTrustedPlayer(UUID uuid) {
        trustedPlayers.add(uuid);
    }

    public void removeTrustedPlayer(UUID uuid) {
        trustedPlayers.remove(uuid);
    }

    public boolean isTrusted(UUID uuid) {
        return trustedPlayers.contains(uuid);
    }

    public void setTrustedPlayers(Set<UUID> trusted) {
        trustedPlayers.clear();
        if (trusted != null) {
            trustedPlayers.addAll(trusted);
        }
    }
}