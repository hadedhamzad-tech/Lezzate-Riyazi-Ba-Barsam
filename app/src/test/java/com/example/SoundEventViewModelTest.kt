package com.example

import com.example.data.local.QuestionDao
import com.example.data.local.QuestionEntity
import com.example.ui.screens.ExamViewModel
import com.example.ui.screens.PracticeRoundSession
import com.example.ui.screens.PracticeViewModel
import com.example.util.sound.SoundEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SoundEventViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeQuestionDao(private val questions: List<QuestionEntity>) : QuestionDao {
        override fun getAllQuestions(): Flow<List<QuestionEntity>> = flowOf(questions)
        override suspend fun getQuestionById(id: String): QuestionEntity? = questions.find { it.id == id }
        override fun getQuestionsByGrade(grade: Int): Flow<List<QuestionEntity>> =
            flowOf(questions.filter { it.grade == grade })
        override fun getTopicsForGrade(grade: Int): Flow<List<String>> =
            flowOf(questions.map { it.topic }.distinct())
        override fun getFilteredQuestions(grade: Int, topic: String?, difficulty: String?): Flow<List<QuestionEntity>> =
            flowOf(questions.filter {
                it.grade == grade && (topic == null || it.topic == topic) && (difficulty == null || it.difficulty == difficulty)
            })
        override suspend fun insertQuestion(question: QuestionEntity) {}
        override suspend fun insertQuestions(questions: List<QuestionEntity>) {}
        override suspend fun getCount(): Int = questions.size
    }

    private fun sampleQuestion(id: String, correctOptionIndex: Int = 1) = QuestionEntity(
        id = id,
        grade = 11,
        topic = "حسابان",
        difficulty = "متوسط",
        questionText = "سؤال $id",
        option1 = "گزینه ۱",
        option2 = "گزینه ۲",
        option3 = "گزینه ۳",
        option4 = "گزینه ۴",
        correctOptionIndex = correctOptionIndex,
        explanation = "توضیح $id"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testPracticeSelectCorrectOptionEmitsAnswerCorrectOnceAndLocks() = runTest {
        val q1 = sampleQuestion("q1", correctOptionIndex = 1)
        val roundSession = PracticeRoundSession()
        roundSession.setQuestions(listOf(q1), forceReset = true)
        val fakeDao = FakeQuestionDao(listOf(q1))
        val viewModel = PracticeViewModel(fakeDao, roundSession)

        val events = mutableListOf<SoundEvent>()
        val collectorJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.soundEvents.collect { events.add(it) }
        }

        // 1. Select correct option (index 1)
        viewModel.selectOption(1)
        assertEquals(listOf(SoundEvent.ANSWER_CORRECT), events)

        // 2. Tapping again after lock must emit nothing
        viewModel.selectOption(1)
        viewModel.selectOption(0)
        assertEquals("No new sounds should be emitted once answer is locked", 1, events.size)

        collectorJob.cancel()
    }

    @Test
    fun testPracticeSelectWrongOptionEmitsAnswerWrongOnceAndLocks() = runTest {
        val q1 = sampleQuestion("q1", correctOptionIndex = 1)
        val roundSession = PracticeRoundSession()
        roundSession.setQuestions(listOf(q1), forceReset = true)
        val fakeDao = FakeQuestionDao(listOf(q1))
        val viewModel = PracticeViewModel(fakeDao, roundSession)

        val events = mutableListOf<SoundEvent>()
        val collectorJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.soundEvents.collect { events.add(it) }
        }

        // 1. Select wrong option (index 0 instead of 1)
        viewModel.selectOption(0)
        assertEquals(listOf(SoundEvent.ANSWER_WRONG), events)

        // 2. Tapping again after lock must emit nothing
        viewModel.selectOption(1)
        assertEquals("No new sounds should be emitted once answer is locked", 1, events.size)

        collectorJob.cancel()
    }

    @Test
    fun testExamSelectionTickClearingTickAndManualFinish() = runTest {
        val questions = listOf(sampleQuestion("e1"), sampleQuestion("e2"))
        val fakeDao = FakeQuestionDao(questions)
        var currentTime = 1000L
        val viewModel = ExamViewModel(fakeDao, timeSource = { currentTime })

        val uiJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }
        val events = mutableListOf<SoundEvent>()
        val collectorJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.soundEvents.collect { events.add(it) }
        }

        // Advance dispatcher to collect available questions
        testScheduler.runCurrent()
        viewModel.startExam()
        testScheduler.runCurrent()

        // 1. Select option 2 on current question -> emits OPTION_TICK
        viewModel.selectOption(2)
        assertEquals(listOf(SoundEvent.OPTION_TICK), events)

        // 2. Clearing option (tap option 2 again) -> emits OPTION_TICK
        viewModel.selectOption(2)
        assertEquals(listOf(SoundEvent.OPTION_TICK, SoundEvent.OPTION_TICK), events)

        // 3. Select option 0 -> emits OPTION_TICK
        viewModel.selectOption(0)
        assertEquals(listOf(SoundEvent.OPTION_TICK, SoundEvent.OPTION_TICK, SoundEvent.OPTION_TICK), events)

        // 4. Manual finish -> emits EXAM_FINISHED once
        viewModel.confirmFinishExam()
        assertEquals(
            listOf(SoundEvent.OPTION_TICK, SoundEvent.OPTION_TICK, SoundEvent.OPTION_TICK, SoundEvent.EXAM_FINISHED),
            events
        )

        // 5. Calling confirmFinishExam a second time must emit nothing
        viewModel.confirmFinishExam()
        assertEquals(
            "Second call to finish must emit nothing",
            4,
            events.size
        )

        collectorJob.cancel()
        uiJob.cancel()
    }

    @Test
    fun testExamTimeoutEmitsExamTimeoutOnce() = runTest {
        val questions = listOf(sampleQuestion("t1"))
        val fakeDao = FakeQuestionDao(questions)
        var currentTime = 1000L
        val viewModel = ExamViewModel(fakeDao, timeSource = { currentTime })

        val uiJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }
        val events = mutableListOf<SoundEvent>()
        val collectorJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.soundEvents.collect { events.add(it) }
        }

        testScheduler.runCurrent()
        viewModel.startExam()
        testScheduler.runCurrent()

        // Advance time beyond deadline (1 question * 90s = 90,000 ms)
        currentTime = 1000L + 95_000L
        testScheduler.advanceTimeBy(600L)
        testScheduler.runCurrent()

        assertTrue("Timeout event should be emitted", events.contains(SoundEvent.EXAM_TIMEOUT))
        assertEquals(1, events.count { it == SoundEvent.EXAM_TIMEOUT })

        // Further finish call after timeout must emit nothing
        viewModel.confirmFinishExam()
        assertEquals(1, events.count { it == SoundEvent.EXAM_TIMEOUT })
        assertFalse(events.contains(SoundEvent.EXAM_FINISHED))

        collectorJob.cancel()
        uiJob.cancel()
    }

    private fun assertFalse(condition: Boolean) {
        org.junit.Assert.assertFalse(condition)
    }
}
