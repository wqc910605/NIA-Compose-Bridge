package com.nia.compose.bridge.data.di

import com.nia.compose.bridge.data.repository.DefaultUserSettingsRepository
import com.nia.compose.bridge.data.repository.DemoItemRepository
import com.nia.compose.bridge.data.repository.OfflineFirstDemoItemRepository
import com.nia.compose.bridge.data.repository.UserSettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindDemoItemRepository(impl: OfflineFirstDemoItemRepository): DemoItemRepository

    @Binds
    @Singleton
    abstract fun bindUserSettingsRepository(impl: DefaultUserSettingsRepository): UserSettingsRepository
}
