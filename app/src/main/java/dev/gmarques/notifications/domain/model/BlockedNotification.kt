package dev.gmarques.notifications.domain.model

import android.graphics.drawable.Drawable
import java.util.UUID

/**
 * Representação de uma notificação bloqueada
 */
data class BlockedNotification(
    val id: String = UUID.randomUUID().toString(),
    val packageName: String,
    val appName: String,
    val appIcon: Drawable? = null,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)