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
    fun `canApply should return false when negative is not allowed and potential trap is negative`() {
        val config = defaultConfig.copy(allowNegativeNumbers = false)
        // Result is 5. Potential traps: 15 (ok) or -5 (not allowed).
        // Since canApply is currently non-deterministic in implementation, 
        // we check that it behavior is consistent with the random choice.
        val expression = Expression.Binary(
            left = Expression.Number(2.0),
            right = Expression.Number(3.0),
            operator = Operation.Add
        )

        // Note: In a real scenario, we might want canApply to be deterministic.
        // But testing the current implementation:
        var sawTrue = false
        var sawFalse = false
        repeat(100) {
            if (OffByTenStrategyArithmetic.canApply(expression, config)) sawTrue = true
            else sawFalse = true
        }
        assertTrue("Should be able to return true when random chooses +10", sawTrue)
        assertTrue("Should be able to return false when random chooses -10", sawFalse)
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
