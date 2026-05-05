package com.pokeskies.skiescrates.config

class InteractionOptions(
    val open: GenericClickType = GenericClickType.ANY_RIGHT_CLICK,
    val preview: GenericClickType = GenericClickType.ANY_LEFT_CLICK,
) {
    override fun toString(): String {
        return "InteractionOptions(open=$open, preview=$preview)"
    }
}