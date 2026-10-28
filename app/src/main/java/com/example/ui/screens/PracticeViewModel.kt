package com.example.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.QuestionDao
import com.example.data.local.QuestionEntity
import com.example.util.sound.SoundEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PracticeUiState(
    val selectedGrade: Int = 11,
    val selectedTopic: String? = null,
    val selectedDifficulty: String? = null,
    val availableTopics: List<String> = emptyList(),
    val currentQuestion: QuestionEntity? = null,
    val selectedOptionIndex: Int? = null,
    val isAnswerLocked: Boolean = false,
    val totalQuestionsInBank: Int = 0,
    val filterMatchingCount: Int = 0,
    val currentQuestionIndexInRound: Int = 0,
    val isRoundFinished: Boolean = false,
    val isFilterEmpty: Boolean = false
)

class PracticeViewModel(
    private val questionDao: QuestionDao,
    val roundSession: PracticeRoundSession = PracticeRoundSession()
) : ViewModel() {

    private val _selectedGrade = MutableStateFlow(11)
    private val _selectedTopic = MutableStateFlow<String?>(null)
    private val _selectedDifficulty = MutableStateFlow<String?>(null)

    private val _selectedOptionIndex = MutableStateFlow<Int?>(null)
    private val _isAnswerLocked = MutableStateFlow(false)
    private val _roundStateVersion = MutableStateFlow(0)

    private val _soundEvents = MutableSharedFlow<SoundEvent>(extraBufferCapacity = 1)
    val soundEvents: SharedFlow<SoundEvent> = _soundEvents.asSharedFlow()

    val totalCountFlow = questionDao.getAllQuestions().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val availableTopics: StateFlow<List<String>> = _selectedGrade.flatMapLatest { grade ->
        questionDao.getTopicsForGrade(grade)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    private val filteredQuestions: StateFlow<List<QuestionEntity>> = combine(
        _selectedGrade,
        _selectedTopic,
        _selectedDifficulty
    ) { grade, topic, difficulty ->
        Triple(grade, topic, difficulty)
    }.flatMapLatest { (grade, topic, difficulty) ->
        questionDao.getFilteredQuestions(grade, topic, difficulty)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val uiState: StateFlow<PracticeUiState> = combine(
        combine(_selectedGrade, _selectedTopic, _selectedDifficulty) { g, t, d -> Triple(g, t, d) },
        availableTopics,
        filteredQuestions,
        combine(_selectedOptionIndex, _isAnswerLocked, _roundStateVersion) { opt, locked, _ -> Pair(opt, locked) },
        totalCountFlow
    ) { (grade, topic, difficulty), topics, questions, (optIdx, locked), allQuestions ->
        PracticeUiState(
            selectedGrade = grade,
            selectedTopic = topic,
            selectedDifficulty = difficulty,
            availableTopics = topics,
            currentQuestion = roundSession.currentQuestion,
            selectedOptionIndex = optIdx,
            isAnswerLocked = locked,
            totalQuestionsInBank = allQuestions.size,
            filterMatchingCount = roundSession.totalCount,
            currentQuestionIndexInRound = roundSession.questionNumber,
            isRoundFinished = roundSession.isRoundFinished,
            isFilterEmpty = allQuestions.isNotEmpty() && questions.isEmpty()
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        PracticeUiState()
    )

    init {
        viewModelScope.launch {
            filteredQuestions.collect { list ->
                val wasUpdated = roundSession.setQuestions(list)
                if (wasUpdated) {
                    resetAnswer()
                    _roundStateVersion.value++
                }
            }
        }
    }

    fun onGradeSelected(grade: Int) {
        if (_selectedGrade.value != grade) {
            _selectedGrade.value = grade
            _selectedTopic.value = null
            resetAnswer()
        }
    }

    fun onTopicSelected(topic: String?) {
        if (_selectedTopic.value != topic) {
            _selectedTopic.value = topic
            resetAnswer()
        }
    }

    fun onDifficultySelected(difficulty: String?) {
        if (_selectedDifficulty.value != difficulty) {
            _selectedDifficulty.value = difficulty
            resetAnswer()
        }
    }

    fun selectOption(index: Int) {
        if (!_isAnswerLocked.value) {
            _selectedOptionIndex.value = index
            _isAnswerLocked.value = true
            val currentQ = roundSession.currentQuestion
            if (currentQ != null) {
                val event = if (index == currentQ.correctOptionIndex) {
                    SoundEvent.ANSWER_CORRECT
                } else {
                    SoundEvent.ANSWER_WRONG
                }
                _soundEvents.tryEmit(event)
            }
        }
    }

    fun nextQuestion() {
        resetAnswer()
        roundSession.next()
        _roundStateVersion.value++
    }

    fun restartRound() {
        resetAnswer()
        roundSession.restart()
        _roundStateVersion.value++
    }

    private fun resetAnswer() {
        _selectedOptionIndex.value = null
        _isAnswerLocked.value = false
    }
}

class PracticeViewModelFactory(
    private val questionDao: QuestionDao,
    private val roundSession: PracticeRoundSession = PracticeRoundSession()
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PracticeViewModel::class.java)) {
            return PracticeViewModel(questionDao, roundSession) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
