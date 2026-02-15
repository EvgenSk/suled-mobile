package com.suled.app.di

import com.suled.app.data.repository.ITournamentRepository
import com.suled.app.data.repository.TournamentRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for repository dependencies.
 * Uses @Binds for interface-to-implementation mapping.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    /**
     * Binds the TournamentRepository implementation to ITournamentRepository interface.
     * This allows ViewModels to depend on the interface rather than concrete implementation,
     * improving testability and following Dependency Inversion Principle.
     */
    @Binds
    @Singleton
    abstract fun bindTournamentRepository(
        implementation: TournamentRepository
    ): ITournamentRepository
}
