package com.example.calculus.domain.game.model

import com.example.calculus.domain.model.ProblemAttempt
import com.example.calculus.domain.problemGenerator.GameConfig
import com.example.calculus.domain.trap.DiagnosticTrap
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
data class GameSummary(
    val id: String? = null,
    val history: List<ProblemAttempt>,
    val config: GameConfig,
    val startedAt: Instant,
    val finishedAt: Instant
){
    val sPersisted: Boolean = id != null

    val totalProblems: Int = history.size
    val correctCount: Int = history.count { it.isCorrect }
    val incorrectCount: Int = history.count { !it.isCorrect }

    val accuracyPercentage: Double = if (totalProblems > 0) {
        (correctCount.toDouble() / totalProblems) * 100.0
    } else 0.0

    val totalPlayTime: Long = finishedAt.minus(startedAt).inWholeMilliseconds

    val averageResponseTimeMillis: Long = if (totalProblems > 0) {
        history.sumOf { it.duration } / totalProblems
    } else 0L

    val averageCorrectTimeMillis: Long = history
        .filter { it.isCorrect }
        .takeIf { it.isNotEmpty() }
        ?.let { list -> list.sumOf { it.duration } / list.size } ?: 0L

    val averageIncorrectTimeMillis: Long = history
        .filter { !it.isCorrect }
        .takeIf { it.isNotEmpty() }
        ?.let { list -> list.sumOf { it.duration } / list.size } ?: 0L

    val slowestAttempt: ProblemAttempt? = history.maxByOrNull { it.duration }

    val fastestAttempt: ProblemAttempt? = history.minByOrNull { it.duration }

    val maxStreak: Int = calculateMaxStreak(history)

    val triggeredTrapsCount: Map<DiagnosticTrap, Int> = history
        .mapNotNull { it.triggeredTrap }
        .groupingBy { it }
        .eachCount()

    companion object {
        private fun calculateMaxStreak(history: List<ProblemAttempt>): Int {
            var currentStreak = 0
            var max = 0
            for (attempt in history) {
                if (attempt.isCorrect) {
                    currentStreak++
                    if (currentStreak > max) max = currentStreak
                } else {
                    currentStreak = 0
                }
            }
            return max
        }
    }
}
