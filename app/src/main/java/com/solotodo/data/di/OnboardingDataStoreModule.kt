package com.solotodo.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.solotodo.data.onboarding.AwakeningDraftStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AwakeningDraftPrefs

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class WelcomeBannerPrefs

/**
 * Preferences DataStore instances. One file per concern, each injected under
 * its own qualifier so tests can swap in temp-file-backed DataStores.
 */
@Module
@InstallIn(SingletonComponent::class)
object OnboardingDataStoreModule {

    @Provides
    @Singleton
    @AwakeningDraftPrefs
    fun provideAwakeningDraftPrefs(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        produceFile = { context.preferencesDataStoreFile(AwakeningDraftStore.FILE_NAME) },
    )

    @Provides
    @Singleton
    @WelcomeBannerPrefs
    fun provideWelcomeBannerPrefs(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        produceFile = { context.preferencesDataStoreFile("welcome_banner") },
    )
}
