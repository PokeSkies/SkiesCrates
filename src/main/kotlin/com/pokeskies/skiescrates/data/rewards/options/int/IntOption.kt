package com.pokeskies.skiescrates.data.rewards.options.int

import com.google.gson.*
import com.google.gson.annotations.JsonAdapter
import java.lang.reflect.Type

@JsonAdapter(IntOption.Adapter::class)
interface IntOption {
    fun getValue(): Int

    fun getValueClamped(limitMin: Int, limitMax: Int): Int {
        return clamp(getValue(), limitMin, limitMax)
    }

    private fun clamp(value: Int, limitMin: Int, limitMax: Int): Int {
        var result = value
        if (result < limitMin) result = limitMin
        if (result > limitMax) result = limitMax
        return result
    }

    class Adapter : JsonSerializer<IntOption>, JsonDeserializer<IntOption> {
        override fun serialize(src: IntOption, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            val obj = JsonObject()

            when (src) {
                is IntValue -> {
                    obj.addProperty("value", src.int)
                }
                is IntRanged -> {
                    obj.addProperty("min", src.min)
                    obj.addProperty("max", src.max)
                }
            }

            return obj
        }

        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): IntOption {
            if (json.isJsonPrimitive && json.asJsonPrimitive.isNumber) {
                // Simple case: just a direct int value
                return IntValue(int = json.asInt)
            }

            val obj = json.asJsonObject

            // Check if it's a range or direct value
            return if (obj.has("min") && obj.has("max")) {
                IntRanged(
                    min = obj.get("min").asInt,
                    max = obj.get("max").asInt
                )
            } else if (obj.has("value")) {
                IntValue(
                    int = obj.get("value").asInt
                )
            } else {
                throw JsonParseException("IntOption must have either 'value' or 'min'/'max' fields")
            }
        }
    }
}