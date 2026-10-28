package com.example.ui.screens

import com.example.data.local.QuestionEntity
import java.util.Random

enum class ExamFinishReason {
    MANUAL,
    TIMEOUT
}

class ExamSession(
    val questions: List<QuestionEntity>,
    val durationSeconds: Long = questions.size * SECONDS_PER_QUESTION,
    private val timeSource: () -> Long = { System.currentTimeMillis() }
) {
    companion object {
        const val SECONDS_PER_QUESTION = 90L

        fun selectQuestions(
            pool: List<QuestionEntity>,
            targetCount: Int,
            random: Random = Random()
        ): List<QuestionEntity> {
            if (pool.isEmpty() || targetCount <= 0) return emptyList()
            val count = minOf(targetCount, pool.size)
            return pool.shuffled(random).take(count)
        }
    }

    val totalCount: Int = questions.size
    private val _selectedOptions = MutableList<Int?>(totalCount) { null }
    val selectedOptions: List<Int?> get() = _selectedOptions

    var currentIndex: Int = 0
        private set

    val startTimeMs: Long = timeSource()
    val deadlineMs: Long = startTimeMs + durationSeconds * 1000L

    var isFinished: Boolean = false
        private set

    var finishReason: ExamFinishReason? = null
        private set

    var endTimeMs: Long = startTimeMs
        private set

    val currentQuestion: QuestionEntity?
        get() = if (currentIndex in 0 until totalCount) questions[currentIndex] else null

    val currentSelectedOption: Int?
        get() = if (currentIndex in 0 until totalCount) _selectedOptions[currentIndex] else null

    val answeredCount: Int
        get() = _selectedOptions.count { it != null }

    val unansweredCount: Int
        get() = totalCount - answeredCount

    fun selectOption(optionIndex: Int) {
        if (isFinished) return
        if (currentIndex !in 0 until totalCount) return
        if (_selectedOptions[currentIndex] == optionIndex) {
            _selectedOptions[currentIndex] = null
        } else {
            _selectedOptions[currentIndex] = optionIndex
        }
    }

    fun selectOptionForQuestion(questionIndex: Int, optionIndex: Int) {
        if (isFinished) return
        if (questionIndex !in 0 until totalCount) return
        if (_selectedOptions[questionIndex] == optionIndex) {
            _selectedOptions[questionIndex] = null
        } else {
            _selectedOptions[questionIndex] = optionIndex
        }
    }

    fun goTo(index: Int) {
        if (totalCount == 0) return
        currentIndex = index.coerceIn(0, totalCount - 1)
    }

    fun next(): Boolean {
        if (currentIndex < totalCount - 1) {
            currentIndex++
            return true
        }
        return false
    }

    fun previous(): Boolean {
        if (currentIndex > 0) {
            currentIndex--
            return true
        }
        return false
    }

    fun isAnswered(index: Int): Boolean {
        if (index !in 0 until totalCount) return false
        return _selectedOptions[index] != null
    }

    fun checkTimeout(): Boolean {
        if (!isFinished && timeSource() >= deadlineMs) {
            finish(ExamFinishReason.TIMEOUT)
            return true
        }
        return false
    }

    fun finish(reason: ExamFinishReason) {
        if (isFinished) return
        isFinished = true
        finishReason = reason
        endTimeMs = minOf(timeSource(), deadlineMs)
    }

    fun remainingSeconds(): Long {
        if (totalCount == 0) return 0L
        val now = if (isFinished) endTimeMs else timeSource()
        val remainingMs = deadlineMs - now
        return maxOf(0L, (remainingMs + 999L) / 1000L)
    }

    fun elapsedSeconds(): Long {
        if (totalCount == 0) return 0L
        val end = if (isFinished) endTimeMs else minOf(timeSource(), deadlineMs)
        return maxOf(0L, (end - startTimeMs) / 1000L)
    }

    fun computeScore(): ExamScoreResult {
        var correct = 0
        var wrong = 0
        for (i in 0 until totalCount) {
            val selected = _selectedOptions[i]
            if (selected != null) {
                if (selected == questions[i].correctOptionIndex) {
                    correct++
                } else {
                    wrong++
                }
            }
        }
        return ExamScoring.score(correct = correct, wrong = wrong, total = totalCount)
    }
}
