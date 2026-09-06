package com.example.calculus.domain.problemGenerator.arithmetic

import com.example.calculus.domain.model.AnswerOptionConfig
import com.example.calculus.domain.problemGenerator.arithmetic.models.Operation
import com.example.calculus.domain.problemGenerator.GameConfig

data class ArithmeticConfig(
    val operations: Set<Operation>,
    val valueRange: IntRange = 1..10,
    val expressionRange: IntRange = 2..3,
    val allowNegativeNumbers: Boolean = false,
    val answerOptionsConfig: AnswerOptionConfig<ArithmeticAnswerSpecificConfig> =
        AnswerOptionConfig.MultipleChoice(
            numberOfOptions = 4,
            specificConfig = ArithmeticAnswerSpecificConfig()
        )
): GameConfig {
    override val title: String = "Aritmética Básica"

    val validationResult: ArithmeticConfigValidationResult by lazy {
        ArithmeticConfigValidationResult(
            operationsError = when {
                operations.isEmpty() -> ArithmeticConfigError.NoOperationsProvided
                else -> null
            },
            expressionRangeError = when {
                expressionRange.isEmpty() -> ArithmeticConfigError.RangeIsEmpty
                expressionRange.first < 2 -> ArithmeticConfigError.ExpressionTooShort
                else -> null
            },
            valueRangeError = when {
                valueRange.isEmpty() -> ArithmeticConfigError.RangeIsEmpty
                !allowNegativeNumbers && valueRange.first < 0 -> ArithmeticConfigError.NegativeNumbersNotAllowed
                Operation.Divide in operations && valueRange.all { it == 0 } -> ArithmeticConfigError.DivisionRequireNonZero
                else -> null
            },
            answerOptionError = when (val options = answerOptionsConfig) {
                is AnswerOptionConfig.MultipleChoice -> when {
                    options.numberOfOptions < 2 -> ArithmeticConfigError.TooFewOptions
                    options.numberOfOptions > 6 -> ArithmeticConfigError.TooManyOptions
                    options.specificConfig.randomOffsetRange < 1 -> ArithmeticConfigError.OffsetRangeTooSmall
                    (options.specificConfig.randomOffsetRange) < (options.numberOfOptions - 1) -> ArithmeticConfigError.OffsetRangeTooSmall
                    else -> null
                }
                else -> null
            },
            trapError = when (val options = answerOptionsConfig) {
                is AnswerOptionConfig.MultipleChoice -> when {
                    options.specificConfig.trapPercentage > 0 && options.specificConfig.trapsWeights.isEmpty() -> ArithmeticConfigError.TrapsRequireOptions
                    options.specificConfig.trapPercentage !in 0.0..1.0 -> ArithmeticConfigError.InvalidTrapProbability
                    options.specificConfig.trapsWeights.values.any { it < 0.0 } -> ArithmeticConfigError.NegativeTrapWeight
                    else -> null
                }
                else -> null
            }
        )
    }
    val isValid: Boolean get() = validationResult.isValid
}

sealed interface ArithmeticConfigError {
    data object NoOperationsProvided: ArithmeticConfigError
    data object ExpressionTooShort: ArithmeticConfigError
    data object RangeIsEmpty: ArithmeticConfigError 
    data object DivisionRequireNonZero: ArithmeticConfigError
    data object NegativeNumbersNotAllowed: ArithmeticConfigError
    data object TooFewOptions: ArithmeticConfigError
    data object TooManyOptions: ArithmeticConfigError

    data object TrapsRequireOptions: ArithmeticConfigError
    data object InvalidTrapProbability: ArithmeticConfigError
    data object NegativeTrapWeight: ArithmeticConfigError

    data object OffsetRangeTooSmall: ArithmeticConfigError
}

data class ArithmeticConfigValidationResult(
    val operationsError: ArithmeticConfigError? = null,
    val expressionRangeError: ArithmeticConfigError? = null,
    val valueRangeError: ArithmeticConfigError? = null,
    val answerOptionError: ArithmeticConfigError? = null,
    val trapError: ArithmeticConfigError? = null
) {
    val isValid: Boolean = operationsError == null &&
            expressionRangeError == null &&
            valueRangeError == null &&
            answerOptionError == null &&
            trapError == null
}
