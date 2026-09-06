package com.example.calculus.domain

import com.example.calculus.domain.problemGenerator.arithmetic.ArithmeticConfig
import com.example.calculus.domain.problemGenerator.arithmetic.models.Expression
import com.example.calculus.domain.problemGenerator.arithmetic.models.Operation
import com.example.calculus.domain.trap.arithmetic.OffByOneStrategyArithmetic
import org.junit.Assert.*
import org.junit.Test

class OffByOneStrategyTest {

    private val defaultConfig = ArithmeticConfig(
        operations = setOf(Operation.Add),
        allowNegativeNumbers = true
    )

    @Test
    fun `getWrongNumber should return evaluation plus or minus one`() {
        val expression = Expression.Number(10.0)
        
        repeat(20) {
            val wrongNumber = OffByOneStrategyArithmetic.getWrongNumber(expression, defaultConfig)
            assertTrue(wrongNumber == 11.0 || wrongNumber == 9.0)
        }
    }

    @Test
    fun `canApply should return false for non-binary expressions`() {
        val numberExpression = Expression.Number(10.0)
        assertFalse(OffByOneStrategyArithmetic.canApply(numberExpression, defaultConfig))

        val groupExpression = Expression.Group(
            expression = Expression.Binary(
                left = Expression.Number(5.0),
                right = Expression.Number(2.0),
                operator = Operation.Add
            )
        )
        assertFalse(OffByOneStrategyArithmetic.canApply(groupExpression, defaultConfig))
    }

    @Test
    fun `canApply should return true for binary expressions even when result is zero and negatives are disallowed`() {
        val config = defaultConfig.copy(allowNegativeNumbers = false)
        val expression = Expression.Binary(Expression.Number(0.0), Expression.Number(0.0), Operation.Add)

        assertTrue(OffByOneStrategyArithmetic.canApply(expression, config))
    }

    @Test
    fun `getWrongNumber should always force positive offset when negatives are disallowed and value is zero`() {
        val config = defaultConfig.copy(allowNegativeNumbers = false)
        // 0 + 0 = 0. Como não permite negativos, a trap DEVE ser obrigatoriamente 0 + 1 = 1
        val expression = Expression.Binary(Expression.Number(0.0), Expression.Number(0.0), Operation.Add)

        repeat(100) {
            val wrongNumber = OffByOneStrategyArithmetic.getWrongNumber(expression, config)
            assertEquals("Deveria forçar +1 e nunca gerar -1", 1.0, wrongNumber, 0.0)
        }
    }

    @Test
    fun `canApply should return true when negative is not allowed but result is large enough`() {
        val config = defaultConfig.copy(allowNegativeNumbers = false)
        // Result is 5. Traps: 6 or 4. Both > 0.
        val expression = Expression.Binary(
            left = Expression.Number(3.0),
            right = Expression.Number(2.0),
            operator = Operation.Add
        )
        
        repeat(20) {
            assertTrue(OffByOneStrategyArithmetic.canApply(expression, config))
        }
    }

    @Test
    fun `canApply should return true when negative is allowed`() {
        val config = defaultConfig.copy(allowNegativeNumbers = true)
        val expression = Expression.Binary(
            left = Expression.Number(0.0),
            right = Expression.Number(0.0),
            operator = Operation.Add
        )
        // Result is 0. Traps: 1 or -1. Both ok since allowNegativeNumbers = true.
        repeat(20) {
            assertTrue(OffByOneStrategyArithmetic.canApply(expression, config))
        }
    }
}
