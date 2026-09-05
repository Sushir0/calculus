package com.example.calculus.domain.model

import com.example.calculus.domain.model.answer.Answer
import java.util.UUID

data class MathProblem(
    val id: String = UUID.randomUUID().toString(),
    val prompt: String,
    val correctAnswer: Answer,
    val answerOptions: List<Answer>? = null,
) {
    val isMultipleChoice: Boolean
        get() = answerOptions != null
}