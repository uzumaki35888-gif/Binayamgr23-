package com.example.model

data class SolutionStep(
    val stepNumber: Int,
    val title: String,
    val explanation: String,
    val mathExpression: String = "",
    val keyFormula: String = ""
)

data class MathSolution(
    val id: Int = 0,
    val questionText: String,
    val category: String, // Algebra, Calculus, Geometry, Trigonometry, Word Problem, etc.
    val summary: String,
    val finalAnswer: String,
    val steps: List<SolutionStep>,
    val keyConcepts: List<String> = emptyList(),
    val similarPracticeQuestions: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis(),
    val isBookmarked: Boolean = false
)
