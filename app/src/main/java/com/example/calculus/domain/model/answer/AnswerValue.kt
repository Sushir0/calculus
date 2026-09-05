package com.example.calculus.domain.model.answer

sealed interface AnswerValue {
    data class ArithmeticValue(val value: Double) : AnswerValue
}