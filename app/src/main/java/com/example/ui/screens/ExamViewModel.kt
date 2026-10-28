package com.example.ui.screens

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.QuestionDao
import com.example.data.local.QuestionEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class ExamScreenStage {
    SETUP,
    IN_PROGRESS,
    RESULT,
    REVIEW
}

data class ExamUiState(
    val stage: ExamScreenStage = ExamScreenStage.SETUP,
    val selectedGrade: Int = 11,
    val selectedDifficulty: String? = null,
    val selectedQuestionCount: Int = 20,
    val availableQuestionsCount: Int = 0,
    val currentQuestionIndex: Int = 0,
    val totalQuestionsInExam: Int = 0,
    val currentQuestion: QuestionEntity? = null,
    val selectedOptionIndex: Int? = null,
    val answeredFlags: List<Boolean> = emptyList(),
    val remainingSeconds: Long = 0L,
    val formattedRemainingTime: String = "00:00",
    val unansweredCount: Int = 0,
    val isFinishDialogVisible: Boolean = false,
    val isAbandonDialogVisible: Boolean = false,
    val scoreResult: ExamScoreResult? = null,
    val elapsedSeconds: Long = 0L,
    val finishReason: ExamFinishReason? = null,
    val reviewItems: List<ExamReviewItem> = emptyList(),
    val filteredReviewItems: List<ExamReviewItem> = emptyList(),
    val activeReviewFilter: ReviewFilter = ReviewFilter.ALL,
    val reviewFilterCounts: ReviewFilterCounts = ReviewFilterCounts(0, 0, 0, 0)
)

class ExamViewModel(
    private val questionDao: QuestionDao,
    private val timeSource: () -> Long = { SystemClock.elapsedRealtime() }
) : ViewModel() {

    private val _stage = MutableStateFlow(ExamScreenStage.SETUP)
    private val _selectedGrade = MutableStateFlow(11)
    private val _selectedDifficulty = MutableStateFlow<String?>(null)
    private val _selectedQuestionCount = MutableStateFlow(20)

    private val _isFinishDialogVisible = MutableStateFlow(false)
    private val _isAbandonDialogVisible = MutableStateFlow(false)
    private val _activeReviewFilter = MutableStateFlow(ReviewFilter.ALL)

    var currentSession: ExamSession? = null
        private set

    private val _sessionVersion = MutableStateFlow(0)
    private var timerJob: Job? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    val availableQuestions: StateFlow<List<QuestionEntity>> = combine(
        _selectedGrade,
        _selectedDifficulty
    ) { grade, difficulty ->
        Pair(grade, difficulty)
    }.flatMapLatest { (grade, difficulty) ->
        questionDao.getFilteredQuestions(grade = grade, topic = null, difficulty = difficulty)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val uiState: StateFlow<ExamUiState> = combine(
        _stage,
        _selectedGrade,
        _selectedDifficulty,
        _selectedQuestionCount,
        availableQuestions,
        _sessionVersion,
        _isFinishDialogVisible,
        _isAbandonDialogVisible,
        _activeReviewFilter
    ) { args: Array<Any?> ->
        val stage = args[0] as ExamScreenStage
        val grade = args[1] as Int
        val diff = args[2] as String?
        val count = args[3] as Int
        @Suppress("UNCHECKED_CAST")
        val pool = args[4] as List<QuestionEntity>
        val isFinishDialog = args[6] as Boolean
        val isAbandonDialog = args[7] as Boolean
        val activeFilter = args[8] as ReviewFilter

        val session = currentSession
        if (session != null && stage != ExamScreenStage.SETUP) {
            val remaining = session.remainingSeconds()
            val answeredList = (0 until session.totalCount).map { session.isAnswered(it) }

            val allReviewItems = if (session.isFinished) {
                ExamReview.buildReviewItems(session.questions, session.selectedOptions)
            } else {
                emptyList()
            }
            val filterCounts = ExamReview.computeFilterCounts(allReviewItems)
            val filteredItems = ExamReview.filterReviewItems(allReviewItems, activeFilter)

            ExamUiState(
                stage = stage,
                selectedGrade = grade,
                selectedDifficulty = diff,
                selectedQuestionCount = count,
                availableQuestionsCount = pool.size,
                currentQuestionIndex = session.currentIndex,
                totalQuestionsInExam = session.totalCount,
                currentQuestion = session.currentQuestion,
                selectedOptionIndex = session.currentSelectedOption,
                answeredFlags = answeredList,
                remainingSeconds = remaining,
                formattedRemainingTime = ExamFormatters.formatTimeMmSs(remaining),
                unansweredCount = session.unansweredCount,
                isFinishDialogVisible = isFinishDialog,
                isAbandonDialogVisible = isAbandonDialog,
                scoreResult = if (session.isFinished) session.computeScore() else null,
                elapsedSeconds = session.elapsedSeconds(),
                finishReason = session.finishReason,
                reviewItems = allReviewItems,
                filteredReviewItems = filteredItems,
                activeReviewFilter = activeFilter,
                reviewFilterCounts = filterCounts
            )
        } else {
            ExamUiState(
                stage = ExamScreenStage.SETUP,
                selectedGrade = grade,
                selectedDifficulty = diff,
                selectedQuestionCount = count,
                availableQuestionsCount = pool.size,
                isFinishDialogVisible = false,
                isAbandonDialogVisible = false
            )
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ExamUiState()
    )

    fun onGradeSelected(grade: Int) {
        if (_stage.value != ExamScreenStage.SETUP) return
        _selectedGrade.value = grade
    }

    fun onDifficultySelected(difficulty: String?) {
        if (_stage.value != ExamScreenStage.SETUP) return
        _selectedDifficulty.value = difficulty
    }

    fun onQuestionCountSelected(count: Int) {
        if (_stage.value != ExamScreenStage.SETUP) return
        _selectedQuestionCount.value = count
    }

    fun startExam() {
        val pool = availableQuestions.value
        if (pool.isEmpty()) return
        val targetCount = _selectedQuestionCount.value
        val examQuestions = ExamSession.selectQuestions(pool, targetCount)
        if (examQuestions.isEmpty()) return

        val session = ExamSession(
            questions = examQuestions,
            durationSeconds = examQuestions.size * ExamSession.SECONDS_PER_QUESTION,
            timeSource = timeSource
        )
        currentSession = session
        _stage.value = ExamScreenStage.IN_PROGRESS
        _isFinishDialogVisible.value = false
        _isAbandonDialogVisible.value = false
        _activeReviewFilter.value = ReviewFilter.ALL
        _sessionVersion.value++

        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(500L)
                val session = currentSession ?: break
                if (session.checkTimeout()) {
                    _stage.value = ExamScreenStage.RESULT
                    _isFinishDialogVisible.value = false
                    _isAbandonDialogVisible.value = false
                    _sessionVersion.value++
                    break
                }
                _sessionVersion.value++
            }
        }
    }

    fun selectOption(optionIndex: Int) {
        val session = currentSession ?: return
        if (session.isFinished) return
        session.selectOption(optionIndex)
        _sessionVersion.value++
    }

    fun goToQuestion(index: Int) {
        val session = currentSession ?: return
        session.goTo(index)
        _sessionVersion.value++
    }

    fun nextQuestion() {
        val session = currentSession ?: return
        session.next()
        _sessionVersion.value++
    }

    fun previousQuestion() {
        val session = currentSession ?: return
        session.previous()
        _sessionVersion.value++
    }

    fun requestFinishDialog(show: Boolean) {
        _isFinishDialogVisible.value = show
    }

    fun requestAbandonDialog(show: Boolean) {
        _isAbandonDialogVisible.value = show
    }

    fun confirmFinishExam() {
        _isFinishDialogVisible.value = false
        val session = currentSession ?: return
        session.finish(ExamFinishReason.MANUAL)
        timerJob?.cancel()
        _stage.value = ExamScreenStage.RESULT
        _sessionVersion.value++
    }

    fun confirmAbandonExam() {
        _isAbandonDialogVisible.value = false
        timerJob?.cancel()
        currentSession = null
        _stage.value = ExamScreenStage.SETUP
        _activeReviewFilter.value = ReviewFilter.ALL
        _sessionVersion.value++
    }

    fun startNewExam() {
        timerJob?.cancel()
        currentSession = null
        _stage.value = ExamScreenStage.SETUP
        _activeReviewFilter.value = ReviewFilter.ALL
        _sessionVersion.value++
    }

    fun openReview() {
        val session = currentSession ?: return
        if (!session.isFinished) return
        _stage.value = ExamScreenStage.REVIEW
        _sessionVersion.value++
    }

    fun closeReview() {
        _stage.value = ExamScreenStage.RESULT
        _sessionVersion.value++
    }

    fun setReviewFilter(filter: ReviewFilter) {
        _activeReviewFilter.value = filter
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}

class ExamViewModelFactory(
    private val questionDao: QuestionDao,
    private val timeSource: () -> Long = { SystemClock.elapsedRealtime() }
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExamViewModel::class.java)) {
            return ExamViewModel(questionDao, timeSource) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
