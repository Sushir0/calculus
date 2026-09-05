package com.example.calculus.domain.problemGenerator.arithmetic.models

enum class Operation {
    Add,
    Subtract,
    Multiply,
    Divide;

    // TODO adicionar outros operadores conforme

    val symbol: String
        get() = when (this) {
            Operation.Add -> "+"
            Operation.Subtract -> "-"
            Operation.Multiply -> "X"
            Operation.Divide -> "/"
        }

    fun calculate(left: Double, right: Double): Double {
        return when (this) {
            Operation.Add -> left + right
            Operation.Subtract -> left - right
            Operation.Multiply -> left * right
            Operation.Divide -> left / right
        }
    }
}