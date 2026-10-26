package com.example.ui.screens

import com.example.data.local.QuestionEntity
import kotlin.random.Random

class PracticeRoundSession(
    private val random: Random = Random
) {
    private var questions: List<QuestionEntity> = emptyList()
    private var lastIdSet: Set<String> = emptySet()

    var currentIndex: Int = 0
        private set

    var isRoundFinished: Boolean = false
        private set

    val totalCount: Int
        get() = questions.size

    val questionNumber: Int
        get() = if (totalCount == 0 || isRoundFinished) 0 else currentIndex + 1

    val currentQuestion: QuestionEntity?
        get() = if (isRoundFinished || questions.isEmpty() || currentIndex !in questions.indices) {
            null
        } else {
            questions[currentIndex]
        }

    /**
     * Updates the questions for the current filter.
     * Returns true if a new round was started (because id set changed or forceReset),
     * or false if the id set is identical (no reset performed).
     */
    fun setQuestions(newQuestions: List<QuestionEntity>, forceReset: Boolean = false): Boolean {
        val newIdSet = newQuestions.map { it.id }.toSet()
        if (!forceReset && newIdSet == lastIdSet && newQuestions.isNotEmpty()) {
            return false
        }

        lastIdSet = newIdSet
        startRound(newQuestions)
        return true
    }

    /**
     * Restarts the round with the same questions, shuffling them again.
     */
    fun restart() {
        startRound(questions)
    }

    /**
     * Advances to the next question in the round.
     * Returns true if there is a next question, or false if the round is now finished.
     */
    fun next(): Boolean {
        if (isRoundFinished || questions.isEmpty()) {
            isRoundFinished = true
            return false
        }

        if (currentIndex + 1 < questions.size) {
            currentIndex++
            return true
        } else {
            isRoundFinished = true
            return false
        }
    }

    private fun startRound(items: List<QuestionEntity>) {
        questions = if (items.size <= 1) {
            items.toList()
        } else {
            items.shuffled(random)
        }
        currentIndex = 0
        isRoundFinished = false
    }
}
