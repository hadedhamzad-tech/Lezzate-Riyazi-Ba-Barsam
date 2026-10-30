package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.UnitCircleCard
import com.example.util.JalaliCalendarUtils
import com.example.util.JalaliDate
import com.example.util.KonkurCountdown
import com.example.util.PersianUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.util.Calendar
import java.util.Locale

@Composable
fun DashboardScreen(
    konkurDate: JalaliDate = KonkurCountdown.DEFAULT,
    onNavigateToPractice: () -> Unit = {},
    onNavigateToExam: () -> Unit = {},
    timeSource: () -> Long = { System.currentTimeMillis() },
    modifier: Modifier = Modifier
) {
    var currentTimeMillis by remember { mutableLongStateOf(timeSource()) }

    LaunchedEffect(Unit) {
        while (isActive) {
            currentTimeMillis = timeSource()
            delay(1000L)
        }
    }

    val calendar = Calendar.getInstance().apply {
        timeInMillis = currentTimeMillis
    }

    val gy = calendar.get(Calendar.YEAR)
    val gm = calendar.get(Calendar.MONTH) + 1
    val gd = calendar.get(Calendar.DAY_OF_MONTH)
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)
    val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

    val jalaliDate = JalaliCalendarUtils.gregorianToJalali(gy, gm, gd)
    val weekdayName = JalaliCalendarUtils.getWeekdayName(dayOfWeek)
    val formattedDate = jalaliDate.formatPersianDate(weekdayName)

    val timeString = "%02d:%02d".format(Locale.US, hour, minute)
    val ltrClock = "${PersianUtils.LRI}$timeString${PersianUtils.PDI}"

    val greeting = DashboardTexts.getGreeting(hour, minute)
    val weekdayMessage = DashboardTexts.getWeekdayMessage(weekdayName)
    val slogan = DashboardTexts.getSloganOfTheDay(jalaliDate.year, jalaliDate.month, jalaliDate.day)

    val daysUntilKonkur = KonkurCountdown.daysUntil(gy, gm, gd, konkurDate)
    val countdownDisplay = KonkurCountdown.display(daysUntilKonkur)
    val targetGregorian = JalaliCalendarUtils.jalaliToGregorian(konkurDate.year, konkurDate.month, konkurDate.day)
    val targetWeekdayName = JalaliCalendarUtils.getWeekdayNameForDate(targetGregorian.first, targetGregorian.second, targetGregorian.third)
    val targetFormattedDate = konkurDate.formatPersianDate(targetWeekdayName)

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("screen_dashboard"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // a) Greeting Card: Greeting, Shamsi date, Live clock
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("dashboard_greeting_card")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.testTag("dashboard_greeting_text")
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.testTag("dashboard_shamsi_date")
                    )

                    Text(
                        text = ltrClock,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.testTag("dashboard_live_clock")
                    )
                }
            }
        }

        // Konkur Countdown Card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("dashboard_konkur_card")
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "روزشمار کنکور 🎡",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )

                Text(
                    text = countdownDisplay.bigText,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.testTag("dashboard_konkur_days_text")
                )

                Text(
                    text = countdownDisplay.message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )

                if (countdownDisplay.weeksText != null) {
                    Text(
                        text = countdownDisplay.weeksText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.85f)
                    )
                }

                Text(
                    text = "کنکور: $targetFormattedDate",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                )
            }
        }

        // b) Weekday Message Card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("dashboard_weekday_card")
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Today,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "پیام امروز",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = weekdayMessage,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.testTag("dashboard_weekday_message")
                )
            }
        }

        // c) Slogan Card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("dashboard_slogan_card")
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = slogan,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("dashboard_slogan_text")
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // d) Action Buttons: "شروع تمرین" and "شروع آزمون"
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onNavigateToPractice,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp)
                    .testTag("dashboard_start_practice_button")
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "شروع تمرین",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = onNavigateToExam,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp)
                    .testTag("dashboard_start_exam_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Assignment,
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

        Spacer(modifier = Modifier.height(8.dp))
        UnitCircleCard()
    }
}
