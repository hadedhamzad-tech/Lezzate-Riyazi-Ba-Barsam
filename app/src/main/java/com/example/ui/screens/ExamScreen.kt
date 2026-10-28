package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.components.MathText
import com.example.ui.theme.LocalExtendedColors

@Composable
fun ExamScreen(
    viewModel: ExamViewModel,
    soundPlayer: com.example.util.sound.AppSoundPlayer? = null,
    modifier: Modifier = Modifier
) {
    androidx.compose.runtime.LaunchedEffect(viewModel, soundPlayer) {
        viewModel.soundEvents.collect { event ->
            soundPlayer?.play(event)
        }
    }

    val uiState by viewModel.uiState.collectAsState()

    BackHandler(enabled = uiState.stage == ExamScreenStage.IN_PROGRESS) {
        viewModel.requestAbandonDialog(true)
    }

    BackHandler(enabled = uiState.stage == ExamScreenStage.REVIEW) {
        viewModel.closeReview()
    }

    // Finish Dialog (opened by both finish buttons)
    if (uiState.isFinishDialogVisible) {
        AlertDialog(
            onDismissRequest = { viewModel.requestFinishDialog(false) },
            title = {
                Text(
                    text = "پایان آزمون",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                val dialogMsg = if (uiState.unansweredCount > 0) {
                    "پاسخ‌های شما ثبت و نمره‌گذاری می‌شود. ${uiState.unansweredCount} سؤال بی‌پاسخ دارید. آزمون را تمام می‌کنید؟"
                } else {
                    "پاسخ‌های شما ثبت و نمره‌گذاری می‌شود. همه‌ی سؤال‌ها پاسخ داده شده‌اند. آزمون را تمام می‌کنید؟"
                }
                Text(
                    text = dialogMsg,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmFinishExam() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.testTag("exam_finish_confirm_button")
                ) {
                    Text("ثبت و پایان آزمون")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.requestFinishDialog(false) },
                    modifier = Modifier.testTag("exam_finish_dismiss_button")
                ) {
                    Text("ادامهٔ آزمون")
                }
            }
        )
    }

    // Abandon Dialog (opened by system back during exam)
    if (uiState.isAbandonDialogVisible) {
        AlertDialog(
            onDismissRequest = { viewModel.requestAbandonDialog(false) },
            title = {
                Text(
                    text = "خروج از آزمون",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Text(
                    text = "اگر خارج شوید، آزمون و پاسخ‌های شما حذف می‌شوند و نمره‌ای ثبت نمی‌شود. خارج می‌شوید؟",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmAbandonExam() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    modifier = Modifier.testTag("exam_abandon_confirm_button")
                ) {
                    Text("خروج و حذف آزمون")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.requestAbandonDialog(false) },
                    modifier = Modifier.testTag("exam_abandon_dismiss_button")
                ) {
                    Text("ادامهٔ آزمون")
                }
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("screen_exam")
    ) {
        when (uiState.stage) {
            ExamScreenStage.SETUP -> {
                ExamSetupContent(
                    uiState = uiState,
                    onGradeSelected = viewModel::onGradeSelected,
                    onDifficultySelected = viewModel::onDifficultySelected,
                    onQuestionCountSelected = viewModel::onQuestionCountSelected,
                    onStartExam = viewModel::startExam
                )
            }
            ExamScreenStage.IN_PROGRESS -> {
                ExamInProgressContent(
                    uiState = uiState,
                    onOptionSelected = viewModel::selectOption,
                    onGoToQuestion = viewModel::goToQuestion,
                    onNext = viewModel::nextQuestion,
                    onPrevious = viewModel::previousQuestion,
                    onRequestFinish = { viewModel.requestFinishDialog(true) }
                )
            }
            ExamScreenStage.RESULT -> {
                ExamResultContent(
                    uiState = uiState,
                    onOpenReview = viewModel::openReview,
                    onNewExam = viewModel::startNewExam
                )
            }
            ExamScreenStage.REVIEW -> {
                ExamReviewContent(
                    uiState = uiState,
                    onFilterSelected = viewModel::setReviewFilter,
                    onCloseReview = viewModel::closeReview
                )
            }
        }
    }
}

@Composable
private fun ExamSetupContent(
    uiState: ExamUiState,
    onGradeSelected: (Int) -> Unit,
    onDifficultySelected: (String?) -> Unit,
    onQuestionCountSelected: (Int) -> Unit,
    onStartExam: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "تنظیمات آزمون کنکوری",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // 1. Grade chips
                Text(
                    text = "پایه تحصیلی:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(10, 11, 12).forEach { grade ->
                        val isSelected = uiState.selectedGrade == grade
                        FilterChip(
                            selected = isSelected,
                            onClick = { onGradeSelected(grade) },
                            label = { Text("پایه $grade") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.testTag("exam_grade_chip_$grade")
                        )
                    }
                }

                // 2. Difficulty chips
                Text(
                    text = "درجه دشواری:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                val difficulties = listOf(
                    null to "همه",
                    "آسان" to "آسان",
                    "متوسط" to "متوسط",
                    "سخت" to "سخت",
                    "کنکوری" to "کنکوری"
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    difficulties.forEach { (diffKey, diffLabel) ->
                        val isSelected = uiState.selectedDifficulty == diffKey
                        FilterChip(
                            selected = isSelected,
                            onClick = { onDifficultySelected(diffKey) },
                            label = { Text(diffLabel) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.testTag("exam_difficulty_chip_$diffLabel")
                        )
                    }
                }

                // 3. Number of questions chips
                Text(
                    text = "تعداد سؤال:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(10, 20, 30).forEach { count ->
                        val isSelected = uiState.selectedQuestionCount == count
                        FilterChip(
                            selected = isSelected,
                            onClick = { onQuestionCountSelected(count) },
                            label = { Text("$count سؤال") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.testTag("exam_count_chip_$count")
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Time info
                val effectiveCount = if (uiState.availableQuestionsCount > 0) {
                    minOf(uiState.selectedQuestionCount, uiState.availableQuestionsCount)
                } else {
                    uiState.selectedQuestionCount
                }
                val durationText = ExamFormatters.formatDuration(effectiveCount * ExamSession.SECONDS_PER_QUESTION)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "زمان",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = durationText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Scoring rule
                Text(
                    text = "هر پاسخ درست 3 نمره مثبت، هر پاسخ غلط 1 نمره منفی، بدون پاسخ صفر",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Match count & warnings
                if (uiState.availableQuestionsCount == 0) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "هیچ سؤالی با این فیلتر یافت نشد. لطفاً پایه یا درجه دشواری دیگری را انتخاب کنید.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                } else {
                    Text(
                        text = "${uiState.availableQuestionsCount} سؤال در این فیلتر موجود است",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (uiState.availableQuestionsCount < uiState.selectedQuestionCount) {
                        Text(
                            text = "فقط ${uiState.availableQuestionsCount} سؤال موجود است و آزمون با تمام سؤال‌های موجود برگزار می‌شود.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }
        }

        Button(
            onClick = onStartExam,
            enabled = uiState.availableQuestionsCount > 0,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("exam_start_button")
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "شروع آزمون",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ExamInProgressContent(
    uiState: ExamUiState,
    onOptionSelected: (Int) -> Unit,
    onGoToQuestion: (Int) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onRequestFinish: () -> Unit
) {
    val question = uiState.currentQuestion
    val scrollState = rememberScrollState()
    val isLast = uiState.currentQuestionIndex >= uiState.totalQuestionsInExam - 1
    val nextLabel = ExamReview.getNextButtonLabel(uiState.currentQuestionIndex, uiState.totalQuestionsInExam)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Top Header: Question counter, Countdown timer, and Top Finish button
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "سؤال ${uiState.currentQuestionIndex + 1} از ${uiState.totalQuestionsInExam}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.testTag("exam_question_counter")
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "زمان باقی‌مانده",
                        tint = if (uiState.remainingSeconds <= 60) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = uiState.formattedRemainingTime,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.remainingSeconds <= 60) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.testTag("exam_timer_text")
                    )
                }

                Button(
                    onClick = onRequestFinish,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    modifier = Modifier.testTag("exam_finish_button")
                ) {
                    Text(
                        text = "پایان آزمون",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 2. Horizontally scrollable row of question numbers
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (i in 0 until uiState.totalQuestionsInExam) {
                val isCurrent = uiState.currentQuestionIndex == i
                val isAnswered = uiState.answeredFlags.getOrElse(i) { false }

                val containerColor = when {
                    isCurrent -> MaterialTheme.colorScheme.primaryContainer
                    isAnswered -> MaterialTheme.colorScheme.secondaryContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                }

                val contentColor = when {
                    isCurrent -> MaterialTheme.colorScheme.onPrimaryContainer
                    isAnswered -> MaterialTheme.colorScheme.onSecondaryContainer
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }

                val border = if (isCurrent) {
                    BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                } else {
                    BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }

                Surface(
                    onClick = { onGoToQuestion(i) },
                    shape = RoundedCornerShape(8.dp),
                    color = containerColor,
                    contentColor = contentColor,
                    border = border,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("exam_question_number_$i")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "${i + 1}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        // 3. Question Card and Options (Scrollable area)
        if (question != null) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "${question.topic} • ${question.difficulty}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )

                        MathText(
                            text = question.questionText,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.fillMaxWidth()
                        )

                        val options = listOf(question.option1, question.option2, question.option3, question.option4)
                        options.forEachIndexed { index, optionText ->
                            val isSelected = uiState.selectedOptionIndex == index

                            val optContainerColor = if (isSelected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                            }

                            val optBorderColor = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outlineVariant
                            }

                            Card(
                                onClick = { onOptionSelected(index) },
                                colors = CardDefaults.cardColors(containerColor = optContainerColor),
                                border = BorderStroke(if (isSelected) 2.dp else 1.dp, optBorderColor),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("exam_option_card_$index")
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Neutral radio-style indicator (no checkmark, no green color)
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .background(
                                                color = MaterialTheme.colorScheme.surface,
                                                shape = CircleShape
                                            )
                                            .border(
                                                width = 2.dp,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .background(
                                                        color = MaterialTheme.colorScheme.primary,
                                                        shape = CircleShape
                                                    )
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Text(
                                        text = "(${index + 1})",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    MathText(
                                        text = optionText,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Bottom navigation: Previous & Next / Finish buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onPrevious,
                enabled = uiState.currentQuestionIndex > 0,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("exam_prev_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("قبلی")
            }

            Spacer(modifier = Modifier.width(12.dp))

            Button(
                onClick = if (isLast) onRequestFinish else onNext,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("exam_next_button")
            ) {
                Text(nextLabel)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = if (isLast) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
private fun ExamResultContent(
    uiState: ExamUiState,
    onOpenReview: () -> Unit,
    onNewExam: () -> Unit
) {
    val score = uiState.scoreResult
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "نتیجهٔ آزمون",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.testTag("exam_result_title")
        )

        if (score != null) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Percentage display
                    Text(
                        text = ExamResultFormatter.formatDisplayPercent(score.formattedPercentage),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (score.percentage >= 50.0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        modifier = Modifier.testTag("exam_percent_text")
                    )

                    // Raw score
                    Text(
                        text = ExamResultFormatter.formatRawScoreText(score.rawScore, score.maxScore),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.testTag("exam_score_text")
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Counts row: Correct / Wrong / Blank
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "درست",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${score.correctCount}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "غلط",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${score.wrongCount}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "بدون پاسخ",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${score.blankCount}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Time used & Finish Reason
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "زمان مصرف‌شده:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = ExamFormatters.formatUsedTime(uiState.elapsedSeconds),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "نحوه پایان:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val reasonText = if (uiState.finishReason == ExamFinishReason.TIMEOUT) {
                            "اتمام زمان آزمون"
                        } else {
                            "تأیید پایان توسط کاربر"
                        }
                        Text(
                            text = reasonText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (uiState.finishReason == ExamFinishReason.TIMEOUT) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Button: Review Answers (only when exam has at least 1 question)
        if (uiState.totalQuestionsInExam > 0) {
            Button(
                onClick = onOpenReview,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("exam_open_review_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "مرور پاسخ‌ها",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Button(
            onClick = onNewExam,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("exam_new_exam_button")
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "آزمون جدید",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ExamReviewContent(
    uiState: ExamUiState,
    onFilterSelected: (ReviewFilter) -> Unit,
    onCloseReview: () -> Unit
) {
    val extendedColors = LocalExtendedColors.current
    val counts = uiState.reviewFilterCounts

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Top Bar: Back button and Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onCloseReview,
                modifier = Modifier.testTag("exam_review_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "بازگشت به نتیجه آزمون",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "مرور آزمون",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.testTag("exam_review_title")
            )
        }

        // Filter chips with counts: همه (N) / غلط (N) / بدون پاسخ (N) / درست (N)
        val filterOptions = listOf(
            ReviewFilter.ALL to "همه (${counts.all})",
            ReviewFilter.WRONG to "غلط (${counts.wrong})",
            ReviewFilter.BLANK to "بدون پاسخ (${counts.blank})",
            ReviewFilter.CORRECT to "درست (${counts.correct})"
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filterOptions.forEach { (filter, label) ->
                val isSelected = uiState.activeReviewFilter == filter
                FilterChip(
                    selected = isSelected,
                    onClick = { onFilterSelected(filter) },
                    label = { Text(label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.testTag("review_filter_chip_${filter.name}")
                )
            }
        }

        // List of Review Cards
        if (uiState.filteredReviewItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "موردی برای نمایش نیست",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("review_empty_text")
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("review_items_list"),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(
                    items = uiState.filteredReviewItems,
                    key = { it.question.id }
                ) { item ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("review_card_${item.number}")
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Header: Original question number, topic • difficulty, and status badge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "سؤال ${item.number}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "${item.question.topic} • ${item.question.difficulty}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }

                                // Status Badge
                                val (badgeBg, badgeText, badgeColor) = when (item.status) {
                                    QuestionReviewStatus.CORRECT -> Triple(
                                        extendedColors.successContainer,
                                        "درست",
                                        extendedColors.onSuccessContainer
                                    )
                                    QuestionReviewStatus.WRONG -> Triple(
                                        MaterialTheme.colorScheme.errorContainer,
                                        "غلط",
                                        MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    QuestionReviewStatus.BLANK -> Triple(
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        "بدون پاسخ",
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = badgeBg,
                                    modifier = Modifier.testTag("review_badge_${item.number}")
                                ) {
                                    Text(
                                        text = badgeText,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = badgeColor,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            // Question Text with MathText
                            MathText(
                                text = item.question.questionText,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.fillMaxWidth()
                            )

                            // 4 Options
                            val options = listOf(
                                item.question.option1,
                                item.question.option2,
                                item.question.option3,
                                item.question.option4
                            )
                            options.forEachIndexed { optIndex, optText ->
                                val isCorrect = optIndex == item.question.correctOptionIndex
                                val isUserWrong = optIndex == item.selectedOptionIndex && item.status == QuestionReviewStatus.WRONG

                                val optBg = when {
                                    isCorrect -> extendedColors.successContainer
                                    isUserWrong -> MaterialTheme.colorScheme.errorContainer
                                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                }

                                val optBorder = when {
                                    isCorrect -> extendedColors.success
                                    isUserWrong -> MaterialTheme.colorScheme.error
                                    else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                }

                                val optTextColor = when {
                                    isCorrect -> extendedColors.onSuccessContainer
                                    isUserWrong -> MaterialTheme.colorScheme.onErrorContainer
                                    else -> MaterialTheme.colorScheme.onSurface
                                }

                                Card(
                                    colors = CardDefaults.cardColors(containerColor = optBg),
                                    border = BorderStroke(if (isCorrect || isUserWrong) 1.5.dp else 1.dp, optBorder),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "(${optIndex + 1})",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = optTextColor
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        MathText(
                                            text = optText,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = optTextColor,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (isCorrect) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "گزینه درست",
                                                tint = extendedColors.success
                                            )
                                        } else if (isUserWrong) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "پاسخ نادرست شما",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }

                            // Summary of answers
                            val userChoiceText = if (item.selectedOptionIndex != null) {
                                "گزینه ${item.selectedOptionIndex + 1}"
                            } else {
                                "بدون پاسخ"
                            }
                            val correctChoiceText = "گزینه ${item.question.correctOptionIndex + 1}"

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "پاسخ شما: $userChoiceText",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = if (item.status == QuestionReviewStatus.CORRECT) extendedColors.success else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "پاسخ درست: $correctChoiceText",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = extendedColors.success
                                )
                            }

                            // Explanation Card
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "توضیح راه‌حل",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    MathText(
                                        text = item.question.explanation,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
