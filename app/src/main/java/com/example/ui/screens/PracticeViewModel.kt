package com.example.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.QuestionDao
import com.example.data.local.QuestionEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

data class PracticeUiState(
    val selectedGrade: Int = 11,
    val selectedTopic: String? = null,
    val selectedDifficulty: String? = null,
    val availableTopics: List<String> = emptyList(),
    val currentQuestion: QuestionEntity? = null,
    val selectedOptionIndex: Int? = null,
    val isAnswerLocked: Boolean = false,
    val totalQuestionsCount: Int = 0,
    val isFilterEmpty: Boolean = false
)

class PracticeViewModel(
    private val questionDao: QuestionDao
) : ViewModel() {

    private val _selectedGrade = MutableStateFlow(11)
    private val _selectedTopic = MutableStateFlow<String?>(null)
    private val _selectedDifficulty = MutableStateFlow<String?>(null)

    private val _selectedOptionIndex = MutableStateFlow<Int?>(null)
    private val _isAnswerLocked = MutableStateFlow(false)
    private val _currentQuestion = MutableStateFlow<QuestionEntity?>(null)
    private var lastQuestionId: String? = null

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
        combine(_currentQuestion, _selectedOptionIndex, _isAnswerLocked) { q, opt, locked -> Triple(q, opt, locked) },
        totalCountFlow
    ) { (grade, topic, difficulty), topics, questions, (currentQ, optIdx, locked), allQuestions ->
        PracticeUiState(
            selectedGrade = grade,
            selectedTopic = topic,
            selectedDifficulty = difficulty,
            availableTopics = topics,
            currentQuestion = currentQ,
            selectedOptionIndex = optIdx,
            isAnswerLocked = locked,
            totalQuestionsCount = allQuestions.size,
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
                pickQuestion(list)
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
        _selectedTopic.value = topic
        resetAnswer()
    }

    fun onDifficultySelected(difficulty: String?) {
        _selectedDifficulty.value = difficulty
        resetAnswer()
    }

    fun selectOption(index: Int) {
        if (!_isAnswerLocked.value) {
            _selectedOptionIndex.value = index
            _isAnswerLocked.value = true
        }
    }

    fun nextQuestion() {
        lastQuestionId = _currentQuestion.value?.id
        resetAnswer()
        pickQuestion(filteredQuestions.value)
    }

    private fun resetAnswer() {
        _selectedOptionIndex.value = null
        _isAnswerLocked.value = false
    }

    private fun pickQuestion(questions: List<QuestionEntity>) {
        if (questions.isEmpty()) {
            _currentQuestion.value = null
            return
        }

        val next = if (questions.size == 1) {
            questions[0]
        } else {
            val candidates = questions.filter { it.id != lastQuestionId }
            if (candidates.isNotEmpty()) {
                candidates[Random.nextInt(candidates.size)]
            } else {
                questions[Random.nextInt(questions.size)]
            }
        }
        _currentQuestion.value = next
    }
}

class PracticeViewModelFactory(
    private val questionDao: QuestionDao
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PracticeViewModel::class.java)) {
            return PracticeViewModel(questionDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
