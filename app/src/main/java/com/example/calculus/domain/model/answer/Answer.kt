package com.example.calculus.domain.model.answer

import com.example.calculus.domain.trap.DiagnosticTrap

sealed interface Answer {
    val value: AnswerValue


    data class Correct(
        override val value: AnswerValue
    ): Answer

    data class Wrong(
        override val value: AnswerValue,
    ): Answer

    data class Trap(
        override val value: AnswerValue,
        val type: DiagnosticTrap
    ): Answer
}