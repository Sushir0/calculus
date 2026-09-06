package com.example.calculus.domain.problemGenerator.arithmetic

import com.example.calculus.domain.model.AnswerOptionConfig
import com.example.calculus.domain.problemGenerator.arithmetic.models.Expression
import com.example.calculus.domain.model.answer.Answer
import com.example.calculus.domain.model.answer.AnswerValue
import com.example.calculus.domain.trap.arithmetic.DigitTranspositionStrategyArithmetic
import com.example.calculus.domain.trap.arithmetic.ArithmeticTrap
import com.example.calculus.domain.trap.arithmetic.OffByOneStrategyArithmetic
import com.example.calculus.domain.trap.arithmetic.OffByTenStrategyArithmetic
import com.example.calculus.domain.trap.arithmetic.SignFlipStrategyArithmetic
import com.example.calculus.domain.trap.arithmetic.ArithmeticTrapStrategy
import com.example.calculus.domain.trap.arithmetic.WrongOperatorStrategyArithmetic
import kotlin.math.abs
import kotlin.random.Random

class ArithmeticAnswerOrganizer(
    private val avaliableStrategies: Map<ArithmeticTrap, ArithmeticTrapStrategy> = mapOf(
        ArithmeticTrap.SIGN_FLIP to SignFlipStrategyArithmetic,
        ArithmeticTrap.OFF_BY_TEN to OffByTenStrategyArithmetic,
        ArithmeticTrap.OFF_BY_ONE to OffByOneStrategyArithmetic,
        ArithmeticTrap.WRONG_OPERATOR to WrongOperatorStrategyArithmetic,
        ArithmeticTrap.DIGIT_TRANSPOSITION to DigitTranspositionStrategyArithmetic
    )
) {
    fun buildOptions(
        expression: Expression,
        config: ArithmeticConfig,
        multipleChoice: AnswerOptionConfig.MultipleChoice<ArithmeticAnswerSpecificConfig>
    ): List<Answer> {
        val options = mutableSetOf<Answer>(Answer.Correct(AnswerValue.ArithmeticValue(expression.evaluate())))

        val selectedStrategy = pickTrapStrategy(
            probability = multipleChoice.specificConfig.trapPercentage,
            trapsWeights = multipleChoice.specificConfig.trapsWeights,
            expression = expression,
            config = config
        )

        selectedStrategy?.let {
            val trapValue = it.second.getWrongNumber(expression, config)
            if (abs(trapValue - expression.evaluate()) < 0.0001) return@let
            options.add(Answer.Trap(value = AnswerValue.ArithmeticValue(trapValue), type = it.first) )
        }

        fillWithRandomOptions(
            existingOptions = options,
            targetSize = multipleChoice.numberOfOptions,
            correctAnswer = expression.evaluate(),
            allowNegativeNumbers = config.allowNegativeNumbers,
            offsetRange = multipleChoice.specificConfig.randomOffsetRange
        )

        return options.toList().shuffled()
    }

    private fun pickTrapStrategy(
        probability: Double,
        trapsWeights: Map<ArithmeticTrap, Double>,
        expression: Expression,
        config: ArithmeticConfig,
    ): Pair<ArithmeticTrap, ArithmeticTrapStrategy>? {
        val shouldApplyTrap = Random.nextDouble() < probability
        if (!shouldApplyTrap) return null

        val applicableStrategiesWeight = trapsWeights
            .mapNotNull { (trap, weight) ->
                val trapStrategy = avaliableStrategies[trap]
                if (trapStrategy != null) Triple(trap, trapStrategy, weight) else null
            }
            .filter { (_, strategy, weight) -> weight > 0.0 && strategy.canApply(expression, config) }

        if (applicableStrategiesWeight.isEmpty()) return null

        val weightSum = applicableStrategiesWeight.sumOf { (_, _,  weight) -> weight }
        var randomThreshold = Random.nextDouble(weightSum)

        val selected = applicableStrategiesWeight.find { (_, _,  weight) ->
            randomThreshold -= weight
            randomThreshold <= 0
        } ?: applicableStrategiesWeight.last()

        return selected.first to selected.second
    }

    private fun fillWithRandomOptions(
        existingOptions: MutableSet<Answer>,
        targetSize: Int,
        correctAnswer: Double,
        allowNegativeNumbers: Boolean,
        offsetRange: Int
    ) {
        var attempts = 0
        val maxAttempts = 100 // Segurança contra loop infinito se o range for muito pequeno

        while (existingOptions.size < targetSize && attempts < maxAttempts) {
            attempts++
            val offset = Random.nextInt(-offsetRange, offsetRange + 1)
            if (offset == 0) continue


            val candidateNumber = correctAnswer + offset
            if (!allowNegativeNumbers && candidateNumber < 0) continue

            val alreadyExists = existingOptions.any { abs((it.value as AnswerValue.ArithmeticValue).value - AnswerValue.ArithmeticValue(candidateNumber).value) < 0.0001 }
            if (alreadyExists) continue

            val candidate = Answer.Wrong(AnswerValue.ArithmeticValue(candidateNumber))

            existingOptions.add(candidate)
        }
    }
}