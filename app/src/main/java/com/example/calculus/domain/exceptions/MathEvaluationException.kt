package com.example.calculus.domain.exceptions

import com.example.calculus.domain.problemGenerator.arithmetic.ArithmeticConfig

sealed class MathEvaluationException(message: String) : RuntimeException(message) {
    class DivisionByZeroException(
        val config: ArithmeticConfig
    ) : MathEvaluationException("Não foi possível gerar um divisor válido (não zero) com a configuração: $config.")
}
