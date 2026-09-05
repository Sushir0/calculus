package com.example.calculus.domain.problemGenerator.arithmetic

import com.example.calculus.domain.model.AnswerOptionConfig
import com.example.calculus.domain.problemGenerator.arithmetic.models.Expression
import com.example.calculus.domain.model.MathProblem
import com.example.calculus.domain.problemGenerator.arithmetic.models.Operation
import com.example.calculus.domain.problemGenerator.ProblemGenerator
import com.example.calculus.domain.exceptions.MathEvaluationException
import com.example.calculus.domain.model.answer.Answer
import com.example.calculus.domain.model.answer.AnswerValue

class ArithmeticProblemGenerator(
    override val config: ArithmeticConfig,
    val arithmeticAnswerOrganizer: ArithmeticAnswerOrganizer = ArithmeticAnswerOrganizer()
): ProblemGenerator {

    init {
        require(config.isValid) {
            "Configuração inválida: ${config.validationResult}"
        }
    }

    override fun generateProblem(): MathProblem {
        val expression = generateExpression()
        return MathProblem(
            prompt = expression.toDisplayString(),
            correctAnswer = Answer.Correct(AnswerValue.ArithmeticValue(expression.evaluate())),
            answerOptions = generateAnswerOptions(expression)
        )
    }

    private fun generateAnswerOptions(expression: Expression): List<Answer>? {
        return when(config.answerOptionsConfig) {
            is AnswerOptionConfig.MultipleChoice -> {
                arithmeticAnswerOrganizer.buildOptions(expression, config, config.answerOptionsConfig)
            }
            is AnswerOptionConfig.Text -> {
                null
            }
        }
    }

    private fun generateExpression(): Expression {
        val expressionLength = config.expressionRange.random()
        var expression: Expression = Expression.Number(generateNumber().toDouble())

        repeat(expressionLength - 1) {
            val operation = generateOperation()
            val nextNumber = generateOperand(operation)
            expression = Expression.Binary(
                left = expression,
                right = nextNumber,
                operator = operation
            )
        }
        return expression
    }

    private fun generateOperand(operation: Operation): Expression.Number {
        var value = generateNumber()

        // Evita divisão por 0
        if (operation == Operation.Divide && value == 0) {
            value = generateSafeNonZeroNumber()
        }
        
        return Expression.Number(value.toDouble())
    }

    private fun generateSafeNonZeroNumber(): Int {
        var index = 0
        var value = generateNumber()
        while (value == 0) {
            value = generateNumber()
            index++
            if (index > 100) {
                throw MathEvaluationException.DivisionByZeroException(config)
            }
        }
        return value
    }

    private fun generateOperation(): Operation = config.operations.random()

    private fun generateNumber(): Int = config.valueRange.random()
}