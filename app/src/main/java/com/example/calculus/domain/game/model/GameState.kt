package com.example.calculus.domain.game.model

import com.example.calculus.domain.model.MathProblem
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

sealed interface GameState {
    data object Idle: GameState

    @OptIn(ExperimentalTime::class)
    data class Playing(
        val currentProblem: MathProblem,
        val currentRound: Int,
        val streak: Int,
        val initialTime: Instant,
        val gameMode: GameMode
    ): GameState

    data class Finished(val summary: GameSummary): GameState
}