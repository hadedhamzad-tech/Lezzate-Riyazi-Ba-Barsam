package com.example

import com.example.ui.screens.DashboardTexts
import com.example.util.JalaliCalendarUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class PersianHalfSpaceTest {

    private val forbiddenGluedWords = listOf(
        "سهشنبه",
        "مهمتر",
        "اشتباهها",
        "معلمهای",
        "خوبیاند",
        "یکیشان",
        "میشود",
        "هفتهای"
    )

    private fun findSourceDir(subpath: String): File {
        val candidates = listOf(
            File(subpath),
            File("app/$subpath"),
            File("../$subpath"),
            File("../app/$subpath")
        )
        return candidates.firstOrNull { it.exists() && it.isDirectory }
            ?: error("Could not find directory '$subpath' from cwd: ${File(".").absolutePath}")
    }

    @Test
    fun testNoForbiddenGluedWordsInUiAndUtilSourceFiles() {
        val uiDir = findSourceDir("src/main/java/com/example/ui")
        val utilDir = findSourceDir("src/main/java/com/example/util")

        val ktFiles = (uiDir.walkTopDown() + utilDir.walkTopDown())
            .filter { it.isFile && it.extension == "kt" }
            .toList()

        assertTrue("Should have inspected source files in ui and util", ktFiles.isNotEmpty())

        val violations = mutableListOf<String>()

        for (file in ktFiles) {
            val content = file.readText(Charsets.UTF_8)
            for (forbidden in forbiddenGluedWords) {
                if (content.contains(forbidden)) {
                    violations.add("File ${file.name} contains forbidden glued word '$forbidden'")
                }
            }
        }

        assertTrue(
            "Found forbidden glued forms:\n" + violations.joinToString("\n"),
            violations.isEmpty()
        )
    }

    @Test
    fun testWeekdayNamesContainsTuesdayWithHalfSpace() {
        assertEquals("سه\u200Cشنبه", JalaliCalendarUtils.WEEKDAY_NAMES[3])
    }

    @Test
    fun testAllSevenWeekdayNamesMapToNonBlankMessage() {
        assertEquals(7, JalaliCalendarUtils.WEEKDAY_NAMES.size)
        for (name in JalaliCalendarUtils.WEEKDAY_NAMES) {
            val msg = DashboardTexts.getWeekdayMessage(name)
            assertNotNull(msg)
            assertTrue("Message for '$name' must not be blank", msg.isNotBlank())
            assertFalse(
                "Message for '$name' should be an explicit day message, not the fallback",
                msg.contains("با انرژی و تمرکز بالا")
            )
        }
    }
}
