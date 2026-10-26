package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.QuestionEntity
import com.example.ui.components.MathText
import com.example.ui.theme.LocalExtendedColors
import com.example.util.PersianUtils

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PracticeScreen(
    viewModel: PracticeViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("screen_practice"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. Filters Section ---
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Grade Filter Chips: «پایه ۱۰»، «پایه ۱۱»، «پایه ۱۲»
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
                        val gradeFa = PersianUtils.toPersianDigits(grade.toString())
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.onGradeSelected(grade) },
                            label = { Text("پایه $gradeFa") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.testTag("grade_chip_$grade")
                        )
                    }
                }

                // Topic Selector
                Text(
                    text = "مبحث:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                var topicDropdownExpanded by remember { mutableStateOf(false) }
                Box {
                    OutlinedCard(
                        onClick = { topicDropdownExpanded = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("topic_selector")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = uiState.selectedTopic ?: "همه مباحث",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "انتخاب مبحث",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = topicDropdownExpanded,
                        onDismissRequest = { topicDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("همه مباحث") },
                            onClick = {
                                viewModel.onTopicSelected(null)
                                topicDropdownExpanded = false
                            }
                        )
                        uiState.availableTopics.forEach { topic ->
                            DropdownMenuItem(
                                text = { Text(topic) },
                                onClick = {
                                    viewModel.onTopicSelected(topic)
                                    topicDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Difficulty Filter Chips: «همه»، «آسان»، «متوسط»، «سخت»، «کنکوری»
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
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    difficulties.forEach { (diffKey, label) ->
                        val isSelected = uiState.selectedDifficulty == diffKey
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.onDifficultySelected(diffKey) },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.testTag("difficulty_chip_$label")
                        )
                    }
                }

                // Count line near filters: "<N> سؤال در این فیلتر"
                val countFa = PersianUtils.toPersianDigits(uiState.filterMatchingCount.toString())
                Text(
                    text = "$countFa سؤال در این فیلتر",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("filter_count_line")
                )
            }
        }

        // --- 2. Content Area ---
        if (uiState.totalQuestionsInBank == 0) {
            // While importing and table is empty
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "در حال آمادهسازی بانک سؤال...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.testTag("loading_questions_text")
                    )
                }
            }
        } else if (uiState.isFilterEmpty) {
            // Empty result for current filters
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "سؤالی با این مشخصات پیدا نشد. فیلترها را تغییر بده.",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.testTag("empty_questions_text")
                    )
                }
            }
        } else if (uiState.isRoundFinished) {
            // Finished all questions of current round
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = MaterialTheme.shapes.large,
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .testTag("round_finished_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = LocalExtendedColors.current.success,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "همهی سؤالهای این فیلتر را دیدی 🎉",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("round_finished_text")
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { viewModel.restartRound() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("restart_round_button")
                    ) {
                        Text(
                            text = "دوباره از اول",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else if (uiState.currentQuestion != null) {
            // Matching Question Display
            val question = uiState.currentQuestion!!
            QuestionPracticeCard(
                question = question,
                currentQuestionIndexInRound = uiState.currentQuestionIndexInRound,
                totalQuestionsInRound = uiState.filterMatchingCount,
                selectedOptionIndex = uiState.selectedOptionIndex,
                isAnswerLocked = uiState.isAnswerLocked,
                onOptionSelected = { index -> viewModel.selectOption(index) },
                onNextQuestion = { viewModel.nextQuestion() }
            )
        }
    }
}

@Composable
fun QuestionPracticeCard(
    question: QuestionEntity,
    currentQuestionIndexInRound: Int,
    totalQuestionsInRound: Int,
    selectedOptionIndex: Int?,
    isAnswerLocked: Boolean,
    onOptionSelected: (Int) -> Unit,
    onNextQuestion: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = LocalExtendedColors.current

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
            .fillMaxWidth()
            .testTag("question_card_${question.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Progress label ("سؤال <i> از <N>") and Topic/Difficulty
            val currentIdxFa = PersianUtils.toPersianDigits(currentQuestionIndexInRound.toString())
            val totalFa = PersianUtils.toPersianDigits(totalQuestionsInRound.toString())

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "سؤال $currentIdxFa از $totalFa",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.testTag("question_progress_label")
                )
                Text(
                    text = "${question.topic} • ${question.difficulty}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            // Question Text with MathText
            MathText(
                text = question.questionText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth()
            )

            // 4 Options
            val options = listOf(question.option1, question.option2, question.option3, question.option4)
            options.forEachIndexed { index, optionText ->
                val optionLabel = PersianUtils.toPersianDigits((index + 1).toString())
                val isSelected = selectedOptionIndex == index
                val isCorrect = question.correctOptionIndex == index

                val containerColor = when {
                    !isAnswerLocked -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    isCorrect -> extendedColors.successContainer
                    isSelected && !isCorrect -> MaterialTheme.colorScheme.errorContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                }

                val borderColor = when {
                    !isAnswerLocked -> MaterialTheme.colorScheme.outlineVariant
                    isCorrect -> extendedColors.success
                    isSelected && !isCorrect -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                }

                val contentColor = when {
                    !isAnswerLocked -> MaterialTheme.colorScheme.onSurface
                    isCorrect -> extendedColors.onSuccessContainer
                    isSelected && !isCorrect -> MaterialTheme.colorScheme.onErrorContainer
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = containerColor),
                    border = BorderStroke(1.5.dp, borderColor),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !isAnswerLocked) {
                            onOptionSelected(index)
                        }
                        .testTag("option_card_$index")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "($optionLabel)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = contentColor
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        MathText(
                            text = optionText,
                            style = MaterialTheme.typography.bodyLarge,
                            color = contentColor,
                            modifier = Modifier.weight(1f)
                        )
                        if (isAnswerLocked) {
                            if (isCorrect) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "پاسخ درست",
                                    tint = extendedColors.success
                                )
                            } else if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "پاسخ نادرست",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }

            // --- 3. Instant Feedback & Explanation Section ---
            AnimatedVisibility(visible = isAnswerLocked) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("explanation_section"),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val isUserCorrect = selectedOptionIndex == question.correctOptionIndex

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUserCorrect) {
                                extendedColors.successContainer.copy(alpha = 0.5f)
                            } else {
                                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                            }
                        ),
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Text(
                                text = if (isUserCorrect) "پاسخ درست" else "پاسخ نادرست",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isUserCorrect) extendedColors.success else MaterialTheme.colorScheme.error
                            )
                            if (!isUserCorrect) {
                                val correctLabel = PersianUtils.toPersianDigits((question.correctOptionIndex + 1).toString())
                                Text(
                                    text = "گزینه درست: گزینه $correctLabel",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Explanation
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "توضیح راهحل",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            MathText(
                                text = question.explanation,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Button: سؤال بعدی
                    Button(
                        onClick = onNextQuestion,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("next_question_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "سؤال بعدی",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.NavigateNext,
                                contentDescription = null
                            )
                        }
                    }
                }
            }
        }
    }
}
