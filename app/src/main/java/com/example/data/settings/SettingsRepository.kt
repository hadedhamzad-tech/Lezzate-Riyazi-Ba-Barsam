package com.example.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

data class UserSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val soundEnabled: Boolean = true
)

class SettingsRepository(private val dataStore: DataStore<Preferences>) {

    constructor(context: Context) : this(context.settingsDataStore)

    companion object {
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        val KEY_SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
    }

    @Volatile
    var isSoundEnabled: Boolean = true

    val userSettingsFlow: Flow<UserSettings> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val themeModeString = preferences[KEY_THEME_MODE] ?: ThemeMode.SYSTEM.name
            val themeMode = try {
                ThemeMode.valueOf(themeModeString)
            } catch (e: IllegalArgumentException) {
                ThemeMode.SYSTEM
            }
            val soundEnabled = preferences[KEY_SOUND_ENABLED] ?: true
            isSoundEnabled = soundEnabled
            UserSettings(
                themeMode = themeMode,
                soundEnabled = soundEnabled
            )
        }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[KEY_THEME_MODE] = mode.name
        }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        isSoundEnabled = enabled
        dataStore.edit { preferences ->
            preferences[KEY_SOUND_ENABLED] = enabled
        }
    }
}
