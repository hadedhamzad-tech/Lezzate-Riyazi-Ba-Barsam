package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.settings.SettingsRepository
import com.example.data.settings.UserSettings
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val settingsRepository = SettingsRepository(applicationContext)

        setContent {
            val userSettings by settingsRepository.userSettingsFlow.collectAsStateWithLifecycle(
                initialValue = UserSettings()
            )

            AppTheme(themeMode = userSettings.themeMode) {
                AppNavigation(
                    userSettings = userSettings,
                    settingsRepository = settingsRepository,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
