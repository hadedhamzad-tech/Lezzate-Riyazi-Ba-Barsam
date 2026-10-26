package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.QuestionImporter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class QuestionBankAssetTest {

    @Test
    fun testRealAssetFileIntegrity() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val bytes = context.assets.open("questions/bank_v1.json").use { it.readBytes() }
        assertNotNull("Asset bytes should not be null", bytes)
        assertTrue("Asset bytes should not be empty", bytes.isNotEmpty())

        val questions = QuestionImporter.parseQuestions(bytes)
        assertNotNull(questions)
        assertEquals("Asset must contain exactly 1424 questions", 1424, questions.size)

        val ids = mutableSetOf<String>()
        val validGrades = setOf(10, 11, 12)
        val validDifficulties = setOf("آسان", "متوسط", "سخت", "کنکوری")

        var gradeMismatchCount = 0
        var difficultyMismatchCount = 0
        var blankOptionCount = 0
        var invalidCorrectOptionIndexCount = 0
        var blankExplanationCount = 0

        questions.forEachIndexed { index, q ->
            assertTrue("Duplicate question id found: ${q.id} at index $index", ids.add(q.id))

            if (q.grade !in validGrades) {
                gradeMismatchCount++
            }

            if (q.difficulty !in validDifficulties) {
                difficultyMismatchCount++
            }

            if (q.option1.isBlank() || q.option2.isBlank() || q.option3.isBlank() || q.option4.isBlank()) {
                blankOptionCount++
            }

            if (q.correctOptionIndex !in 0..3) {
                invalidCorrectOptionIndexCount++
            }

            if (q.explanation.isBlank()) {
                blankExplanationCount++
            }
        }

        assertEquals("All questions must have grade in {10, 11, 12}", 0, gradeMismatchCount)
        assertEquals("All questions must have difficulty in {آسان, متوسط, سخت, کنکوری}", 0, difficultyMismatchCount)
        assertEquals("All questions must have four non-blank options", 0, blankOptionCount)
        assertEquals("All questions must have correctOptionIndex in 0..3", 0, invalidCorrectOptionIndexCount)
        assertEquals("All questions must have non-blank explanation", 0, blankExplanationCount)
    }
}
