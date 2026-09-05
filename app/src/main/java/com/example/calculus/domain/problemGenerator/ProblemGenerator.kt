package com.example.calculus.domain.problemGenerator

import com.example.calculus.domain.model.MathProblem

interface ProblemGenerator {
    val config: GameConfig
    fun generateProblem(): MathProblem
}