package com.pokeskies.skiescrates.data.actions

import com.google.gson.*
import com.pokeskies.skiescrates.data.actions.types.*
import java.lang.reflect.Type

enum class ActionType(val identifier: String, val clazz: Class<*>) {
    COMMAND_CONSOLE("command_console", CommandConsole::class.java),
    COMMAND_PLAYER("command_player", CommandPlayer::class.java),
    MESSAGE("message", MessagePlayer::class.java),
    BROADCAST("broadcast", MessageBroadcast::class.java),
    PLAYSOUND("playsound", PlaySound::class.java);

    companion object {
        fun valueOfAnyCase(name: String): ActionType? {
            for (type in entries) {
                if (name.equals(type.identifier, true)) return type
            }
            return null
        }
    }

    internal class ActionTypeAdaptor : JsonSerializer<Action>, JsonDeserializer<Action> {
        override fun serialize(src: Action, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            return context.serialize(src, src::class.java)
        }

        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Action {
            val jsonObject: JsonObject = json.getAsJsonObject()
            val value = jsonObject.get("type").asString
            val type: ActionType? = valueOfAnyCase(value)
            return try {
                context.deserialize(json, type!!.clazz)
            } catch (e: NullPointerException) {
                throw JsonParseException("Could not deserialize action type: $value", e)
            }
        }
    }
}
