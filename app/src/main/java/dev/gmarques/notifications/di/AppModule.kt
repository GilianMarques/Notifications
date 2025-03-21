package dev.gmarques.notifications.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.gmarques.notifications.data.preferences.AppPreferencesManager
import dev.gmarques.notifications.data.repository.AppRepository
import dev.gmarques.notifications.domain.usecase.NotificationFilterUseCase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppPreferencesManager(@ApplicationContext context: Context): AppPreferencesManager {
        return AppPreferencesManager(context)
    }

    @Provides
    @Singleton
    fun provideAppRepository(
        @ApplicationContext context: Context,
        preferencesManager: AppPreferencesManager
    ): AppRepository {
        return AppRepository(context, preferencesManager)
    }

    @Provides
    @Singleton
    fun provideNotificationFilterUseCase(
        @ApplicationContext context: Context,
        appRepository: AppRepository
    ): NotificationFilterUseCase {
        return NotificationFilterUseCase(context, appRepository)
    }
}