package com.example

import com.example.data.local.QuestionEntity
import com.example.ui.screens.PracticeRoundSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PracticeRoundSessionTest {

    private fun createQuestion(id: String) = QuestionEntity(
        id = id,
        grade = 11,
        difficulty = "متوسط",
        topic = "مبحث",
        questionText = "متن $id",
        option1 = "۱",
        option2 = "۲",
        option3 = "۳",
        option4 = "۴",
        correctOptionIndex = 0,
        explanation = "توضیح $id"
    )

    @Test
    fun testEveryQuestionAppearsExactlyOnceInRound() {
        val questions = (1..5).map { createQuestion("q$it") }
        val session = PracticeRoundSession()
        session.setQuestions(questions)

        val seen = mutableListOf<String>()
        var count = 0
        while (!session.isRoundFinished && count < 10) {
            val current = session.currentQuestion
            assertNotNull(current)
            seen.add(current!!.id)
            session.next()
            count++
        }

        assertEquals(5, seen.size)
        assertEquals(5, seen.toSet().size)
        assertEquals(questions.map { it.id }.toSet(), seen.toSet())
    }

    @Test
    fun testRoundEndsAfterNQuestions() {
        val questions = (1..4).map { createQuestion("q$it") }
        val session = PracticeRoundSession()
        session.setQuestions(questions)

        assertEquals(4, session.totalCount)
        assertEquals(1, session.questionNumber)

        assertTrue(session.next()) // to 2
        assertEquals(2, session.questionNumber)

        assertTrue(session.next()) // to 3
        assertEquals(3, session.questionNumber)

        assertTrue(session.next()) // to 4
        assertEquals(4, session.questionNumber)

        assertFalse(session.next()) // finishes round
        assertTrue(session.isRoundFinished)
        assertNull(session.currentQuestion)
        assertEquals(0, session.questionNumber)
    }

    @Test
    fun testRestartGivesNewRoundWithAllNQuestionsAgain() {
        val questions = (1..3).map { createQuestion("q$it") }
        val session = PracticeRoundSession()
        session.setQuestions(questions)

        // Consume round
        session.next()
        session.next()
        session.next()
        assertTrue(session.isRoundFinished)

        // Restart
        session.restart()
        assertFalse(session.isRoundFinished)
        assertEquals(1, session.questionNumber)
        assertEquals(3, session.totalCount)

        val seenAfterRestart = mutableListOf<String>()
        while (!session.isRoundFinished) {
            val q = session.currentQuestion
            assertNotNull(q)
            seenAfterRestart.add(q!!.id)
            session.next()
        }

        assertEquals(3, seenAfterRestart.size)
        assertEquals(setOf("q1", "q2", "q3"), seenAfterRestart.toSet())
    }

    @Test
    fun testSameIdSetReemittedDoesNotReset() {
        val questions = (1..3).map { createQuestion("q$it") }
        val session = PracticeRoundSession()
        session.setQuestions(questions)

        session.next() // advance to 2nd question
        assertEquals(2, session.questionNumber)
        val currentQ = session.currentQuestion
        assertNotNull(currentQ)

        // Room re-emits a new list with same IDs (e.g. reversed)
        val reemitted = questions.reversed()
        val resetTriggered = session.setQuestions(reemitted)

        assertFalse("Same id set should not trigger reset", resetTriggered)
        assertEquals("Current question index must stay on 2", 2, session.questionNumber)
        assertEquals("Current question must not change", currentQ?.id, session.currentQuestion?.id)
        assertFalse(session.isRoundFinished)
    }

    @Test
    fun testChangedIdSetOrFilterResets() {
        val questionsA = (1..3).map { createQuestion("q$it") }
        val session = PracticeRoundSession()
        session.setQuestions(questionsA)
        session.next() // at question 2

        val questionsB = (4..6).map { createQuestion("q$it") }
        val resetTriggered = session.setQuestions(questionsB)

        assertTrue("Changed ID set must trigger reset", resetTriggered)
        assertEquals(1, session.questionNumber)
        assertEquals(3, session.totalCount)
        assertFalse(session.isRoundFinished)
        assertTrue(session.currentQuestion?.id in setOf("q4", "q5", "q6"))
    }

    @Test
    fun testEdgeCaseNIsOne() {
        val single = listOf(createQuestion("q1"))
        val session = PracticeRoundSession()
        session.setQuestions(single)

        assertEquals(1, session.totalCount)
        assertEquals(1, session.questionNumber)
        assertEquals("q1", session.currentQuestion?.id)
        assertFalse(session.isRoundFinished)

        // Next finishes the 1-question round
        val hasNext = session.next()
        assertFalse(hasNext)
        assertTrue(session.isRoundFinished)
        assertNull(session.currentQuestion)

        // Restart works
        session.restart()
        assertFalse(session.isRoundFinished)
        assertEquals(1, session.questionNumber)
        assertEquals("q1", session.currentQuestion?.id)
    }

    @Test
    fun testEdgeCaseNIsZero() {
        val empty = emptyList<QuestionEntity>()
        val session = PracticeRoundSession()
        session.setQuestions(empty)

        assertEquals(0, session.totalCount)
        assertEquals(0, session.questionNumber)
        assertNull(session.currentQuestion)

        val hasNext = session.next()
        assertFalse(hasNext)
        assertTrue(session.isRoundFinished)
    }
}
