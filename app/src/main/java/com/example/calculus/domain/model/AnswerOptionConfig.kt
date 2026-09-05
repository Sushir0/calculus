package com.example.calculus.domain.model

sealed interface AnswerOptionConfig<out T> {

    data class MultipleChoice<T>(
        val numberOfOptions: Int = 4,
        val specificConfig: T
    ): AnswerOptionConfig<T>

    data object Text: AnswerOptionConfig<Nothing>

// todo    data object TrueFalse: AnswerOption
// todo    data object MultipleChoiceOperator: AnswerOption
}