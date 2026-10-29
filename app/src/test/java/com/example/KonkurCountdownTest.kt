package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.settings.SettingsRepository
import com.example.util.JalaliDate
import com.example.util.KonkurCountdown
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class KonkurCountdownTest {

    @Test
    fun testDaysUntil() {
        // daysUntil(2026,10,29, 1406/4/11) == 246
        val target = JalaliDate(1406, 4, 11)
        val d246 = KonkurCountdown.daysUntil(2026, 10, 29, target)
        assertEquals(246L, d246)

        // same day -> 0 (target is 2027, 7, 2)
        val sameDay = KonkurCountdown.daysUntil(2027, 7, 2, target)
        assertEquals(0L, sameDay)

        // next day (target is tomorrow -> 1)
        val nextDay = KonkurCountdown.daysUntil(2027, 7, 1, target)
        assertEquals(1L, nextDay)

        // past date -> negative
        val pastDay = KonkurCountdown.daysUntil(2027, 7, 3, target)
        assertTrue("Past date must yield negative days", pastDay < 0)
        assertEquals(-1L, pastDay)
    }

    @Test
    fun testWeeksAndDaysText() {
        assertEquals("35 هفته و 1 روز", KonkurCountdown.weeksAndDaysText(246L))
        assertEquals("2 هفته", KonkurCountdown.weeksAndDaysText(14L))
        assertNull(KonkurCountdown.weeksAndDaysText(6L))
    }

    @Test
    fun testDisplayStrings() {
        val disp246 = KonkurCountdown.display(246L)
        assertEquals("246 روز", disp246.bigText)
        assertEquals("تا بزرگترین ماجراجویی! 🚀", disp246.message)
        assertEquals("35 هفته و 1 روز", disp246.weeksText)

        val disp1 = KonkurCountdown.display(1L)
        assertEquals("فردا", disp1.bigText)
        assertEquals("فردا روز بزرگه! نفس عمیق بکش و آرامش داشته باش 🌟", disp1.message)
        assertNull(disp1.weeksText)

        val disp0 = KonkurCountdown.display(0L)
        assertEquals("امروز", disp0.bigText)
        assertEquals("امروز روز کنکوره! تو آمادهای؛ با آرامش برو و بدرخش 🏆", disp0.message)
        assertNull(disp0.weeksText)

        val dispPast = KonkurCountdown.display(-5L)
        assertEquals("تاریخ گذشته", dispPast.bigText)
        assertEquals("از تنظیمات، تاریخ جدید کنکور را وارد کن ⚙️", dispPast.message)
        assertNull(dispPast.weeksText)
    }

    @Test
    fun testParseAndSerialize() {
        assertEquals(KonkurCountdown.DEFAULT, KonkurCountdown.parse(null))
        assertEquals(KonkurCountdown.DEFAULT, KonkurCountdown.parse("garbage"))
        assertEquals(KonkurCountdown.DEFAULT, KonkurCountdown.parse("1405-12-30"))

        val parsed = KonkurCountdown.parse("1406-04-11")
        assertEquals(JalaliDate(1406, 4, 11), parsed)

        // serialize / parse round trip
        val original = JalaliDate(1407, 5, 23)
        val serialized = KonkurCountdown.serialize(original)
        assertEquals("1407-05-23", serialized)
        val roundTrip = KonkurCountdown.parse(serialized)
        assertEquals(original, roundTrip)
    }

    @Test
    fun testPersistence() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = SettingsRepository(context)

        // Test default value
        val initial = repository.userSettingsFlow.first()
        assertEquals(KonkurCountdown.DEFAULT, initial.konkurDate)

        // Set valid date
        repository.setKonkurDate(1407, 4, 12)
        val updated = repository.userSettingsFlow.first()
        assertEquals(JalaliDate(1407, 4, 12), updated.konkurDate)

        // Set invalid date (1405-12-30) -> ignored
        repository.setKonkurDate(1405, 12, 30)
        val afterInvalid = repository.userSettingsFlow.first()
        assertEquals("Invalid date must be ignored", JalaliDate(1407, 4, 12), afterInvalid.konkurDate)
    }
}
