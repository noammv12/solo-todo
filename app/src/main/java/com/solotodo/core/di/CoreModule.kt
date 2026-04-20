package com.solotodo.core.di

import com.solotodo.core.haptics.AndroidVibratorWrapper
import com.solotodo.core.haptics.VibratorWrapper
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CoreModule {

    @Binds
    @Singleton
    abstract fun bindVibratorWrapper(impl: AndroidVibratorWrapper): VibratorWrapper
}
