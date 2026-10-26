package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.settings.SettingsRepository
import com.example.data.settings.ThemeMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SettingsPersistenceTest {

    @Test
    fun testSettingsPersistence() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = SettingsRepository(context)

        // Check initial default or existing value
        val initial = repository.userSettingsFlow.first()
        assertNotNull(initial)

        // Persist dark theme
        repository.setThemeMode(ThemeMode.DARK)
        val afterThemeChange = repository.userSettingsFlow.first()
        assertEquals(ThemeMode.DARK, afterThemeChange.themeMode)

        // Persist sound disabled
        repository.setSoundEnabled(false)
        val afterSoundChange = repository.userSettingsFlow.first()
        assertEquals(false, afterSoundChange.soundEnabled)

        // Re-enable sound
        repository.setSoundEnabled(true)
        val afterSoundReenabled = repository.userSettingsFlow.first()
        assertEquals(true, afterSoundReenabled.soundEnabled)
    }
}
