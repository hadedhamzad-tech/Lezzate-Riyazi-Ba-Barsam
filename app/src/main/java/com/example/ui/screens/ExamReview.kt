package com.example.ui.screens

import com.example.data.local.QuestionEntity

enum class QuestionReviewStatus {
    CORRECT,
    WRONG,
    BLANK
}

enum class ReviewFilter {
    ALL,
    WRONG,
    BLANK,
    CORRECT
}

data class ExamReviewItem(
    val number: Int,
    val question: QuestionEntity,
    val selectedOptionIndex: Int?,
    val status: QuestionReviewStatus
)

data class ReviewFilterCounts(
    val all: Int,
    val wrong: Int,
    val blank: Int,
    val correct: Int
)

object ExamReview {

    fun buildReviewItems(
        questions: List<QuestionEntity>,
        selectedOptions: List<Int?>
    ): List<ExamReviewItem> {
        val result = ArrayList<ExamReviewItem>(questions.size)
        for (i in questions.indices) {
            val q = questions[i]
            val selected = selectedOptions.getOrNull(i)
            val status = when {
                selected == null -> QuestionReviewStatus.BLANK
                selected == q.correctOptionIndex -> QuestionReviewStatus.CORRECT
                else -> QuestionReviewStatus.WRONG
            }
            result.add(
                ExamReviewItem(
                    number = i + 1,
                    question = q,
                    selectedOptionIndex = selected,
                    status = status
                )
            )
        }
        return result
    }

    fun filterReviewItems(
        items: List<ExamReviewItem>,
        filter: ReviewFilter
    ): List<ExamReviewItem> {
        return when (filter) {
            ReviewFilter.ALL -> items
            ReviewFilter.WRONG -> items.filter { it.status == QuestionReviewStatus.WRONG }
            ReviewFilter.BLANK -> items.filter { it.status == QuestionReviewStatus.BLANK }
            ReviewFilter.CORRECT -> items.filter { it.status == QuestionReviewStatus.CORRECT }
        }
    }

    fun computeFilterCounts(items: List<ExamReviewItem>): ReviewFilterCounts {
        var wrong = 0
        var blank = 0
        var correct = 0
        for (item in items) {
            when (item.status) {
                QuestionReviewStatus.WRONG -> wrong++
                QuestionReviewStatus.BLANK -> blank++
                QuestionReviewStatus.CORRECT -> correct++
            }
        }
        return ReviewFilterCounts(
            all = items.size,
            wrong = wrong,
            blank = blank,
            correct = correct
        )
    }

    fun getNextButtonLabel(currentIndex: Int, totalCount: Int): String {
        return if (currentIndex >= totalCount - 1) "پایان آزمون" else "بعدی"
    }
}
