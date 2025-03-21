package dev.gmarques.notifications.utils

import android.content.Context
import android.graphics.drawable.Drawable
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

/**
 * TypeAdapter para converter Drawable para JSON e vice-versa
 */
class DrawableTypeAdapter(private val context: Context) : JsonSerializer<Drawable>,
    JsonDeserializer<Drawable> {

    override fun serialize(
        src: Drawable,
        typeOfSrc: Type,
        context: JsonSerializationContext
    ): JsonElement {
        // Armazena apenas a referência do ícone pelo packageName
        return JsonPrimitive("ICON_REFERENCE")
    }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Drawable {
        // Retorna um ícone genérico, o ícone real será carregado em tempo de execução
        return this.context.getDrawable(android.R.drawable.ic_menu_info_details)!!
    }
}
