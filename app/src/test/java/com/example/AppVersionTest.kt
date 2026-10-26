package com.example

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppVersionTest {

    @Test
    fun testAppVersionIsNotEmpty() {
        assertNotNull(BuildConfig.VERSION_NAME)
        assertTrue("App versionName must not be empty", BuildConfig.VERSION_NAME.isNotEmpty())
        assertEquals("0.1.5", BuildConfig.VERSION_NAME)
        assertEquals(6, BuildConfig.VERSION_CODE)
    }
}
