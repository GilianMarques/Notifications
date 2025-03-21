package dev.gmarques.notifications.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import dev.gmarques.notifications.domain.service.MyNotificationListenerService
import java.lang.reflect.Type
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * TypeAdapter para converter LocalTime para JSON e vice-versa
 */
class LocalTimeTypeAdapter : JsonSerializer<LocalTime>, JsonDeserializer<LocalTime> {

    private val formatter = DateTimeFormatter.ISO_LOCAL_TIME

    override fun serialize(
        src: LocalTime,
        typeOfSrc: Type,
        context: JsonSerializationContext
    ): JsonElement {
        return JsonPrimitive(formatter.format(src))
    }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): LocalTime {
        return LocalTime.parse(json.asString, formatter)
    }
}


/**
 * Verifica se o serviço de acesso a notificações está habilitado
 */
fun isNotificationListenerEnabled(context: Context): Boolean {
    val packageName = context.packageName
    // Substitua YourNotificationListener pelo nome da sua implementação de NotificationListenerService
    val serviceString = "$packageName/${ MyNotificationListenerService::class.java.name}"
    val enabledListeners = android.provider.Settings.Secure.getString(
        context.contentResolver,
        "enabled_notification_listeners"
    )

    // Para debug: Log.d("NotificationCheck", "Current listeners: $enabledListeners")
    // Para debug: Log.d("NotificationCheck", "Seeking service: $serviceString")

    return enabledListeners?.contains(serviceString) ?: false
}



/**
 * Extensão para converter StatusBarNotification em uma string legível
 */
fun StatusBarNotification.toReadableString(): String {
    val extras = notification.extras
    val title = extras.getString("android.title") ?: ""
    val text = extras.getString("android.text") ?: ""

    return "$packageName: $title - $text"
}