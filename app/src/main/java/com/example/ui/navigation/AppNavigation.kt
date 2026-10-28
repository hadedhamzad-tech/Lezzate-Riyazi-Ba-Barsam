package com.example.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.local.QuestionDao
import com.example.data.settings.SettingsRepository
import com.example.data.settings.ThemeMode
import com.example.data.settings.UserSettings
import com.example.ui.components.AppBottomBar
import com.example.ui.components.AppTopBar
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ExamScreen
import com.example.ui.screens.ExamViewModel
import com.example.ui.screens.ExamViewModelFactory
import com.example.ui.screens.MistakesScreen
import com.example.ui.screens.PracticeScreen
import com.example.ui.screens.PracticeViewModel
import com.example.ui.screens.PracticeViewModelFactory
import com.example.ui.screens.SettingsScreen
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(
    userSettings: UserSettings,
    settingsRepository: SettingsRepository,
    questionDao: QuestionDao,
    soundPlayer: com.example.util.sound.AppSoundPlayer? = null,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: BottomTab.Dashboard.route
    val coroutineScope = rememberCoroutineScope()

    val isSettingsRoute = currentRoute == NavDestinations.SETTINGS
    val currentTitle = when (currentRoute) {
        NavDestinations.SETTINGS -> "تنظیمات"
        BottomTab.Dashboard.route -> BottomTab.Dashboard.title
        BottomTab.Practice.route -> BottomTab.Practice.title
        BottomTab.Exam.route -> BottomTab.Exam.title
        BottomTab.Mistakes.route -> BottomTab.Mistakes.title
        BottomTab.Analytics.route -> BottomTab.Analytics.title
        else -> "لذت ریاضی با برسام"
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                AppTopBar(
                    title = currentTitle,
                    canNavigateBack = isSettingsRoute,
                    onNavigateBack = { navController.popBackStack() },
                    onOpenSettings = {
                        navController.navigate(NavDestinations.SETTINGS) {
                            launchSingleTop = true
                        }
                    }
                )
            },
            bottomBar = {
                if (!isSettingsRoute) {
                    AppBottomBar(
                        currentRoute = currentRoute,
                        onTabSelected = { tab ->
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.background,
            modifier = modifier.fillMaxSize()
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = BottomTab.Dashboard.route,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                composable(BottomTab.Dashboard.route) {
                    DashboardScreen(
                        onNavigateToPractice = {
                            navController.navigate(BottomTab.Practice.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onNavigateToExam = {
                            navController.navigate(BottomTab.Exam.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
                composable(BottomTab.Practice.route) {
                    val practiceViewModel: PracticeViewModel = viewModel(
                        factory = PracticeViewModelFactory(questionDao)
                    )
                    PracticeScreen(viewModel = practiceViewModel, soundPlayer = soundPlayer)
                }
                composable(BottomTab.Exam.route) {
                    val examViewModel: ExamViewModel = viewModel(
                        factory = ExamViewModelFactory(questionDao)
                    )
                    ExamScreen(viewModel = examViewModel, soundPlayer = soundPlayer)
                }
                composable(BottomTab.Mistakes.route) {
                    MistakesScreen()
                }
                composable(BottomTab.Analytics.route) {
                    AnalyticsScreen()
                }
                composable(NavDestinations.SETTINGS) {
                    SettingsScreen(
                        userSettings = userSettings,
                        onThemeModeChange = { mode: ThemeMode ->
                            coroutineScope.launch {
                                settingsRepository.setThemeMode(mode)
                            }
                        },
                        onSoundEnabledChange = { enabled: Boolean ->
                            coroutineScope.launch {
                                settingsRepository.setSoundEnabled(enabled)
                            }
                        },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
