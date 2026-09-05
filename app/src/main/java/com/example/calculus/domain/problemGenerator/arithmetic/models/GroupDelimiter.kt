package com.example.calculus.domain.problemGenerator.arithmetic.models

enum class GroupDelimiter {
    Parentheses,
    SquareBrackets,
    CurlyBrackets;

    val openDelimiter: String
        get() = when (this) {
            Parentheses -> "("
            SquareBrackets -> "["
            CurlyBrackets -> "{"
        }

    val closeDelimiter: String
        get() = when (this) {
            Parentheses -> ")"
            SquareBrackets -> "]"
            CurlyBrackets -> "}"
        }
}
