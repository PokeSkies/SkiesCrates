package com.pokeskies.skiescrates.config

import com.google.gson.*
import com.pokeskies.skiescrates.events.CrateInteractionEvent
import com.pokeskies.skiescrates.utils.Utils
import eu.pb4.sgui.api.ClickType
import net.minecraft.util.StringRepresentable
import java.lang.reflect.Type

enum class GenericClickType(
    val identifier: String,
    val buttonClicks: List<ClickType>,
    val interactionTypes: List<CrateInteractionEvent.InteractionType>
): StringRepresentable {
    LEFT_CLICK("left_click",
        listOf(ClickType.MOUSE_LEFT),
        listOf(CrateInteractionEvent.InteractionType.LEFT_CLICK)),
    SHIFT_LEFT_CLICK("shift_left_click",
        listOf(ClickType.MOUSE_LEFT_SHIFT),
        listOf(CrateInteractionEvent.InteractionType.SHIFT_LEFT_CLICK)),
    ANY_LEFT_CLICK("any_left_click",
        listOf(ClickType.MOUSE_LEFT, ClickType.MOUSE_LEFT_SHIFT),
        listOf(CrateInteractionEvent.InteractionType.LEFT_CLICK, CrateInteractionEvent.InteractionType.SHIFT_LEFT_CLICK)),

    RIGHT_CLICK("right_click", listOf(ClickType.MOUSE_RIGHT),
        listOf(CrateInteractionEvent.InteractionType.RIGHT_CLICK)),
    SHIFT_RIGHT_CLICK("shift_right_click", listOf(ClickType.MOUSE_RIGHT_SHIFT),
        listOf(CrateInteractionEvent.InteractionType.SHIFT_RIGHT_CLICK)),
    ANY_RIGHT_CLICK("any_right_click", listOf(ClickType.MOUSE_RIGHT, ClickType.MOUSE_RIGHT_SHIFT),
        listOf(CrateInteractionEvent.InteractionType.RIGHT_CLICK, CrateInteractionEvent.InteractionType.SHIFT_RIGHT_CLICK)),

    ANY_CLICK("any_click",
        listOf(ClickType.MOUSE_LEFT, ClickType.MOUSE_LEFT_SHIFT, ClickType.MOUSE_RIGHT, ClickType.MOUSE_RIGHT_SHIFT),
        listOf(CrateInteractionEvent.InteractionType.LEFT_CLICK, CrateInteractionEvent.InteractionType.SHIFT_LEFT_CLICK,
            CrateInteractionEvent.InteractionType.RIGHT_CLICK, CrateInteractionEvent.InteractionType.SHIFT_RIGHT_CLICK)
    ),
    ANY_MAIN_CLICK("any_main_click",
        listOf(ClickType.MOUSE_LEFT, ClickType.MOUSE_RIGHT),
        listOf(CrateInteractionEvent.InteractionType.LEFT_CLICK, CrateInteractionEvent.InteractionType.RIGHT_CLICK)),
    ANY_SHIFT_CLICK("any_shift_click",
        listOf(ClickType.MOUSE_LEFT_SHIFT, ClickType.MOUSE_RIGHT_SHIFT),
        listOf(CrateInteractionEvent.InteractionType.SHIFT_LEFT_CLICK, CrateInteractionEvent.InteractionType.SHIFT_RIGHT_CLICK)),

    MIDDLE_CLICK("middle_click",
        listOf(ClickType.MOUSE_MIDDLE), listOf()),
    THROW("throw",
        listOf(ClickType.DROP), listOf(CrateInteractionEvent.InteractionType.DROP)),

    ANY("any", ClickType.entries, CrateInteractionEvent.InteractionType.entries);

    override fun getSerializedName(): String {
        return this.identifier
    }

    companion object {
        fun valueOfAnyCase(name: String): GenericClickType? {
            for (type in entries) {
                if (name.equals(type.identifier, true)) return type
            }
            return null
        }

        fun fromClickType(clickType: ClickType): List<GenericClickType> {
            return entries.filter { it.buttonClicks.contains(clickType) }
        }
    }

    internal class Adapter : JsonSerializer<GenericClickType>, JsonDeserializer<GenericClickType> {
        override fun serialize(src: GenericClickType, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            return JsonPrimitive(src.identifier)
        }

        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): GenericClickType {
            val click = valueOfAnyCase(json.asString)

            if (click == null) {
                Utils.printError("Could not deserialize Click Type '${json.asString}'!")
                return ANY
            }

            return click
        }
    }
}