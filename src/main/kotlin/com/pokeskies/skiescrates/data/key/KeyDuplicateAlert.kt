package com.pokeskies.skiescrates.data.key

enum class KeyDuplicateAlert(val message: String) {
    MISSING_UUID("Player %player% (%player_uuid%) used a unique key (%key_id%) that has an empty unique id"),
    STACKED("Player %player% (%player_uuid%) used a unique key (%key_id%) that is stacked %amount%x: %key_uuid%"),
    INVALID_UUID("Player %player% (%player_uuid%) used a unique key (%key_id%) that contains an invalid unique id: %key_uuid%"),
    ALREADY_USED("Player %player% (%player_uuid%) used a unique key (%key_id%) that has an already used unique id: %key_uuid%");
}