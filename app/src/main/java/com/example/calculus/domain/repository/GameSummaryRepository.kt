package com.example.calculus.domain.repository

import com.example.calculus.domain.game.model.GameSummary
import com.example.calculus.domain.problemGenerator.GameConfig
import com.example.calculus.domain.problemGenerator.arithmetic.ArithmeticConfig
import kotlinx.coroutines.flow.Flow
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
interface GameSummaryRepository {
    suspend fun save(gameSummary: GameSummary): Result<Unit>
    fun get(id: String): Flow<GameSummary?>
    fun getAll(): Flow<List<GameSummary>>
    fun getByConfig(config: GameConfig): Flow<List<GameSummary>>

}