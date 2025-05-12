package com.pokeskies.skiescrates.data.logging;

import com.pokeskies.skiescrates.SkiesCrates;
import com.pokeskies.skiescrates.utils.Utils;
import org.bson.codecs.pojo.annotations.BsonProperty;

import java.util.UUID;

public class CrateLogEntry extends LogEntry {
    @BsonProperty
    public UUID uuid;
    @BsonProperty
    public Long timestamp;
    @BsonProperty
    public String crateId;
    //  This is a list of rewardIds separated by commas
    @BsonProperty
    public String rewardsList;

    public CrateLogEntry(UUID uuid, Long timestamp, String crateId, String rewardsList) {
        this.uuid = uuid;
        this.timestamp = timestamp;
        this.crateId = crateId;
        this.rewardsList = rewardsList;
    }

    @Override
    void logToConsole() {
        Utils.INSTANCE.printInfo(
                String.format(
                        "[Logging] %s opened %s and got %s at %s",
                        uuid.toString(),
                        crateId,
                        rewardsList,
                        timestamp.toString()
                )
        );
    }

    @Override
    void logToFile() {

    }

    @Override
    void logToStorage() {
        var storage = SkiesCrates.INSTANCE.getStorage();
        if (storage != null) {
            storage.writeCrateLogAsync(this);
        }
    }

    @Override
    public String toString() {
        return "RewardLog{" +
                "uuid=" + uuid +
                ", timestamp=" + timestamp +
                ", crateId='" + crateId + '\'' +
                ", rewardsList='" + rewardsList + '\'' +
                '}';
    }
}
