package com.example.calculus.domain.arithmetic

import com.example.calculus.domain.problemGenerator.arithmetic.models.Operation
import com.example.calculus.domain.model.answer.AnswerValue
import com.example.calculus.domain.problemGenerator.arithmetic.ArithmeticConfig
import com.example.calculus.domain.problemGenerator.arithmetic.ArithmeticProblemGenerator
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ArithmeticProblemGeneratorTest {

    @Test
    fun `generateProblem should NEVER generate negative answers when allowNegativeNumbers is false`() {
        val nonNegativeConfig = ArithmeticConfig(
            operations = setOf(Operation.Add, Operation.Subtract, Operation.Multiply, Operation.Divide),
            valueRange = 1..20,
            expressionRange = 2..4, // Testa contas com 2, 3 e 4 termos!
            allowNegativeNumbers = false
        )

        val generator = ArithmeticProblemGenerator(nonNegativeConfig)

        // Roda 1.000 vezes consecutivas para forçar qualquer combinação matemática
        repeat(1000) {
            val problem = generator.generateProblem()
            val result = (problem.correctAnswer.value as AnswerValue.ArithmeticValue).value

            assertTrue(
                "Gerou um resultado negativo indesejado: ${problem.prompt} = $result",
                result >= 0.0
            )
        }
    }

    @Test
    fun `generateProblem should return a valid MathProblem`() {
        val config = ArithmeticConfig(
            operations = setOf(Operation.Add, Operation.Subtract, Operation.Multiply),
            valueRange = 1..10,
            expressionRange = 2..3,
            allowNegativeNumbers = false
        )
        val generator = ArithmeticProblemGenerator(config)
        
        val problem = generator.generateProblem()
        
        assertNotNull(problem)
        assertNotNull(problem.prompt)
        assertNotNull(problem.correctAnswer)
        
        // Print for manual verification (similar to what was in main)
        println("Generated Problem: ${problem.prompt} = ${problem.correctAnswer}")
    }

    @Test
    fun `generated problem should evaluate correctly`() {
        val config = ArithmeticConfig(
            operations = setOf(Operation.Add),
            valueRange = 1..10,
            expressionRange = 2..2
        )
        val generator = ArithmeticProblemGenerator(config)
        
        val problem = generator.generateProblem()
        
        // Check if prompt contains '+'
        assertTrue(problem.prompt.contains("+"))
        
        // Basic check: result should be a number
        val result = (problem.correctAnswer.value as? AnswerValue.ArithmeticValue)?.value
        assertNotNull("Correct answer should be a valid double", result)
    }

    @Test
    fun `generator should produce multiple different problems`() {
        val config = ArithmeticConfig(
            operations = setOf(Operation.Add, Operation.Subtract, Operation.Multiply),
            valueRange = 1..100,
            expressionRange = 2..5
        )
        val generator = ArithmeticProblemGenerator(config)
        
        val problems = List(10) { generator.generateProblem() }
        
        problems.forEach { problem ->
            println("${problem.prompt} = ${problem.correctAnswer}")
            assertNotNull(problem.prompt)
            assertNotNull(problem.correctAnswer)
        }
    }

    @Test
    fun `generator should avoid division by zero`() {
        val config = ArithmeticConfig(
            operations = setOf(Operation.Divide),
            valueRange = 0..1, // Apenas 0 e 1 disponíveis
            expressionRange = 2..2
        )
        val generator = ArithmeticProblemGenerator(config)
        
        // Geramos 100 problemas para garantir que o zero nunca seja escolhido como divisor
        repeat(100) {
            val problem = generator.generateProblem()
            // Se o prompt for "X / 0", o evaluate daria Infinity ou NaN
            assertTrue("Divisor não deve ser zero em: ${problem.prompt}", !problem.prompt.endsWith(" 0.0"))
            val result = (problem.correctAnswer.value as? AnswerValue.ArithmeticValue)?.value ?: Double.NaN
            assertTrue("Resultado não deve ser infinito", result.isFinite())
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `generator should throw exception if only zero is available for division`() {
        val config = ArithmeticConfig(
            operations = setOf(Operation.Divide),
            valueRange = 0..0, // Apenas zero disponível
            expressionRange = 2..2
        )
        // Isso agora deve falhar no init do gerador
        ArithmeticProblemGenerator(config)
    }
}
