package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MathQuestionDao {
    @Query("SELECT * FROM math_questions ORDER BY timestamp DESC")
    fun getAllQuestions(): Flow<List<MathQuestionEntity>>

    @Query("SELECT * FROM math_questions WHERE isBookmarked = 1 ORDER BY timestamp DESC")
    fun getBookmarkedQuestions(): Flow<List<MathQuestionEntity>>

    @Query("SELECT * FROM math_questions WHERE id = :id LIMIT 1")
    suspend fun getQuestionById(id: Int): MathQuestionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: MathQuestionEntity): Long

    @Query("UPDATE math_questions SET isBookmarked = :isBookmarked WHERE id = :id")
    suspend fun updateBookmark(id: Int, isBookmarked: Boolean)

    @Query("DELETE FROM math_questions WHERE id = :id")
    suspend fun deleteQuestion(id: Int)

    @Query("DELETE FROM math_questions")
    suspend fun clearAll()
}
