package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.QuestionDao
import com.example.data.local.QuestionImporter
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class QuestionImporterTest {
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
    fun testImportingTwiceGivesSameRowCount() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val importer = QuestionImporter(context, dao)

        val jsonAsset = """
        [
          {
            "id": "q1",
            "grade": 11,
            "difficulty": "آسان",
            "topic": "مثلثات",
            "questionText": "سوال 1",
            "option1": "1",
            "option2": "2",
            "option3": "3",
            "option4": "4",
            "correctOptionIndex": 0,
            "explanation": "راهحل 1"
          },
          {
            "id": "q2",
            "grade": 12,
            "difficulty": "سخت",
            "topic": "مشتق",
            "questionText": "سوال 2",
            "option1": "1",
            "option2": "2",
            "option3": "3",
            "option4": "4",
            "correctOptionIndex": 1,
            "explanation": "راهحل 2"
          }
        ]
        """.trimIndent().toByteArray(Charsets.UTF_8)

        val firstImport = importer.importBytes(jsonAsset)
        assertTrue(firstImport)
        assertEquals(2, dao.getCount())

        val secondImport = importer.importBytes(jsonAsset)
        assertTrue(secondImport)
        assertEquals(2, dao.getCount())
    }

    @Test
    fun testChangedAssetAddsNewRowsWithoutTouchingOldOnes() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val importer = QuestionImporter(context, dao)

        val initialJson = """
        [
          {
            "id": "q1",
            "grade": 11,
            "difficulty": "آسان",
            "topic": "مثلثات",
            "questionText": "سوال 1",
            "option1": "1",
            "option2": "2",
            "option3": "3",
            "option4": "4",
            "correctOptionIndex": 0,
            "explanation": "راهحل 1"
          }
        ]
        """.trimIndent().toByteArray(Charsets.UTF_8)

        importer.importBytes(initialJson)
        assertEquals(1, dao.getCount())
        val q1Before = dao.getQuestionById("q1")
        assertNotNull(q1Before)

        val updatedJsonWithNewAndModifiedQ1 = """
        [
          {
            "id": "q1",
            "grade": 11,
            "difficulty": "آسان",
            "topic": "مثلثات",
            "questionText": "متن جدید نباید جایگزین شود",
            "option1": "1",
            "option2": "2",
            "option3": "3",
            "option4": "4",
            "correctOptionIndex": 0,
            "explanation": "توضیح"
          },
          {
            "id": "q2",
            "grade": 12,
            "difficulty": "متوسط",
            "topic": "حد",
            "questionText": "سوال 2 جدید",
            "option1": "1",
            "option2": "2",
            "option3": "3",
            "option4": "4",
            "correctOptionIndex": 2,
            "explanation": "توضیح 2"
          }
        ]
        """.trimIndent().toByteArray(Charsets.UTF_8)

        importer.importBytes(updatedJsonWithNewAndModifiedQ1)
        assertEquals(2, dao.getCount())
        val q1After = dao.getQuestionById("q1")
        assertEquals("Old question text must remain untouched due to IGNORE conflict strategy", "سوال 1", q1After?.questionText)
        val q2 = dao.getQuestionById("q2")
        assertNotNull("New question must be added", q2)
    }
}
