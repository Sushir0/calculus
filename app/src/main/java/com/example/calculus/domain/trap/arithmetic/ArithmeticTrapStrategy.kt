package com.example.calculus.domain.trap.arithmetic

import com.example.calculus.domain.problemGenerator.arithmetic.ArithmeticConfig
import com.example.calculus.domain.problemGenerator.arithmetic.models.Expression

interface ArithmeticTrapStrategy {
    /**
     * Retorna um número errado plausível.
    */
    fun getWrongNumber(
        expression: Expression,
        config: ArithmeticConfig
    ): Double

    /**
     * Retorna true se esta trap se aplica à expressão atual.
     */
    fun canApply(expression: Expression, config: ArithmeticConfig): Boolean
}