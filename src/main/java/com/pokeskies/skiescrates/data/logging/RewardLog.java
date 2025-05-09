package com.pokeskies.skiescrates.data.logging;

import com.pokeskies.skiescrates.SkiesCrates;
import com.pokeskies.skiescrates.utils.Utils;
import org.bson.codecs.pojo.annotations.BsonProperty;

import java.util.UUID;

public class RewardLog extends ILog {
    @BsonProperty
    public UUID uuid;
    @BsonProperty
    public Long timestamp;
    @BsonProperty
    public String crateId;
    @BsonProperty
    public String rewardId;

    public RewardLog(UUID uuid, Long timestamp, String crateId, String rewardId) {
        this.uuid = uuid;
        this.timestamp = timestamp;
        this.crateId = crateId;
        this.rewardId = rewardId;
    }

    @Override
    void logToConsole() {
        Utils.INSTANCE.printInfo(
                String.format(
                        "[Logging] %s opened %s and got %s at %s",
                        uuid.toString(),
                        crateId,
                        rewardId,
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
            storage.writeCrateLog(this);
        }
    }

    @Override
    public String toString() {
        return "RewardLog{" +
                "uuid=" + uuid +
                ", timestamp=" + timestamp +
                ", crateId='" + crateId + '\'' +
                ", rewardId='" + rewardId + '\'' +
                '}';
    }
}
