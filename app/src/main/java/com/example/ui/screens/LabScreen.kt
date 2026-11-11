package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.FilterChip
import com.example.ui.components.ConicSectionCard
import com.example.ui.components.SimilarTrianglesCard
import com.example.ui.components.TransformationsCard
import com.example.ui.components.UnitCircleCard

@Composable
fun LabScreen(
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("lab_screen")
    ) {
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = { Text("مثلثات", style = MaterialTheme.typography.titleMedium) },
                modifier = Modifier.testTag("lab_tab_trig")
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = { Text("هندسه", style = MaterialTheme.typography.titleMedium) },
                modifier = Modifier.testTag("lab_tab_geometry")
            )
        }

        when (selectedTabIndex) {
            0 -> {
                val scrollState = rememberScrollState()
                var speed by rememberSaveable { mutableFloatStateOf(1.0f) }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    UnitCircleCard(
                        height = 260.dp,
                        speedRadPerSec = speed
                    )
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "سرعت چرخش",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Slider(
                            value = speed,
                            onValueChange = { speed = it },
                            valueRange = 0.3f..3.0f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("lab_speed_slider")
                        )
                    }
                }
            }
            1 -> {
                val scrollState = rememberScrollState()
                var selectedScene by rememberSaveable { mutableIntStateOf(0) }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val chipScrollState = rememberScrollState()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(chipScrollState),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedScene == 0,
                            onClick = { selectedScene = 0 },
                            label = { Text("مقاطع مخروطی") },
                            modifier = Modifier.testTag("lab_scene_conic")
                        )
                        FilterChip(
                            selected = selectedScene == 1,
                            onClick = { selectedScene = 1 },
                            label = { Text("تشابه و تالس") },
                            modifier = Modifier.testTag("lab_scene_similar")
                        )
                        FilterChip(
                            selected = selectedScene == 2,
                            onClick = { selectedScene = 2 },
                            label = { Text("تبدیل\u200cها") },
                            modifier = Modifier.testTag("lab_scene_transform")
                        )
                    }

                    when (selectedScene) {
                        0 -> {
                            Text(
                                text = "مقاطع مخروطی",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            ConicSectionCard()
                        }
                        1 -> {
                            Text(
                                text = "تشابه و قضیه\u200cی تالس",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            SimilarTrianglesCard()
                        }
                        2 -> {
                            Text(
                                text = "تبدیل\u200cهای هندسی",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            TransformationsCard()
                        }
                    }
                }
            }
        }
    }
}
