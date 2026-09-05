package com.example.calculus.domain.problemGenerator.arithmetic.models

sealed interface Expression {
    fun evaluate(): Double

    fun toDisplayString(): String

    data class Number(val value: Double) : Expression {
        override fun evaluate(): Double = value
        override fun toDisplayString(): String {
            return value.toString()
        }
    }

    data class Binary(val left: Expression, val right: Expression, val operator: Operation) : Expression {
        override fun evaluate(): Double {
            val leftValue = left.evaluate()
            val rightValue = right.evaluate()
            return operator.calculate(leftValue, rightValue)
        }

        override fun toDisplayString(): String {
            return "${left.toDisplayString()} ${operator.symbol} ${right.toDisplayString()}"
        }
    }

    data class Group(val expression: Expression, val delimiter: GroupDelimiter = GroupDelimiter.Parentheses) : Expression {
        override fun evaluate(): Double {
            return expression.evaluate()
        }

        override fun toDisplayString(): String {
            return "${delimiter.openDelimiter} ${expression.toDisplayString()} ${delimiter.closeDelimiter}"
        }

    }
}