package com.pokeskies.skiescrates.data.userdata;

import org.bson.codecs.pojo.annotations.BsonProperty;

public class CrateData {
    @BsonProperty
    public Long lastOpen;
    @BsonProperty
    public Integer openCount;

    public CrateData(Long lastOpen, Integer openCount) {
        this.lastOpen = lastOpen;
        this.openCount = openCount;
    }

    @Override
    public String toString() {
        return "CrateData{" +
                "lastOpen=" + lastOpen +
                ", openCount=" + openCount +
                '}';
    }
}
