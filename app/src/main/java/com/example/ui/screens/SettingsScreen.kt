package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.BuildConfig
import com.example.data.settings.ThemeMode
import com.example.data.settings.UserSettings
import com.example.util.JalaliCalendarUtils
import com.example.util.KonkurCountdown
import com.example.util.PersianUtils

@Composable
fun SettingsScreen(
    userSettings: UserSettings,
    onThemeModeChange: (ThemeMode) -> Unit,
    onSoundEnabledChange: (Boolean) -> Unit,
    onKonkurDateChange: (Int, Int, Int) -> Unit = { _, _, _ -> },
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onNavigateBack()
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("screen_settings"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Theme Selection Card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ColorLens,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "پوسته برنامه",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column(modifier = Modifier.selectableGroup()) {
                    ThemeMode.values().forEach { mode ->
                        val isSelected = userSettings.themeMode == mode
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .selectable(
                                    selected = isSelected,
                                    onClick = { onThemeModeChange(mode) },
                                    role = Role.RadioButton
                                )
                                .testTag("theme_option_${mode.name.lowercase()}")
                                .padding(horizontal = 8.dp)
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = null,
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = MaterialTheme.colorScheme.primary,
                                    unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = mode.titleFa,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Sound Settings Card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSoundEnabledChange(!userSettings.soundEnabled) }
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.VolumeUp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "صدای برنامه",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (userSettings.soundEnabled) "فعال" else "غیرفعال",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = userSettings.soundEnabled,
                    onCheckedChange = { onSoundEnabledChange(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.testTag("sound_switch")
                )
            }
        }

        // Konkur Date Card
        var yearExpanded by remember { mutableStateOf(false) }
        var monthExpanded by remember { mutableStateOf(false) }
        var dayExpanded by remember { mutableStateOf(false) }

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Today,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "تاریخ کنکور",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Day dropdown
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = { dayExpanded = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("settings_konkur_day")
                        ) {
                            Text(
                                text = "${userSettings.konkurDate.day}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        DropdownMenu(
                            expanded = dayExpanded,
                            onDismissRequest = { dayExpanded = false },
                            modifier = Modifier.heightIn(max = 240.dp)
                        ) {
                            val maxDays = JalaliCalendarUtils.daysInJalaliMonth(
                                userSettings.konkurDate.year,
                                userSettings.konkurDate.month
                            )
                            (1..maxDays).forEach { d ->
                                DropdownMenuItem(
                                    text = { Text("$d") },
                                    onClick = {
                                        dayExpanded = false
                                        onKonkurDateChange(
                                            userSettings.konkurDate.year,
                                            userSettings.konkurDate.month,
                                            d
                                        )
                                    }
                                )
                            }
                        }
                    }

                    // Month dropdown
                    Box(modifier = Modifier.weight(1.3f)) {
                        val currentMonthName = JalaliCalendarUtils.JALALI_MONTH_NAMES.getOrElse(userSettings.konkurDate.month - 1) { "" }
                        OutlinedButton(
                            onClick = { monthExpanded = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("settings_konkur_month")
                        ) {
                            Text(
                                text = currentMonthName,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        DropdownMenu(
                            expanded = monthExpanded,
                            onDismissRequest = { monthExpanded = false },
                            modifier = Modifier.heightIn(max = 240.dp)
                        ) {
                            JalaliCalendarUtils.JALALI_MONTH_NAMES.forEachIndexed { index, name ->
                                val m = index + 1
                                DropdownMenuItem(
                                    text = { Text(name) },
                                    onClick = {
                                        monthExpanded = false
                                        val maxDays = JalaliCalendarUtils.daysInJalaliMonth(userSettings.konkurDate.year, m)
                                        val clampedDay = userSettings.konkurDate.day.coerceAtMost(maxDays)
                                        onKonkurDateChange(userSettings.konkurDate.year, m, clampedDay)
                                    }
                                )
                            }
                        }
                    }

                    // Year dropdown
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = { yearExpanded = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("settings_konkur_year")
                        ) {
                            Text(
                                text = "${userSettings.konkurDate.year}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        DropdownMenu(
                            expanded = yearExpanded,
                            onDismissRequest = { yearExpanded = false },
                            modifier = Modifier.heightIn(max = 240.dp)
                        ) {
                            (1405..1410).forEach { y ->
                                DropdownMenuItem(
                                    text = { Text("$y") },
                                    onClick = {
                                        yearExpanded = false
                                        val maxDays = JalaliCalendarUtils.daysInJalaliMonth(y, userSettings.konkurDate.month)
                                        val clampedDay = userSettings.konkurDate.day.coerceAtMost(maxDays)
                                        onKonkurDateChange(y, userSettings.konkurDate.month, clampedDay)
                                    }
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        onKonkurDateChange(
                            KonkurCountdown.DEFAULT_YEAR,
                            KonkurCountdown.DEFAULT_MONTH,
                            KonkurCountdown.DEFAULT_DAY
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_konkur_reset")
                ) {
                    Text(
                        text = "بازگشت به پیش\u200cفرض (11 تیر 1406)",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }

        // App Version Card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "نسخه برنامه",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                val formattedVersion = "${PersianUtils.LRI}${BuildConfig.VERSION_NAME}${PersianUtils.PDI}"
                Text(
                    text = formattedVersion,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.testTag("app_version_text")
                )
            }
        }
    }
}
