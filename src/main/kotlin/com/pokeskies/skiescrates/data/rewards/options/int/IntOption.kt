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
        override fun serialize(value: IntOption, type: Type, context: JsonSerializationContext): JsonElement {
            return when (value) {
                is IntValue -> JsonPrimitive(value.int)
                is IntRanged -> {
                    val obj = JsonObject()
                    obj.addProperty("min", value.min)
                    obj.addProperty("max", value.max)
                    obj
                }
                else -> context.serialize(value, value::class.java)
            }
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