package com.pokeskies.skiescrates.data.userdata;

import org.bson.codecs.pojo.annotations.BsonProperty;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

public class UserData {
    @BsonProperty("_id")
    public UUID uuid;
    @BsonProperty
    public HashMap<String, CrateData> crates;
    @BsonProperty
    public HashMap<String, Integer> keys;

    public UserData(UUID uuid) {
        this.uuid = uuid;
        this.crates = new HashMap<>();
        this.keys = new HashMap<>();
    }

    public UserData(UUID uuid, HashMap<String, CrateData> crates, HashMap<String, Integer> keys) {
        this.uuid = uuid;
        this.crates = crates;
        this.keys = keys;
    }

    public @Nullable Long getCrateCooldown(String id) {
        return Optional.ofNullable(crates.get(id)).map(data -> data.lastOpen).orElse(null);
    }

    public void addCrateCooldown(String id, Long lastOpen) {
        crates.computeIfAbsent(id, k -> new CrateData(0L, 0)).lastOpen = lastOpen;
    }

    public void addCrateUse(String id) {
        crates.computeIfAbsent(id, k -> new CrateData(0L, 0)).openCount++;
    }

    public void addKeys(String id, int amount) {
        keys.merge(id, amount, Integer::sum);
    }

    public boolean removeKeys(String id, int amount) {
        if (!keys.containsKey(id) || keys.get(id) < amount) return false;
        keys.compute(id, (k, v) -> v > amount ? v - amount : null);
        return true;
    }

    public void setKeys(String id, int amount) {
        keys.put(id, amount);
    }

    @Override
    public String toString() {
        return "UserData{" +
                "uuid=" + uuid +
                ", crates=" + crates +
                ", keys=" + keys +
                '}';
    }
}
