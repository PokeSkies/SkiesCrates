package com.pokeskies.skiescrates.data.userdata;

import org.bson.codecs.pojo.annotations.BsonProperty;

import java.util.Map;

public class CrateData {
    @BsonProperty
    public Long lastOpen;
    @BsonProperty
    public Integer openCount;
    @BsonProperty
    public Map<String, RewardLimitData> rewards;

    public CrateData(Long lastOpen, Integer openCount, Map<String, RewardLimitData> rewards) {
        this.lastOpen = lastOpen;
        this.openCount = openCount;
        this.rewards = rewards;
    }

    public CrateData() {}

    @Override
    public String toString() {
        return "CrateData{" +
                "lastOpen=" + lastOpen +
                ", openCount=" + openCount +
                ", rewards=" + rewards +
                '}';
    }
}
