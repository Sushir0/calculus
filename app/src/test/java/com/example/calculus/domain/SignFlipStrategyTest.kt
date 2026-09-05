package com.example.calculus.domain

import com.example.calculus.domain.problemGenerator.arithmetic.ArithmeticConfig
import com.example.calculus.domain.problemGenerator.arithmetic.models.Expression
import com.example.calculus.domain.problemGenerator.arithmetic.models.Operation
import com.example.calculus.domain.trap.arithmetic.SignFlipStrategyArithmetic
import org.junit.Assert.*
import org.junit.Test

class SignFlipStrategyTest {

    private val defaultConfig = ArithmeticConfig(
        operations = setOf(Operation.Add),
        allowNegativeNumbers = true
    )

    @Test
    fun `getWrongNumber should return negative of evaluation`() {
        val expression = Expression.Number(10.0)
        val wrongNumber = SignFlipStrategyArithmetic.getWrongNumber(expression, defaultConfig)
        assertEquals(-10.0, wrongNumber, 0.0)

        val negativeExpression = Expression.Number(-5.0)
        val wrongNumberNegative = SignFlipStrategyArithmetic.getWrongNumber(negativeExpression, defaultConfig)
        assertEquals(5.0, wrongNumberNegative, 0.0)
    }

    @Test
    fun `canApply should return false for non-binary expressions`() {
        val numberExpression = Expression.Number(10.0)
        assertFalse(SignFlipStrategyArithmetic.canApply(numberExpression, defaultConfig))

        val groupExpression = Expression.Group(
            expression = Expression.Binary(
                left = Expression.Number(5.0),
                right = Expression.Number(2.0),
                operator = Operation.Add
            )
        )
        // Atualmente a implementação exige estritamente Expression.Binary no topo
        assertFalse(SignFlipStrategyArithmetic.canApply(groupExpression, defaultConfig))
    }

    @Test
    fun `canApply should return false for zero result`() {
        val expression = Expression.Binary(
            left = Expression.Number(5.0),
            right = Expression.Number(5.0),
            operator = Operation.Subtract
        )
        assertEquals(0.0, expression.evaluate(), 0.0)
        assertFalse(SignFlipStrategyArithmetic.canApply(expression, defaultConfig))
    }

    @Test
    fun `canApply should return false when negative is not allowed and result is positive`() {
        val config = defaultConfig.copy(allowNegativeNumbers = false)
        val expression = Expression.Binary(
            left = Expression.Number(5.0),
            right = Expression.Number(3.0),
            operator = Operation.Add
        )
        // 5 + 3 = 8. Trap = -8. Negative not allowed -> should return false
        assertFalse(SignFlipStrategyArithmetic.canApply(expression, config))
    }

    @Test
    fun `canApply should return true when negative is not allowed and result is negative`() {
        val config = defaultConfig.copy(allowNegativeNumbers = false)
        val expression = Expression.Binary(
            left = Expression.Number(3.0),
            right = Expression.Number(5.0),
            operator = Operation.Subtract
        )
        // 3 - 5 = -2. Trap = 2. Positive allowed -> should return true
        assertTrue(SignFlipStrategyArithmetic.canApply(expression, config))
    }

    @Test
    fun `canApply should return true when negative is allowed and result is positive`() {
        val config = defaultConfig.copy(allowNegativeNumbers = true)
        val expression = Expression.Binary(
            left = Expression.Number(5.0),
            right = Expression.Number(3.0),
            operator = Operation.Add
        )
        // 5 + 3 = 8. Trap = -8. Negative allowed -> should return true
        assertTrue(SignFlipStrategyArithmetic.canApply(expression, config))
    }

    @Test
    fun `canApply should return true for standard binary expression with non-zero result`() {
        val expression = Expression.Binary(
            left = Expression.Number(10.0),
            right = Expression.Number(2.0),
            operator = Operation.Multiply
        )
        assertTrue(SignFlipStrategyArithmetic.canApply(expression, defaultConfig))
    }

    @Test
    fun `canApply and getWrongNumber should work for nested binary expressions`() {
        // (2 + 3) * 4 = 20
        val nestedExpression = Expression.Binary(
            left = Expression.Binary(
                left = Expression.Number(2.0),
                operator = Operation.Add,
                right = Expression.Number(3.0)
            ),
            operator = Operation.Multiply,
            right = Expression.Number(4.0)
        )

        assertTrue(SignFlipStrategyArithmetic.canApply(nestedExpression, defaultConfig))
        assertEquals(-20.0, SignFlipStrategyArithmetic.getWrongNumber(nestedExpression, defaultConfig), 0.0)
    }

    @Test
    fun `canApply should return false for negative zero result`() {
        val expression = Expression.Binary(
            left = Expression.Number(0.0),
            right = Expression.Number(-5.0),
            operator = Operation.Multiply
        )
        // 0.0 * -5.0 pode gerar -0.0
        assertFalse(SignFlipStrategyArithmetic.canApply(expression, defaultConfig))
    }

    @Test
    fun `getWrongNumber should never equal the correct answer when canApply is true`() {
        val expression = Expression.Binary(
            left = Expression.Number(7.0),
            right = Expression.Number(3.0),
            operator = Operation.Subtract
        )

        if (SignFlipStrategyArithmetic.canApply(expression, defaultConfig)) {
            val correct = expression.evaluate()
            val wrong = SignFlipStrategyArithmetic.getWrongNumber(expression, defaultConfig)
            assertNotEquals(correct, wrong, 0.0)
        }
    }
}
