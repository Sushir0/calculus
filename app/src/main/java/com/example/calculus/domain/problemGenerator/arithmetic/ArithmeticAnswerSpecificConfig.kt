package com.example.calculus.domain.problemGenerator.arithmetic

import com.example.calculus.domain.trap.arithmetic.ArithmeticTrap

data class ArithmeticAnswerSpecificConfig (
    val randomOffsetRange: Int = 10,
    val trapPercentage: Double = 0.1,
    val trapsWeights: Map<ArithmeticTrap, Double> = mapOf(
        ArithmeticTrap.OFF_BY_TEN to 0.1,
        ArithmeticTrap.OFF_BY_ONE to 0.1,
        ArithmeticTrap.WRONG_OPERATOR to 0.1
    ),
)