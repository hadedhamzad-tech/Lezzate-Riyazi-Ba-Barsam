package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionDao {
    @Query("SELECT * FROM questions ORDER BY id ASC")
    fun getAllQuestions(): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE id = :id LIMIT 1")
    suspend fun getQuestionById(id: String): QuestionEntity?

    @Query("SELECT * FROM questions WHERE grade = :grade")
    fun getQuestionsByGrade(grade: Int): Flow<List<QuestionEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertQuestion(question: QuestionEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertQuestions(questions: List<QuestionEntity>)

    @Query("SELECT COUNT(*) FROM questions")
    suspend fun getCount(): Int
}
