package com.example.calculus.domain

import com.example.calculus.domain.model.answer.Answer
import com.example.calculus.domain.model.answer.AnswerValue
import com.example.calculus.domain.problemGenerator.arithmetic.ArithmeticConfig
import com.example.calculus.domain.problemGenerator.arithmetic.ArithmeticAnswerSpecificConfig
import com.example.calculus.domain.model.AnswerOptionConfig
import com.example.calculus.domain.problemGenerator.arithmetic.models.Expression
import com.example.calculus.domain.problemGenerator.arithmetic.models.Operation
import com.example.calculus.domain.problemGenerator.arithmetic.ArithmeticAnswerOrganizer
import com.example.calculus.domain.trap.arithmetic.ArithmeticTrap
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ArithmeticAnswerOrganizerTest {

    private val config = ArithmeticConfig(
        operations = setOf(Operation.Add),
        valueRange = 1..20,
        allowNegativeNumbers = false
    )

    private val multipleChoice = AnswerOptionConfig.MultipleChoice(
        numberOfOptions = 4,
        specificConfig = ArithmeticAnswerSpecificConfig(
            trapPercentage = 1.0, // Sempre tenta aplicar trap
            trapsWeights = mapOf(ArithmeticTrap.SIGN_FLIP to 1.0)
        )
    )

    private val organizer = ArithmeticAnswerOrganizer()

    @Test
    fun `buildOptions should always include the correct answer`() {
        val expression = Expression.Binary(Expression.Number(5.0), Expression.Number(3.0), Operation.Add)
        val correctAnswer = 8.0
        
        val options = organizer.buildOptions(expression, config, multipleChoice)
        
        assertTrue("Options should contain the correct answer", options.any { (it.value as? AnswerValue.ArithmeticValue)?.value == correctAnswer })
    }

    @Test
    fun `buildOptions should return the correct number of options`() {
        val expression = Expression.Binary(Expression.Number(5.0), Expression.Number(3.0), Operation.Add)
        
        val options = organizer.buildOptions(expression, config, multipleChoice)
        
        assertEquals(4, options.size)
    }

    @Test
    fun `buildOptions should apply trap when available and probability is 1`() {
        // 5 + 3 = 8. Trap (Sign Flip) = -8. 
        // Note: SignFlipStrategy.canApply checks config.allowNegativeNumbers.
        // If allowNegativeNumbers is false, and result is 8, trap is -8 -> canApply = false.
        
        // Let's use a case where SignFlip can apply: result is negative and trap is positive.
        val negConfig = config.copy(allowNegativeNumbers = true)
        val expression = Expression.Binary(Expression.Number(5.0), Expression.Number(10.0), Operation.Subtract)
        val correctAnswer = -5.0
        val trapAnswer = 5.0
        
        val options = organizer.buildOptions(expression, negConfig, multipleChoice)
        
        assertTrue("Options should contain the trap answer", options.any { (it.value as? AnswerValue.ArithmeticValue)?.value == trapAnswer })
        assertTrue("Options should contain the correct answer", options.any { (it.value as? AnswerValue.ArithmeticValue)?.value == correctAnswer })
    }

    @Test
    fun `buildOptions should fill with randoms if no traps apply`() {
        val noTrapsChoice = multipleChoice.copy(
            specificConfig = multipleChoice.specificConfig.copy(trapsWeights = emptyMap())
        )
        val expression = Expression.Binary(Expression.Number(5.0), Expression.Number(3.0), Operation.Add)
        
        val options = organizer.buildOptions(expression, config, noTrapsChoice)
        
        assertEquals(4, options.size)
        assertTrue(options.any { (it.value as? AnswerValue.ArithmeticValue)?.value == 8.0 })
    }

    @Test
    fun `buildOptions should never apply traps when trapPercentage is zero`() {
        val zeroTrapChoice = multipleChoice.copy(
            specificConfig = multipleChoice.specificConfig.copy(
                trapPercentage = 0.0,
                trapsWeights = mapOf(ArithmeticTrap.SIGN_FLIP to 1.0)
            )
        )
        // 5 - 10 = -5. A trap do SignFlip seria +5
        val negConfig = config.copy(allowNegativeNumbers = true)
        val expression = Expression.Binary(Expression.Number(5.0), Expression.Number(10.0), Operation.Subtract)

        repeat(50) {
            val options = organizer.buildOptions(expression, negConfig, zeroTrapChoice)
            // Se a probabilidade é 0, a trap (5.0) não pode estar garantida via trap
            // (ela só poderia aparecer se o random sorteasse, mas o random não deve colidir)
            assertTrue(options.any { (it.value as? AnswerValue.ArithmeticValue)?.value == -5.0 })
        }
    }

    @Test
    fun `buildOptions should handle zero weight traps gracefully without crashing`() {
        val zeroWeightChoice = multipleChoice.copy(
            specificConfig = multipleChoice.specificConfig.copy(
                trapPercentage = 1.0,
                trapsWeights = mapOf(ArithmeticTrap.SIGN_FLIP to 0.0) // Peso zero!
            )
        )
        val expression = Expression.Binary(Expression.Number(5.0), Expression.Number(3.0), Operation.Add)

        // Não deve lançar IllegalArgumentException
        val options = organizer.buildOptions(expression, config, zeroWeightChoice)
        assertEquals(4, options.size)
    }

    @Test
    fun `buildOptions should never contain duplicate values`() {
        val expression = Expression.Binary(Expression.Number(5.0), Expression.Number(3.0), Operation.Add)

        repeat(50) {
            val options = organizer.buildOptions(expression, config, multipleChoice)
            val uniqueValues = options.map { it.value }.toSet()
            assertEquals(
                "Options contain duplicate values: ${options.map { it.value }}",
                options.size,
                uniqueValues.size
            )
        }
    }

    @Test
    fun `buildOptions should NEVER contain negative numbers when allowNegativeNumbers is false`() {
        val nonNegativeConfig = config.copy(allowNegativeNumbers = false)
        // Uma expressão com resultado pequeno próximo a zero (ex: 2 - 1 = 1)
        val expression = Expression.Binary(Expression.Number(2.0), Expression.Number(1.0), Operation.Subtract)

        // Roda várias vezes para garantir estatisticamente que o gerador randômico não erra
        repeat(50) {
            val options = organizer.buildOptions(expression, nonNegativeConfig, multipleChoice)
            assertTrue(
                "Found negative number in options: $options",
                options.all { ((it.value as? AnswerValue.ArithmeticValue)?.value ?: 0.0) >= 0.0 }
            )
        }
    }

    @Test
    fun `buildOptions should skip trap if trap value is equal to correct answer`() {
        // SignFlip de 0.0 é 0.0.
        val zeroExpression = Expression.Binary(Expression.Number(0.0), Expression.Number(0.0), Operation.Add)
        val choiceWithSignFlip = multipleChoice.copy(
            specificConfig = multipleChoice.specificConfig.copy(
                trapPercentage = 1.0,
                trapsWeights = mapOf(ArithmeticTrap.SIGN_FLIP to 1.0)
            )
        )

        val options = organizer.buildOptions(zeroExpression, config.copy(allowNegativeNumbers = true), choiceWithSignFlip)

        // Deve ter 4 opções, mas NENHUMA delas deve ser do tipo Answer.Trap (já que foi descartada por ser 0.0)
        assertEquals(4, options.size)
        assertTrue("Should not contain a Trap because trap value was identical to correct answer",
            options.none { it is Answer.Trap }
        )
    }

    @Test
    fun `buildOptions should handle collisions in random filling`() {
        val expression = Expression.Binary(Expression.Number(10.0), Expression.Number(0.0), Operation.Add)
        // Offset range muito pequeno (1). Opções possíveis: 9, 11 (correta é 10).
        // Se pedir 4 opções, ele só vai conseguir 3 (9, 10, 11).
        val smallRangeChoice = multipleChoice.copy(
            numberOfOptions = 4,
            specificConfig = multipleChoice.specificConfig.copy(
                randomOffsetRange = 1,
                trapPercentage = 0.0
            )
        )

        val options = organizer.buildOptions(expression, config, smallRangeChoice)

        // O set garante unicidade.
        val uniqueValues = options.map { (it.value as AnswerValue.ArithmeticValue).value }.toSet()
        assertEquals("Unique values count should match options size", options.size, uniqueValues.size)

        // Com correctAnswer = 10 e offset = 1, as únicas opções são 9, 10, 11.
        assertTrue("Should have exactly 3 options due to small range limit", options.size == 3)
    }
    @Test
    fun `buildOptions should NEVER produce duplicate values across 500 random runs with traps enabled`() {
        // 6 - 5 = 1.0 (O caso exato que causou o bug na sua UI!)
        val expression = Expression.Binary(
            Expression.Number(6.0),
            Expression.Number(5.0),
            Operation.Subtract
        )

        val choiceWithTraps = multipleChoice.copy(
            numberOfOptions = 4,
            specificConfig = multipleChoice.specificConfig.copy(
                trapPercentage = 1.0, // FORÇA a trap a ser aplicada em todas as rodadas!
                randomOffsetRange = 3, // Range estreito para forçar colisões constantes
                trapsWeights = mapOf(
                    ArithmeticTrap.OFF_BY_ONE to 1.0,
                    ArithmeticTrap.OFF_BY_TEN to 1.0
                )
            )
        )

        // Roda 500 vezes para testar todas as combinações randômicas possíveis
        repeat(500) { iteration ->
            val options = organizer.buildOptions(expression, config, choiceWithTraps)

            // 1. Extrai os valores numéricos reais
            val values = options.map { (it.value as AnswerValue.ArithmeticValue).value }

            // 2. Transforma em Set para ver se o tamanho diminui (se diminuir, houve duplicado!)
            val uniqueValues = values.toSet()

            assertEquals(
                "Iteração $iteration falhou! Opções contêm valores duplicados: $values",
                values.size,
                uniqueValues.size
            )

            // 3. Garante que sempre foram entregues as 4 opções pedidas
            assertEquals("Deveria ter exatamente 4 opções", 4, options.size)
        }
    }
}
