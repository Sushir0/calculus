package com.example.calculus.domain

import com.example.calculus.domain.problemGenerator.arithmetic.ArithmeticConfig
import com.example.calculus.domain.problemGenerator.arithmetic.models.Expression
import com.example.calculus.domain.problemGenerator.arithmetic.models.Operation
import com.example.calculus.domain.trap.arithmetic.DigitTranspositionStrategyArithmetic
import org.junit.Assert.*
import org.junit.Test

class DigitTranspositionStrategyTest {

    private val config = ArithmeticConfig(
        operations = setOf(Operation.Add),
        allowNegativeNumbers = true
    )

    @Test
    fun `should transpose standard 2-digit numbers`() {
        // 12 + 14 = 26 -> Trap deve ser 62
        val expr = Expression.Binary(Expression.Number(12.0), operator = Operation.Add, right = Expression.Number(14.0))
        assertTrue(DigitTranspositionStrategyArithmetic.canApply(expr, config))
        assertEquals(62.0, DigitTranspositionStrategyArithmetic.getWrongNumber(expr, config), 0.0)
    }

    @Test
    fun `should maintain negative sign when transposing`() {
        // 5 - 29 = -24 -> Trap deve ser -42
        val expr = Expression.Binary(Expression.Number(5.0), operator = Operation.Subtract, right = Expression.Number(29.0))
        assertTrue(DigitTranspositionStrategyArithmetic.canApply(expr, config))
        assertEquals(-42.0, DigitTranspositionStrategyArithmetic.getWrongNumber(expr, config), 0.0)
    }

    @Test
    fun `canApply should return false for numbers ending in zero`() {
        // 10 + 10 = 20 -> Inversão daria 2 (inválido pedagogicamente)
        val expr = Expression.Binary(Expression.Number(10.0), operator = Operation.Add, right = Expression.Number(10.0))
        assertFalse(DigitTranspositionStrategyArithmetic.canApply(expr, config))
    }

    @Test
    fun `canApply should return false for palindromes`() {
        // 20 + 13 = 33 -> Inversão dá 33 (igual à resposta)
        val expr = Expression.Binary(Expression.Number(20.0), operator = Operation.Add, right = Expression.Number(13.0))
        assertFalse(DigitTranspositionStrategyArithmetic.canApply(expr, config))
    }

    @Test
    fun `canApply should return false for single digit results`() {
        // 2 + 3 = 5 -> Impossível transpor
        val expr = Expression.Binary(Expression.Number(2.0), operator = Operation.Add, right = Expression.Number(3.0))
        assertFalse(DigitTranspositionStrategyArithmetic.canApply(expr, config))
    }

    @Test
    fun `canApply should return false for non-integer numbers`() {
        // 5 / 2 = 2.5
        val expr = Expression.Binary(
            Expression.Number(5.0), operator = Operation.Divide, right = Expression.Number(2.0)
        )
        assertFalse(DigitTranspositionStrategyArithmetic.canApply(expr, config))
    }
}