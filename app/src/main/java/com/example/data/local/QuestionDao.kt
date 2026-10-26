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

    @Query("SELECT DISTINCT topic FROM questions WHERE grade = :grade ORDER BY topic ASC")
    fun getTopicsForGrade(grade: Int): Flow<List<String>>

    @Query(
        """
        SELECT * FROM questions 
        WHERE grade = :grade 
          AND (:topic IS NULL OR topic = :topic) 
          AND (:difficulty IS NULL OR difficulty = :difficulty)
        ORDER BY id ASC
        """
    )
    fun getFilteredQuestions(grade: Int, topic: String?, difficulty: String?): Flow<List<QuestionEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertQuestion(question: QuestionEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertQuestions(questions: List<QuestionEntity>)

    @Query("SELECT COUNT(*) FROM questions")
    suspend fun getCount(): Int
}
