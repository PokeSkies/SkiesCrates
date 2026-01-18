package com.pokeskies.skiescrates.data.rewards.options.boolean

import com.google.gson.*
import java.lang.reflect.Type

interface BooleanOption {
    fun getValue(): Boolean

    class Adapter : JsonSerializer<BooleanOption>, JsonDeserializer<BooleanOption> {
        override fun serialize(
            src: BooleanOption,
            typeOfSrc: Type,
            context: JsonSerializationContext
        ): JsonElement {
            return context.serialize(src, src::class.java)
        }

        override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext
        ): BooleanOption {
            val prim = json.asJsonPrimitive
            if (prim.isBoolean) {
                return BooleanValue(prim.asBoolean)
            }

            if (prim.isNumber) {
                return BooleanChance(prim.asFloat)
            }

            if (prim.isString) {
                val value = prim.asString.lowercase()
                if (value == "true" || value == "false") {
                    return BooleanValue(value.toBoolean())
                }
                try {
                    return BooleanChance(value.toFloat())
                } catch (_: NumberFormatException) {}
            }

            throw JsonParseException("Unable to deserialize BooleanOption from: $json")
        }
    }
}