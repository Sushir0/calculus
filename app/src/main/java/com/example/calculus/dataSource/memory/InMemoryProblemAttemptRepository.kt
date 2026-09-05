package com.example.calculus.dataSource.memory

import com.example.calculus.domain.repository.ProblemAttemptRepository
import com.example.calculus.domain.model.ProblemAttempt
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.util.UUID
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class InMemoryProblemAttemptRepository : ProblemAttemptRepository {
    private val _attempts = MutableStateFlow<Map<String, ProblemAttempt>>(emptyMap())

    override suspend fun save(problemAttempt: ProblemAttempt): Result<Unit> {
        val id = problemAttempt.id ?: UUID.randomUUID().toString()
        val attemptWithId = problemAttempt.copy(id = id)

        _attempts.update { it + (id to attemptWithId) }
        return Result.success(Unit)
    }

    override fun get(id: String): Flow<ProblemAttempt?> {
        return _attempts.map { it[id] }
    }

    override fun getAll(): Flow<List<ProblemAttempt>> {
        return _attempts.map { it.values.toList() }
    }
}
