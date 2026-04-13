package com.example.oriontek_technical_interview.core.di

import com.example.oriontek_technical_interview.feature.clients.data.local.ClientLocalDataSource
import com.example.oriontek_technical_interview.feature.clients.data.repository.InMemoryClientRepository
import com.example.oriontek_technical_interview.feature.clients.domain.repository.ClientRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideClientLocalDataSource(): ClientLocalDataSource {
        return InMemoryClientRepository()
    }

    @Provides
    @Singleton
    fun provideClientRepository(
        localDataSource: ClientLocalDataSource
    ): ClientRepository {
        return ClientRepositoryImpl(localDataSource)
    }
}
