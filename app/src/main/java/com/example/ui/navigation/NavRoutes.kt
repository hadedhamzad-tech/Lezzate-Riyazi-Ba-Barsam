package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.School
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomTab(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    data object Dashboard : BottomTab(
        route = "dashboard",
        title = "داشبورد",
        selectedIcon = Icons.Filled.Dashboard,
        unselectedIcon = Icons.Outlined.Dashboard,
        testTag = "tab_dashboard"
    )

    data object Practice : BottomTab(
        route = "practice",
        title = "تمرین",
        selectedIcon = Icons.Filled.School,
        unselectedIcon = Icons.Outlined.School,
        testTag = "tab_practice"
    )

    data object Exam : BottomTab(
        route = "exam",
        title = "آزمون",
        selectedIcon = Icons.Filled.Assignment,
        unselectedIcon = Icons.Outlined.Assignment,
        testTag = "tab_exam"
    )

    data object Mistakes : BottomTab(
        route = "mistakes",
        title = "اشتباهات",
        selectedIcon = Icons.Filled.ErrorOutline,
        unselectedIcon = Icons.Outlined.ErrorOutline,
        testTag = "tab_mistakes"
    )

    data object Analytics : BottomTab(
        route = "analytics",
        title = "تحلیل",
        selectedIcon = Icons.Filled.Analytics,
        unselectedIcon = Icons.Outlined.Analytics,
        testTag = "tab_analytics"
    )

    companion object {
        val tabs: List<BottomTab>
            get() = listOf(
                Dashboard,
                Practice,
                Exam,
                Mistakes,
                Analytics
            )
    }
}

object NavDestinations {
    const val SETTINGS = "settings"
}
