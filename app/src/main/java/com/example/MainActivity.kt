package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.data.local.AppDatabase
import com.example.data.local.QuestionImporter
import com.example.data.settings.SettingsRepository
import com.example.data.settings.UserSettings
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.AppTheme
import com.example.util.sound.AppSoundPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private var soundPlayer: AppSoundPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getInstance(applicationContext)
        val questionDao = database.questionDao()
        val settingsRepository = SettingsRepository(applicationContext)
        val player = AppSoundPlayer(applicationContext, settingsRepository)
        soundPlayer = player

        // Run question bank importer in background without blocking UI
        lifecycleScope.launch(Dispatchers.IO) {
            QuestionImporter(applicationContext, questionDao).importFromAsset()
        }

        setContent {
            val userSettings by settingsRepository.userSettingsFlow.collectAsStateWithLifecycle(
                initialValue = UserSettings()
            )

            AppTheme(themeMode = userSettings.themeMode) {
                AppNavigation(
                    userSettings = userSettings,
                    settingsRepository = settingsRepository,
                    questionDao = questionDao,
                    soundPlayer = player,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPlayer?.release()
        soundPlayer = null
    }
}
