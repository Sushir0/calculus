package com.example.calculus.domain.repository

import com.example.calculus.domain.model.ProblemAttempt
import kotlinx.coroutines.flow.Flow
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
interface ProblemAttemptRepository {
    suspend fun save(problemAttempt: ProblemAttempt): Result<Unit>

    fun get(id: String): Flow<ProblemAttempt?>
    fun getAll(): Flow<List<ProblemAttempt>>
// Todo    fun getByOperation(): Flow<List<ProblemAttempt>>

}