package com.example.calculus.dataSource.memory

import com.example.calculus.domain.repository.GameSummaryRepository
import com.example.calculus.domain.game.model.GameSummary
import com.example.calculus.domain.problemGenerator.GameConfig
import com.example.calculus.domain.problemGenerator.arithmetic.ArithmeticConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.util.UUID
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class InMemoryGameSummaryRepository : GameSummaryRepository {
    private val _summaries = MutableStateFlow<Map<String, GameSummary>>(emptyMap())

    override suspend fun save(gameSummary: GameSummary): Result<Unit> {
        val id = gameSummary.id ?: UUID.randomUUID().toString()
        val summaryWithId = gameSummary.copy(id = id)

        _summaries.update { it + (id to summaryWithId) }
        return Result.success(Unit)
    }

    override fun get(id: String): Flow<GameSummary?> {
        return _summaries.map { it[id] }
    }

    override fun getAll(): Flow<List<GameSummary>> {
        return _summaries.map { it.values.toList() }
    }

    override fun getByConfig(config: GameConfig): Flow<List<GameSummary>> {
        return _summaries.map { it.values.filter { summary -> summary.config == config } }
    }
}
