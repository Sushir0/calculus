package com.example.calculus.domain.model

import com.example.calculus.domain.model.answer.Answer
import com.example.calculus.domain.trap.DiagnosticTrap
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
data class ProblemAttempt(
    val id: String? = null,
    val problem: MathProblem,
    val selectedAnswer: Answer,
    val startedAt: Instant,
    val finishedAt: Instant
){
    val isPersisted: Boolean = id != null

    val duration: Long = finishedAt.minus(startedAt).inWholeMilliseconds
    val isCorrect: Boolean = selectedAnswer is Answer.Correct
    val triggeredTrap: DiagnosticTrap? = (selectedAnswer as? Answer.Trap)?.type
}