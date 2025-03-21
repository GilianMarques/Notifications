package dev.gmarques.notifications.domain.model

/**
 * Define o tipo de lista usada para gerenciar notificações
 */
enum class ListType {
    BLACKLIST, // Horários em que o app NÃO pode mostrar notificações
    WHITELIST  // Horários em que o app PODE mostrar notificações
}