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
        var attempts = 0
        val maxGlobalAttempts = 20

        while (attempts < maxGlobalAttempts) {
            attempts++
            val candidate = buildRandomExpression(config.expressionRange.random())

            if (config.allowNegativeNumbers || candidate.evaluate() >= 0.0) {
                return candidate
            }
        }

        val a = config.valueRange.random().coerceAtLeast(1)
        val b = config.valueRange.random().coerceAtLeast(1)
        return Expression.Binary(Expression.Number(a.toDouble()), Expression.Number(b.toDouble()), Operation.Add)
    }

    private fun buildRandomExpression(length: Int): Expression {
        if (length == 2) {
            val op = generateOperation()
            var a = generateOperand(op)
            var b = generateOperand(op)

            if (!config.allowNegativeNumbers && op == Operation.Subtract) {
                if (a.evaluate() < b.evaluate()) {
                    val temp = a
                    a = b
                    b = temp
                }
            }
            return Expression.Binary(left = a, right = b, operator = op)
        }

        var currentExpression: Expression = Expression.Number(generateNumber().toDouble())

        for (i in 1 until length) {
            val op = generateOperation()
            val nextOperand = generateOperand(op)

            currentExpression = Expression.Binary(
                left = currentExpression,
                right = nextOperand,
                operator = op
            )
        }

        return currentExpression
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