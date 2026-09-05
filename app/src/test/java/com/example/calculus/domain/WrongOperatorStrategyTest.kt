package com.example.calculus.domain

import com.example.calculus.domain.problemGenerator.arithmetic.ArithmeticConfig
import com.example.calculus.domain.problemGenerator.arithmetic.models.Expression
import com.example.calculus.domain.problemGenerator.arithmetic.models.Operation
import com.example.calculus.domain.trap.arithmetic.WrongOperatorStrategyArithmetic
import org.junit.Assert.*
import org.junit.Test

class WrongOperatorStrategyTest {

    private val defaultConfig = ArithmeticConfig(
        operations = setOf(Operation.Add, Operation.Subtract, Operation.Multiply, Operation.Divide),
        allowNegativeNumbers = true
    )

    @Test
    fun `getWrongNumber should use alternative operator`() {
        // Add -> Multiply: 2 + 3 = 5, Trap: 2 * 3 = 6
        val addExpr = Expression.Binary(Expression.Number(2.0), Expression.Number(3.0), Operation.Add)
        assertEquals(6.0, WrongOperatorStrategyArithmetic.getWrongNumber(addExpr, defaultConfig), 0.0)

        // Subtract -> Add: 10 - 4 = 6, Trap: 10 + 4 = 14
        val subExpr = Expression.Binary(Expression.Number(10.0), Expression.Number(4.0), Operation.Subtract)
        assertEquals(14.0, WrongOperatorStrategyArithmetic.getWrongNumber(subExpr, defaultConfig), 0.0)

        // Multiply -> Add: 3 * 4 = 12, Trap: 3 + 4 = 7
        val mulExpr = Expression.Binary(Expression.Number(3.0), Expression.Number(4.0), Operation.Multiply)
        assertEquals(7.0, WrongOperatorStrategyArithmetic.getWrongNumber(mulExpr, defaultConfig), 0.0)

        // Divide -> Subtract: 10 / 2 = 5, Trap: 10 - 2 = 8
        val divExpr = Expression.Binary(Expression.Number(10.0), Expression.Number(2.0), Operation.Divide)
        assertEquals(8.0, WrongOperatorStrategyArithmetic.getWrongNumber(divExpr, defaultConfig), 0.0)
    }

    @Test
    fun `canApply should return false for non-binary expressions`() {
        val numberExpression = Expression.Number(10.0)
        assertFalse(WrongOperatorStrategyArithmetic.canApply(numberExpression, defaultConfig))
    }

    @Test
    fun `canApply should return false when potential trap is equal to correct answer`() {
        // 2 + 2 = 4, 2 * 2 = 4. Trap == Correct -> should return false
        val expression = Expression.Binary(Expression.Number(2.0), Expression.Number(2.0), Operation.Add)
        assertEquals(4.0, expression.evaluate(), 0.0)
        assertEquals(4.0, WrongOperatorStrategyArithmetic.getWrongNumber(expression, defaultConfig), 0.0)
        assertFalse(WrongOperatorStrategyArithmetic.canApply(expression, defaultConfig))
    }

    @Test
    fun `canApply should return true when negative is allowed and trap is negative`() {
        val config = defaultConfig.copy(allowNegativeNumbers = true)
        // 2 - 5 = -3. Trap (Add): 2 + 5 = 7. 
        // Or Reverse: 2 + (-5) = -3. Trap (Multiply): 2 * -5 = -10.
        val expression = Expression.Binary(Expression.Number(2.0), Expression.Number(-5.0), Operation.Add)
        // 2 + (-5) = -3. Trap: 2 * -5 = -10.
        assertTrue(WrongOperatorStrategyArithmetic.canApply(expression, config))
    }

    @Test
    fun `canApply should return false when negative is NOT allowed and trap is negative`() {
        val config = defaultConfig.copy(allowNegativeNumbers = false)
        // 2 - 5 = -3. Trap (Add): 2 + 5 = 7 (This would be true)
        // Let's find one where trap is negative: 1 * -5 = -5. Trap (Add): 1 + -5 = -4.
        val expression = Expression.Binary(Expression.Number(1.0), Expression.Number(-5.0), Operation.Add)
        // Correct: 1 * -5 = -5 (Wait, Multiply -> Add)
        // Let's use Subtract -> Add: 2 - 10 = -8. Trap: 2 + 10 = 12.
        // Let's use Add -> Multiply: 2 + (-5) = -3. Trap: 2 * -5 = -10.
        val negativeTrapExpr = Expression.Binary(Expression.Number(2.0), Expression.Number(-5.0), Operation.Add)
        assertEquals(-10.0, WrongOperatorStrategyArithmetic.getWrongNumber(negativeTrapExpr, config), 0.0)
        
        // Should return false because negative not allowed
        assertFalse(WrongOperatorStrategyArithmetic.canApply(negativeTrapExpr, config))
    }
}
