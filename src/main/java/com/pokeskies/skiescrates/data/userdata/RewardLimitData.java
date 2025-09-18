package com.pokeskies.skiescrates.data.userdata;

import org.bson.codecs.pojo.annotations.BsonProperty;

public class RewardLimitData {
    @BsonProperty
    public Integer claimed = 0; // Number of times this reward has been claimed
    @BsonProperty
    public Long time = 0L; // Last time the reward was claimed

    public RewardLimitData(Integer claimed, Long time) {
        this.claimed = claimed;
        this.time = time;
    }

    public RewardLimitData() {}

    @Override
    public String toString() {
        return "RewardLimitData{" +
                "claimed=" + claimed +
                ", time=" + time +
                '}';
    }
}
