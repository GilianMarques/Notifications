package dev.gmarques.notifications.domain.model

import java.time.DayOfWeek
import java.time.LocalTime

/**
 * Configuração de um aplicativo gerenciado
 */
data class AppConfiguration(
    val packageName: String,
    val listType: ListType,
    val scheduledDays: Set<DayOfWeek> = emptySet(),
    val startTime: LocalTime = LocalTime.of(0, 0),
    val endTime: LocalTime = LocalTime.of(23, 59)
){

    // Extensões e utilitários
    fun isNotificationAllowed(currentDay: DayOfWeek, currentTime: LocalTime): Boolean {
        val isDayAllowed = scheduledDays.contains(currentDay)
        val isTimeInRange = currentTime.isAfter(startTime) && currentTime.isBefore(endTime)

        return when (listType) {
            ListType.BLACKLIST -> !isDayAllowed || !isTimeInRange
            ListType.WHITELIST -> isDayAllowed && isTimeInRange
        }
    }
}