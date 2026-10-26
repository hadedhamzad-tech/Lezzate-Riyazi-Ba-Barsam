package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.QuestionDao
import com.example.data.local.QuestionEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class QuestionDaoTest {
    private lateinit var db: AppDatabase
    private lateinit var dao: QuestionDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.questionDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testGetTopicsForGrade() = runTest {
        val questions = listOf(
            QuestionEntity("1", 11, "آسان", "مثلثات", "س1", "1", "2", "3", "4", 0, "ت"),
            QuestionEntity("2", 11, "متوسط", "تابع", "س2", "1", "2", "3", "4", 1, "ت"),
            QuestionEntity("3", 11, "سخت", "مثلثات", "س3", "1", "2", "3", "4", 2, "ت"),
            QuestionEntity("4", 12, "آسان", "مشتق", "س4", "1", "2", "3", "4", 3, "ت")
        )
        dao.insertQuestions(questions)

        val topics11 = dao.getTopicsForGrade(11).first()
        assertEquals(2, topics11.size)
        assertEquals("تابع", topics11[0])
        assertEquals("مثلثات", topics11[1])

        val topics12 = dao.getTopicsForGrade(12).first()
        assertEquals(listOf("مشتق"), topics12)

        val topics10 = dao.getTopicsForGrade(10).first()
        assertTrue(topics10.isEmpty())
    }

    @Test
    fun testGetFilteredQuestions() = runTest {
        val questions = listOf(
            QuestionEntity("1", 11, "آسان", "مثلثات", "س1", "1", "2", "3", "4", 0, "ت"),
            QuestionEntity("2", 11, "سخت", "مثلثات", "س2", "1", "2", "3", "4", 1, "ت"),
            QuestionEntity("3", 11, "آسان", "تابع", "س3", "1", "2", "3", "4", 2, "ت"),
            QuestionEntity("4", 12, "آسان", "مشتق", "س4", "1", "2", "3", "4", 3, "ت")
        )
        dao.insertQuestions(questions)

        // All grade 11 questions
        val grade11All = dao.getFilteredQuestions(grade = 11, topic = null, difficulty = null).first()
        assertEquals(3, grade11All.size)

        // Grade 11, topic مثلثات
        val grade11Trig = dao.getFilteredQuestions(grade = 11, topic = "مثلثات", difficulty = null).first()
        assertEquals(2, grade11Trig.size)

        // Grade 11, topic مثلثات, difficulty آسان
        val grade11TrigEasy = dao.getFilteredQuestions(grade = 11, topic = "مثلثات", difficulty = "آسان").first()
        assertEquals(1, grade11TrigEasy.size)
        assertEquals("1", grade11TrigEasy[0].id)

        // Non-matching filter
        val nonMatching = dao.getFilteredQuestions(grade = 11, topic = "مشتق", difficulty = null).first()
        assertTrue(nonMatching.isEmpty())
    }
}
