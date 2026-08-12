package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "math_questions")
data class MathQuestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val questionText: String,
    val hasImage: Boolean = false,
    val category: String,
    val summary: String,
    val stepsJson: String,
    val finalAnswer: String,
    val keyConceptsJson: String = "[]",
    val timestamp: Long = System.currentTimeMillis(),
    val isBookmarked: Boolean = false
)
