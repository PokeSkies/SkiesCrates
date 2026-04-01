package com.pokeskies.skiescrates.data.key

enum class KeyCheckResult(val priority: Int) {
    NOT_FOUND(4),
    NOT_HOLDING(3),
    NOT_ENOUGH(2),
    SUCCESS(1);

    companion object {
        fun getStandardResult(result: Boolean): KeyCheckResult {
            return if (result) SUCCESS else NOT_ENOUGH
        }
    }
}