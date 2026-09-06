package com.example.calculus.domain

import com.example.calculus.domain.problemGenerator.arithmetic.ArithmeticConfig
import com.example.calculus.domain.problemGenerator.arithmetic.models.Expression
import com.example.calculus.domain.problemGenerator.arithmetic.models.Operation
import com.example.calculus.domain.trap.arithmetic.OffByTenStrategyArithmetic
import org.junit.Assert.*
import org.junit.Test

class OffByTenStrategyTest {

    private val defaultConfig = ArithmeticConfig(
        operations = setOf(Operation.Add),
        allowNegativeNumbers = true
    )

    @Test
    fun `getWrongNumber should return evaluation plus or minus ten`() {
        val expression = Expression.Number(25.0)
        
        // As it's random, we test multiple times to ensure we catch both or at least valid ones
        repeat(20) {
            val wrongNumber = OffByTenStrategyArithmetic.getWrongNumber(expression, defaultConfig)
            assertTrue(wrongNumber == 35.0 || wrongNumber == 15.0)
        }
    }

    @Test
    fun `canApply should return false for non-binary expressions`() {
        val numberExpression = Expression.Number(10.0)
        assertFalse(OffByTenStrategyArithmetic.canApply(numberExpression, defaultConfig))

        val groupExpression = Expression.Group(
            expression = Expression.Binary(
                left = Expression.Number(5.0),
                right = Expression.Number(2.0),
                operator = Operation.Add
            )
        )
        assertFalse(OffByTenStrategyArithmetic.canApply(groupExpression, defaultConfig))
    }

    @Test
    fun `canApply should return true for binary expressions even when result is less than 10 and negatives are disallowed`() {
        val config = defaultConfig.copy(allowNegativeNumbers = false)
        val expression = Expression.Binary(Expression.Number(2.0), Expression.Number(3.0), Operation.Add) // 2 + 3 = 5

        assertTrue(OffByTenStrategyArithmetic.canApply(expression, config))
    }

    @Test
    fun `getWrongNumber should always force positive offset when negatives are disallowed and value is less than 10`() {
        val config = defaultConfig.copy(allowNegativeNumbers = false)
        // 2 + 3 = 5. Como 5 - 10 daria -5 (proibido), a trap DEVE ser obrigatoriamente 5 + 10 = 15
        val expression = Expression.Binary(Expression.Number(2.0), Expression.Number(3.0), Operation.Add)

        repeat(100) {
            val wrongNumber = OffByTenStrategyArithmetic.getWrongNumber(expression, config)
            assertEquals("Deveria forçar +10 e nunca gerar -5", 15.0, wrongNumber, 0.0)
        }
    }

    @Test
    fun `canApply should return true when negative is not allowed but result is large enough`() {
        val config = defaultConfig.copy(allowNegativeNumbers = false)
        // Result is 20. Traps: 30 or 10. Both > 0.
        val expression = Expression.Binary(
            left = Expression.Number(15.0),
            right = Expression.Number(5.0),
            operator = Operation.Add
        )
        
        repeat(20) {
            assertTrue(OffByTenStrategyArithmetic.canApply(expression, config))
        }
    }

    @Test
    fun `canApply should return true when negative is allowed`() {
        val config = defaultConfig.copy(allowNegativeNumbers = true)
        val expression = Expression.Binary(
            left = Expression.Number(2.0),
            right = Expression.Number(3.0),
            operator = Operation.Add
        )
        // Result is 5. Traps: 15 or -5. Both ok since allowNegativeNumbers = true.
        repeat(20) {
            assertTrue(OffByTenStrategyArithmetic.canApply(expression, config))
        }
    }
}
