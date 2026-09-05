package com.example.calculus.domain.trap.arithmetic

import com.example.calculus.domain.problemGenerator.arithmetic.ArithmeticConfig
import com.example.calculus.domain.problemGenerator.arithmetic.models.Expression
import com.example.calculus.domain.problemGenerator.arithmetic.models.Operation
import com.example.calculus.domain.trap.DiagnosticTrap
import kotlin.math.abs
import kotlin.random.Random

enum class ArithmeticTrap: DiagnosticTrap {
    SIGN_FLIP,            // Inverte sinal (+x vira -x)
    OFF_BY_TEN,           // Erro de "vai-um" ou dezena (±10)
    OFF_BY_ONE,           // Erro de contagem nos dedos (±1)
    WRONG_OPERATOR,       // Se era 3 * 4, gera a resposta de 3 + 4
    DIGIT_TRANSPOSITION;   // Inverte os dígitos (36 vira 63)

    override val identifier: String = name
}

object SignFlipStrategyArithmetic : ArithmeticTrapStrategy {
    override fun getWrongNumber(
        expression: Expression,
        config: ArithmeticConfig,
    ): Double {
        return -expression.evaluate()
    }

    override fun canApply(
        expression: Expression,
        config: ArithmeticConfig,
    ): Boolean {
        val correctAnswer = expression.evaluate()

        val potencialTrap = getWrongNumber(expression, config)

        if (expression !is Expression.Binary) return false
        if (correctAnswer == 0.0) return false
        if (!config.allowNegativeNumbers && potencialTrap < 0) return false
        return true
    }
}

object OffByTenStrategyArithmetic : ArithmeticTrapStrategy {
    override fun getWrongNumber(
        expression: Expression,
        config: ArithmeticConfig,
    ): Double {
        val value = expression.evaluate()

        val offset = when{
            !config.allowNegativeNumbers && (value - 10 < 0) -> 10
            else -> if(Random.nextBoolean()) 10 else -10
        }
        return value + offset
    }

    override fun canApply(
        expression: Expression,
        config: ArithmeticConfig,
    ): Boolean {
        return expression is Expression.Binary
    }
}

object OffByOneStrategyArithmetic : ArithmeticTrapStrategy {
    override fun getWrongNumber(
        expression: Expression,
        config: ArithmeticConfig,
    ): Double {
        val value = expression.evaluate()
        val offset = when{
            !config.allowNegativeNumbers && (value - 1 < 0) -> 1
            else -> if(Random.nextBoolean()) 1 else -1
        }
        return value + offset
    }

    override fun canApply(
        expression: Expression,
        config: ArithmeticConfig,
    ): Boolean {
        return expression is Expression.Binary
    }
}

object WrongOperatorStrategyArithmetic: ArithmeticTrapStrategy {
    override fun getWrongNumber(
        expression: Expression,
        config: ArithmeticConfig,
    ): Double {
        if (expression !is Expression.Binary) throw IllegalArgumentException("Expression must be a binary expression")

        val alternativeOperator = when(expression.operator) {
            Operation.Add -> Operation.Multiply
            Operation.Subtract -> Operation.Add
            Operation.Multiply -> Operation.Add
            Operation.Divide -> Operation.Subtract
        }

        val alternativeExpression = Expression.Binary(
            left = expression.left,
            right = expression.right,
            operator = alternativeOperator
        )
        return alternativeExpression.evaluate()
    }

    override fun canApply(
        expression: Expression,
        config: ArithmeticConfig,
    ): Boolean {
        // Validação prematura para evitar exceções
        if (expression !is Expression.Binary) return false

        val correctAnswer = expression.evaluate()
        val potencialTrap = getWrongNumber(expression, config)

        if (potencialTrap == correctAnswer) return false
        if (!config.allowNegativeNumbers && potencialTrap < 0) return false
        return true
    }
}

object DigitTranspositionStrategyArithmetic : ArithmeticTrapStrategy {

    override fun getWrongNumber(
        expression: Expression,
        config: ArithmeticConfig,
    ): Double {
        val value = expression.evaluate()
        val isNegative = value < 0
        val absValueInt = abs(value.toInt())

        val transposed = absValueInt.toString().reversed().toDouble()
        return if (isNegative) -transposed else transposed
    }

    override fun canApply(
        expression: Expression,
        config: ArithmeticConfig,
    ): Boolean {
        if (expression is Expression.Number) return false

        val value = expression.evaluate()

        if (value % 1.0 != 0.0) return false

        val absValue = abs(value.toInt())

        if (absValue !in 10..99) return false
        if (absValue % 10 == 0) return false

        val transposed = getWrongNumber(expression, config)
        if (transposed == value) return false

        if (!config.allowNegativeNumbers && transposed < 0) return false
        return true
    }
}