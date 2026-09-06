package com.example.calculus.di

import com.example.calculus.dataSource.memory.InMemoryGameSummaryRepository
import com.example.calculus.dataSource.memory.InMemoryProblemAttemptRepository
import com.example.calculus.domain.repository.GameSummaryRepository
import com.example.calculus.domain.repository.ProblemAttemptRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule{

    @Binds
    @Singleton
    abstract fun bindProblemAttemptRepository(
        impl: InMemoryProblemAttemptRepository
    ): ProblemAttemptRepository

    @Binds
    @Singleton
    abstract fun bindGameSummaryRepository(
        impl: InMemoryGameSummaryRepository
    ): GameSummaryRepository
}