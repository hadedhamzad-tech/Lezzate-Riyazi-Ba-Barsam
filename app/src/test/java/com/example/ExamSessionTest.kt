package com.example

import com.example.data.local.QuestionEntity
import com.example.ui.screens.ExamFinishReason
import com.example.ui.screens.ExamSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Random

class ExamSessionTest {

    private fun createQuestion(id: String, grade: Int = 11, difficulty: String = "متوسط", correctIndex: Int = 0) =
        QuestionEntity(
            id = id,
            grade = grade,
            difficulty = difficulty,
            topic = "مبحث آزمایشی",
            questionText = "متن سؤال $id",
            option1 = "گزینه 1",
            option2 = "گزینه 2",
            option3 = "گزینه 3",
            option4 = "گزینه 4",
            correctOptionIndex = correctIndex,
            explanation = "توضیح $id"
        )

    @Test
    fun testSelectionHasExactCountAndNoDuplicates() {
        val pool = (1..20).map { createQuestion("q$it") }

        // Request 10 from 20
        val selected10 = ExamSession.selectQuestions(pool, targetCount = 10, random = Random(42))
        assertEquals(10, selected10.size)
        assertEquals(10, selected10.map { it.id }.toSet().size)

        // Request 30 from 20 -> should cap at min(count, available) = 20
        val selectedAll = ExamSession.selectQuestions(pool, targetCount = 30, random = Random(42))
        assertEquals(20, selectedAll.size)
        assertEquals(20, selectedAll.map { it.id }.toSet().size)
    }

    @Test
    fun testAllSelectedQuestionsMatchFilter() {
        val pool = (1..15).map { createQuestion("q$it", grade = 11, difficulty = "کنکوری") }
        val selected = ExamSession.selectQuestions(pool, targetCount = 10, random = Random(123))

        for (q in selected) {
            assertEquals(11, q.grade)
            assertEquals("کنکوری", q.difficulty)
        }
    }

    @Test
    fun testSelectingChangingAndClearingAnswer() {
        val questions = (1..3).map { createQuestion("q$it") }
        val session = ExamSession(questions)

        assertNull(session.currentSelectedOption)
        assertEquals(0, session.answeredCount)
        assertEquals(3, session.unansweredCount)

        // Select option 1
        session.selectOption(1)
        assertEquals(1, session.currentSelectedOption)
        assertEquals(1, session.answeredCount)
        assertTrue(session.isAnswered(0))

        // Change to option 2
        session.selectOption(2)
        assertEquals(2, session.currentSelectedOption)
        assertEquals(1, session.answeredCount)

        // Tap option 2 again -> clears it to null
        session.selectOption(2)
        assertNull(session.currentSelectedOption)
        assertEquals(0, session.answeredCount)
        assertFalse(session.isAnswered(0))
    }

    @Test
    fun testNavigationNeverGoesOutOfRangeAndJumpingWorks() {
        val questions = (1..5).map { createQuestion("q$it") }
        val session = ExamSession(questions)

        assertEquals(0, session.currentIndex)
        assertFalse(session.previous()) // Cannot go below 0
        assertEquals(0, session.currentIndex)

        assertTrue(session.next()) // to 1
        assertEquals(1, session.currentIndex)

        session.goTo(4)
        assertEquals(4, session.currentIndex)
        assertFalse(session.next()) // Cannot go above 4
        assertEquals(4, session.currentIndex)

        // Clamping on invalid goTo indices
        session.goTo(-10)
        assertEquals(0, session.currentIndex)
        session.goTo(100)
        assertEquals(4, session.currentIndex)
    }

    @Test
    fun testFinishManually() {
        val questions = (1..3).map { createQuestion("q$it", correctIndex = 0) }
        val session = ExamSession(questions)

        session.selectOption(0)
        session.finish(ExamFinishReason.MANUAL)

        assertTrue(session.isFinished)
        assertEquals(ExamFinishReason.MANUAL, session.finishReason)
    }

    @Test
    fun testFinishingAutomaticallyWhenTimePassesDeadline() {
        var currentTime = 100000L
        val questions = (1..2).map { createQuestion("q$it") }
        // 2 questions * 90s = 180s = 180,000ms duration -> deadline = 280,000ms
        val session = ExamSession(
            questions = questions,
            durationSeconds = 180L,
            timeSource = { currentTime }
        )

        assertFalse(session.isFinished)
        assertEquals(180L, session.remainingSeconds())

        // Advance time to before deadline
        currentTime += 100000L // 200,000ms
        assertFalse(session.checkTimeout())
        assertFalse(session.isFinished)
        assertEquals(80L, session.remainingSeconds())

        // Advance time past deadline
        currentTime = 280001L
        assertTrue(session.checkTimeout())
        assertTrue(session.isFinished)
        assertEquals(ExamFinishReason.TIMEOUT, session.finishReason)
        assertEquals(0L, session.remainingSeconds())
    }

    @Test
    fun testAfterFinishAnswersCanNoLongerChange() {
        val questions = (1..2).map { createQuestion("q$it") }
        val session = ExamSession(questions)

        session.selectOption(0)
        assertEquals(0, session.currentSelectedOption)

        session.finish(ExamFinishReason.MANUAL)

        // Attempting to select, change or clear after finish should be ignored
        session.selectOption(2)
        assertEquals(0, session.currentSelectedOption)

        session.selectOption(0)
        assertEquals(0, session.currentSelectedOption)
    }

    @Test
    fun testRemainingTimeNeverGoesBelowZero() {
        var currentTime = 1000L
        val questions = (1..2).map { createQuestion("q$it") }
        val session = ExamSession(
            questions = questions,
            durationSeconds = 10L,
            timeSource = { currentTime }
        )

        // Advance far into future
        currentTime = 1000000L
        assertEquals(0L, session.remainingSeconds())
    }

    @Test
    fun testScoreComputationMatchesAnswers() {
        // 3 questions: q1 correctIndex 0, q2 correctIndex 1, q3 correctIndex 2
        val questions = listOf(
            createQuestion("q1", correctIndex = 0),
            createQuestion("q2", correctIndex = 1),
            createQuestion("q3", correctIndex = 2)
        )
        val session = ExamSession(questions)

        // q1: answered correctly (0)
        session.goTo(0)
        session.selectOption(0)

        // q2: answered wrongly (0 instead of 1)
        session.goTo(1)
        session.selectOption(0)

        // q3: left blank (no option selected)

        session.finish(ExamFinishReason.MANUAL)
        val score = session.computeScore()

        assertEquals(3, score.totalQuestions)
        assertEquals(1, score.correctCount)
        assertEquals(1, score.wrongCount)
        assertEquals(1, score.blankCount)
        // raw = 3*1 - 1 = 2
        assertEquals(2, score.rawScore)
        assertEquals(9, score.maxScore)
        assertEquals((2.0 / 9.0) * 100.0, score.percentage, 0.01)
    }

    @Test
    fun testEdgeCaseZeroAvailableQuestions() {
        val emptyPool = emptyList<QuestionEntity>()
        val selected = ExamSession.selectQuestions(emptyPool, targetCount = 10)
        assertTrue(selected.isEmpty())

        val session = ExamSession(emptyList())
        assertEquals(0, session.totalCount)
        assertNull(session.currentQuestion)
        assertEquals(0L, session.remainingSeconds())
        assertEquals(0L, session.elapsedSeconds())
        val score = session.computeScore()
        assertEquals(0, score.totalQuestions)
        assertEquals(0, score.rawScore)
    }

    @Test
    fun testEdgeCaseOneAvailableQuestion() {
        val singlePool = listOf(createQuestion("q1", correctIndex = 3))
        val selected = ExamSession.selectQuestions(singlePool, targetCount = 20)
        assertEquals(1, selected.size)

        val session = ExamSession(selected)
        assertEquals(1, session.totalCount)
        assertNotNull(session.currentQuestion)

        // Answer correctly
        session.selectOption(3)
        session.finish(ExamFinishReason.MANUAL)

        val score = session.computeScore()
        assertEquals(1, score.totalQuestions)
        assertEquals(1, score.correctCount)
        assertEquals(0, score.wrongCount)
        assertEquals(3, score.rawScore)
        assertEquals(3, score.maxScore)
        assertEquals(100.0, score.percentage, 0.001)
    }
}
