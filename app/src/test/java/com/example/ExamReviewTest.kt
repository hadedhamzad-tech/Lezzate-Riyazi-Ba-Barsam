package com.example

import com.example.data.local.QuestionEntity
import com.example.ui.screens.ExamReview
import com.example.ui.screens.QuestionReviewStatus
import com.example.ui.screens.ReviewFilter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExamReviewTest {

    private fun createQuestion(id: String, correctOptionIndex: Int = 0) = QuestionEntity(
        id = id,
        grade = 11,
        difficulty = "متوسط",
        topic = "مبحث آزمایشی",
        questionText = "متن سؤال $id",
        option1 = "گزینه 1",
        option2 = "گزینه 2",
        option3 = "گزینه 3",
        option4 = "گزینه 4",
        correctOptionIndex = correctOptionIndex,
        explanation = "توضیح $id"
    )

    @Test
    fun testStatusesComputedCorrectlyForCorrectWrongAndBlank() {
        val q1 = createQuestion("q1", correctOptionIndex = 0)
        val q2 = createQuestion("q2", correctOptionIndex = 1)
        val q3 = createQuestion("q3", correctOptionIndex = 2)

        val questions = listOf(q1, q2, q3)
        // q1: answered correctly (0)
        // q2: answered wrongly (3 instead of 1)
        // q3: left blank (null)
        val answers = listOf(0, 3, null)

        val items = ExamReview.buildReviewItems(questions, answers)

        assertEquals(3, items.size)

        assertEquals(1, items[0].number)
        assertEquals("q1", items[0].question.id)
        assertEquals(0, items[0].selectedOptionIndex)
        assertEquals(QuestionReviewStatus.CORRECT, items[0].status)

        assertEquals(2, items[1].number)
        assertEquals("q2", items[1].question.id)
        assertEquals(3, items[1].selectedOptionIndex)
        assertEquals(QuestionReviewStatus.WRONG, items[1].status)

        assertEquals(3, items[2].number)
        assertEquals("q3", items[2].question.id)
        assertEquals(null, items[2].selectedOptionIndex)
        assertEquals(QuestionReviewStatus.BLANK, items[2].status)
    }

    @Test
    fun testListPreservesOriginalOrderAndOriginalNumbers() {
        val questions = (1..6).map { createQuestion("q$it", correctOptionIndex = 0) }
        val answers = listOf(0, 1, null, 0, 2, null)

        val items = ExamReview.buildReviewItems(questions, answers)

        assertEquals(6, items.size)
        for (i in 0 until 6) {
            assertEquals(i + 1, items[i].number)
            assertEquals("q${i + 1}", items[i].question.id)
        }
    }

    @Test
    fun testEachFilterReturnsExactlyRightItemsWithOriginalNumbers() {
        val questions = (1..5).map { createQuestion("q$it", correctOptionIndex = 0) }
        // q1: correct (0), q2: wrong (1), q3: blank (null), q4: correct (0), q5: wrong (2)
        val answers = listOf(0, 1, null, 0, 2)

        val allItems = ExamReview.buildReviewItems(questions, answers)

        // ALL filter
        val all = ExamReview.filterReviewItems(allItems, ReviewFilter.ALL)
        assertEquals(5, all.size)

        // WRONG filter
        val wrong = ExamReview.filterReviewItems(allItems, ReviewFilter.WRONG)
        assertEquals(2, wrong.size)
        assertEquals(listOf(2, 5), wrong.map { it.number })
        assertTrue(wrong.all { it.status == QuestionReviewStatus.WRONG })

        // BLANK filter
        val blank = ExamReview.filterReviewItems(allItems, ReviewFilter.BLANK)
        assertEquals(1, blank.size)
        assertEquals(3, blank[0].number)
        assertEquals(QuestionReviewStatus.BLANK, blank[0].status)

        // CORRECT filter
        val correct = ExamReview.filterReviewItems(allItems, ReviewFilter.CORRECT)
        assertEquals(2, correct.size)
        assertEquals(listOf(1, 4), correct.map { it.number })
        assertTrue(correct.all { it.status == QuestionReviewStatus.CORRECT })
    }

    @Test
    fun testFilterCountsAreConsistent() {
        val questions = (1..10).map { createQuestion("q$it", correctOptionIndex = 0) }
        // 5 correct, 3 wrong, 2 blank
        val answers = listOf(0, 0, 0, 0, 0, 1, 2, 3, null, null)

        val items = ExamReview.buildReviewItems(questions, answers)
        val counts = ExamReview.computeFilterCounts(items)

        assertEquals(10, counts.all)
        assertEquals(5, counts.correct)
        assertEquals(3, counts.wrong)
        assertEquals(2, counts.blank)
        assertEquals(counts.all, counts.correct + counts.wrong + counts.blank)
    }

    @Test
    fun testExamWithAllBlankAnswers() {
        val questions = (1..4).map { createQuestion("q$it", correctOptionIndex = 1) }
        val answers = listOf<Int?>(null, null, null, null)

        val items = ExamReview.buildReviewItems(questions, answers)
        val counts = ExamReview.computeFilterCounts(items)

        assertEquals(4, counts.all)
        assertEquals(0, counts.correct)
        assertEquals(0, counts.wrong)
        assertEquals(4, counts.blank)

        assertTrue(ExamReview.filterReviewItems(items, ReviewFilter.CORRECT).isEmpty())
        assertTrue(ExamReview.filterReviewItems(items, ReviewFilter.WRONG).isEmpty())
        assertEquals(4, ExamReview.filterReviewItems(items, ReviewFilter.BLANK).size)
    }

    @Test
    fun testExamWithOneQuestion() {
        val singleQuestion = listOf(createQuestion("q1", correctOptionIndex = 2))

        // When answered correctly
        val itemsCorrect = ExamReview.buildReviewItems(singleQuestion, listOf(2))
        assertEquals(1, itemsCorrect.size)
        assertEquals(1, itemsCorrect[0].number)
        assertEquals(QuestionReviewStatus.CORRECT, itemsCorrect[0].status)

        // When answered wrongly
        val itemsWrong = ExamReview.buildReviewItems(singleQuestion, listOf(1))
        assertEquals(1, itemsWrong.size)
        assertEquals(QuestionReviewStatus.WRONG, itemsWrong[0].status)

        // When blank
        val itemsBlank = ExamReview.buildReviewItems(singleQuestion, listOf(null))
        assertEquals(1, itemsBlank.size)
        assertEquals(QuestionReviewStatus.BLANK, itemsBlank[0].status)
    }

    @Test
    fun testEmptyExamList() {
        val items = ExamReview.buildReviewItems(emptyList(), emptyList())
        assertTrue(items.isEmpty())

        val counts = ExamReview.computeFilterCounts(items)
        assertEquals(0, counts.all)
        assertEquals(0, counts.correct)
        assertEquals(0, counts.wrong)
        assertEquals(0, counts.blank)

        assertTrue(ExamReview.filterReviewItems(items, ReviewFilter.ALL).isEmpty())
        assertTrue(ExamReview.filterReviewItems(items, ReviewFilter.WRONG).isEmpty())
        assertTrue(ExamReview.filterReviewItems(items, ReviewFilter.BLANK).isEmpty())
        assertTrue(ExamReview.filterReviewItems(items, ReviewFilter.CORRECT).isEmpty())
    }

    @Test
    fun testGetNextButtonLabelForFirstMiddleLastAndOneQuestionExam() {
        // Multi-question exam (totalCount = 5)
        // First question (index = 0)
        assertEquals("بعدی", ExamReview.getNextButtonLabel(currentIndex = 0, totalCount = 5))

        // Middle question (index = 2)
        assertEquals("بعدی", ExamReview.getNextButtonLabel(currentIndex = 2, totalCount = 5))

        // Penultimate question (index = 3)
        assertEquals("بعدی", ExamReview.getNextButtonLabel(currentIndex = 3, totalCount = 5))

        // Last question (index = 4)
        assertEquals("پایان آزمون", ExamReview.getNextButtonLabel(currentIndex = 4, totalCount = 5))

        // One-question exam (totalCount = 1)
        // Index 0 is both first and last question
        assertEquals("پایان آزمون", ExamReview.getNextButtonLabel(currentIndex = 0, totalCount = 1))
    }
}
