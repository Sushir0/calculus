package com.example.calculus.domain.arithmetic

import com.example.calculus.domain.model.AnswerOptionConfig
import com.example.calculus.domain.problemGenerator.arithmetic.models.Operation
import com.example.calculus.domain.problemGenerator.arithmetic.ArithmeticAnswerSpecificConfig
import com.example.calculus.domain.problemGenerator.arithmetic.ArithmeticConfig
import com.example.calculus.domain.problemGenerator.arithmetic.ArithmeticConfigError
import com.example.calculus.domain.trap.arithmetic.ArithmeticTrap
import org.junit.Assert.*
import org.junit.Test

class ArithmeticConfigTest {

    @Test
    fun `valid configuration should have no errors`() {
        val config = ArithmeticConfig(
            operations = setOf(Operation.Add),
            valueRange = 1..10,
            expressionRange = 2..3
        )
        
        assertTrue(config.isValid)
        assertNull(config.validationResult.operationsError)
        assertNull(config.validationResult.valueRangeError)
        assertNull(config.validationResult.expressionRangeError)
    }

    @Test
    fun `should detect empty operations`() {
        val config = ArithmeticConfig(
            operations = emptySet()
        )
        
        assertFalse(config.isValid)
        assertEquals(ArithmeticConfigError.NoOperationsProvided, config.validationResult.operationsError)
    }

    @Test
    fun `should detect expression too short`() {
        val config = ArithmeticConfig(
            operations = setOf(Operation.Add),
            expressionRange = 1..1 // Mínimo deve ser 2
        )
        
        assertFalse(config.isValid)
        assertEquals(ArithmeticConfigError.ExpressionTooShort, config.validationResult.expressionRangeError)
    }

    @Test
    fun `should detect empty range`() {
        val config = ArithmeticConfig(
            operations = setOf(Operation.Add),
            valueRange = 10..1 // Início maior que o fim
        )
        
        assertFalse(config.isValid)
        assertEquals(ArithmeticConfigError.RangeIsEmpty, config.validationResult.valueRangeError)
    }

    @Test
    fun `should detect division with only zeros`() {
        val config = ArithmeticConfig(
            operations = setOf(Operation.Divide),
            valueRange = 0..0
        )
        
        assertFalse(config.isValid)
        assertEquals(ArithmeticConfigError.DivisionRequireNonZero, config.validationResult.valueRangeError)
    }

    @Test
    fun `should detect negative numbers when not allowed`() {
        val config = ArithmeticConfig(
            operations = setOf(Operation.Add),
            valueRange = -5..5,
            allowNegativeNumbers = false
        )
        
        assertFalse(config.isValid)
        assertEquals(ArithmeticConfigError.NegativeNumbersNotAllowed, config.validationResult.valueRangeError)
    }

    @Test
    fun `should allow negative numbers when explicitly enabled`() {
        val config = ArithmeticConfig(
            operations = setOf(Operation.Add),
            valueRange = -5..5,
            allowNegativeNumbers = true
        )
        
        assertTrue(config.isValid)
        assertNull(config.validationResult.valueRangeError)
    }

    @Test
    fun `should detect too few options in multiple choice`() {
        val config = ArithmeticConfig(
            operations = setOf(Operation.Add),
            answerOptionsConfig = AnswerOptionConfig.MultipleChoice(
                numberOfOptions = 1,
                specificConfig = ArithmeticAnswerSpecificConfig()
            )
        )

        assertFalse(config.isValid)
        assertEquals(ArithmeticConfigError.TooFewOptions, config.validationResult.answerOptionError)
    }

    @Test
    fun `should detect too many options in multiple choice`() {
        val config = ArithmeticConfig(
            operations = setOf(Operation.Add),
            answerOptionsConfig = AnswerOptionConfig.MultipleChoice(
                numberOfOptions = 10,
                specificConfig = ArithmeticAnswerSpecificConfig()
            )
        )

        assertFalse(config.isValid)
        assertEquals(ArithmeticConfigError.TooManyOptions, config.validationResult.answerOptionError)
    }

    @Test
    fun `should detect offset range too small`() {
        val config = ArithmeticConfig(
            operations = setOf(Operation.Add),
            answerOptionsConfig = AnswerOptionConfig.MultipleChoice(
                numberOfOptions = 4,
                specificConfig = ArithmeticAnswerSpecificConfig(randomOffsetRange = 1)
            )
        )

        assertFalse(config.isValid)
        assertEquals(ArithmeticConfigError.OffsetRangeTooSmall, config.validationResult.answerOptionError)
    }

    @Test
    fun `should detect traps require options`() {
        val config = ArithmeticConfig(
            operations = setOf(Operation.Add),
            answerOptionsConfig = AnswerOptionConfig.MultipleChoice(
                numberOfOptions = 4,
                specificConfig = ArithmeticAnswerSpecificConfig(
                    trapPercentage = 0.5,
                    trapsWeights = emptyMap()
                )
            )
        )

        assertFalse(config.isValid)
        assertEquals(ArithmeticConfigError.TrapsRequireOptions, config.validationResult.trapError)
    }

    @Test
    fun `should detect invalid trap probability`() {
        val config = ArithmeticConfig(
            operations = setOf(Operation.Add),
            answerOptionsConfig = AnswerOptionConfig.MultipleChoice(
                numberOfOptions = 4,
                specificConfig = ArithmeticAnswerSpecificConfig(trapPercentage = 1.5)
            )
        )

        assertFalse(config.isValid)
        assertEquals(ArithmeticConfigError.InvalidTrapProbability, config.validationResult.trapError)
    }

    @Test
    fun `should detect negative trap weight`() {
        val config = ArithmeticConfig(
            operations = setOf(Operation.Add),
            answerOptionsConfig = AnswerOptionConfig.MultipleChoice(
                numberOfOptions = 4,
                specificConfig = ArithmeticAnswerSpecificConfig(
                    trapsWeights = mapOf(ArithmeticTrap.SIGN_FLIP to -1.0)
                )
            )
        )

        assertFalse(config.isValid)
        assertEquals(ArithmeticConfigError.NegativeTrapWeight, config.validationResult.trapError)
    }

    @Test
    fun `text answer mode should be valid`() {
        val config = ArithmeticConfig(
            operations = setOf(Operation.Add),
            answerOptionsConfig = AnswerOptionConfig.Text
        )

        assertTrue(config.isValid)
        assertNull(config.validationResult.answerOptionError)
        assertNull(config.validationResult.trapError)
    }

    @Test
    fun `should detect empty or inverted expression range`() {
        val config = ArithmeticConfig(
            operations = setOf(Operation.Add),
            expressionRange = 5..2
        )

        assertFalse(config.isValid)
        assertEquals(ArithmeticConfigError.RangeIsEmpty, config.validationResult.expressionRangeError)
    }

    @Test
    fun `division should be valid if range contains non-zero numbers`() {
        val config = ArithmeticConfig(
            operations = setOf(Operation.Divide),
            valueRange = 0..10
        )

        assertTrue(config.isValid)
        assertNull(config.validationResult.valueRangeError)
    }

    @Test
    fun `boundary values for multiple choice should be valid`() {
        val minOptionsConfig = ArithmeticConfig(
            operations = setOf(Operation.Add),
            answerOptionsConfig = AnswerOptionConfig.MultipleChoice(
                numberOfOptions = 2,
                specificConfig = ArithmeticAnswerSpecificConfig()
            )
        )
        assertTrue("Min options (2) should be valid", minOptionsConfig.isValid)

        val maxOptionsConfig = ArithmeticConfig(
            operations = setOf(Operation.Add),
            answerOptionsConfig = AnswerOptionConfig.MultipleChoice(
                numberOfOptions = 6,
                specificConfig = ArithmeticAnswerSpecificConfig()
            )
        )
        assertTrue("Max options (6) should be valid", maxOptionsConfig.isValid)
    }

    @Test
    fun `boundary values for trap percentage should be valid`() {
        val minTrapConfig = ArithmeticConfig(
            operations = setOf(Operation.Add),
            answerOptionsConfig = AnswerOptionConfig.MultipleChoice(
                numberOfOptions = 4,
                specificConfig = ArithmeticAnswerSpecificConfig(trapPercentage = 0.0)
            )
        )
        assertTrue("Min trap percentage (0.0) should be valid", minTrapConfig.isValid)

        val maxTrapConfig = ArithmeticConfig(
            operations = setOf(Operation.Add),
            answerOptionsConfig = AnswerOptionConfig.MultipleChoice(
                numberOfOptions = 4,
                specificConfig = ArithmeticAnswerSpecificConfig(trapPercentage = 1.0)
            )
        )
        assertTrue("Max trap percentage (1.0) should be valid", maxTrapConfig.isValid)
    }

    @Test
    fun `should detect trap percentage below zero`() {
        val config = ArithmeticConfig(
            operations = setOf(Operation.Add),
            answerOptionsConfig = AnswerOptionConfig.MultipleChoice(
                numberOfOptions = 4,
                specificConfig = ArithmeticAnswerSpecificConfig(trapPercentage = -0.1)
            )
        )

        assertFalse(config.isValid)
        assertEquals(ArithmeticConfigError.InvalidTrapProbability, config.validationResult.trapError)
    }
}
