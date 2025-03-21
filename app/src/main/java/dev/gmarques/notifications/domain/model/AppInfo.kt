package dev.gmarques.notifications.domain.model

import android.graphics.drawable.Drawable

/**
 * Representação de um aplicativo instalado no dispositivo
 */
data class AppInfo(
    val packageName: String,
    val appName: String,
    val appIcon: Drawable,
    val isManaged: Boolean = false
)