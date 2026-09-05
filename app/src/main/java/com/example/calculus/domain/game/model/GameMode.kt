package com.example.calculus.domain.game.model

sealed interface GameMode {
    data class FixedRounds(val rounds: Int): GameMode
    data class Timed(val durationSeconds: Int): GameMode
    data object Endless: GameMode
}