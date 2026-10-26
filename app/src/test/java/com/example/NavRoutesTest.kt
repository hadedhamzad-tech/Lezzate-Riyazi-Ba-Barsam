package com.example

import com.example.ui.navigation.BottomTab
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class NavRoutesTest {

    @Test
    fun testBottomTabInitialization() {
        val tabs = BottomTab.tabs
        assertEquals(5, tabs.size)
        tabs.forEachIndexed { index, tab ->
            assertNotNull("Tab at index $index must not be null", tab)
            assertNotNull("Tab route at index $index must not be null", tab.route)
            assertNotNull("Tab title at index $index must not be null", tab.title)
        }

        // Verify tabs order from right to left as required by the manifest
        assertEquals("dashboard", tabs[0].route)
        assertEquals("داشبورد", tabs[0].title)

        assertEquals("practice", tabs[1].route)
        assertEquals("تمرین", tabs[1].title)

        assertEquals("exam", tabs[2].route)
        assertEquals("آزمون", tabs[2].title)

        assertEquals("mistakes", tabs[3].route)
        assertEquals("اشتباهات", tabs[3].title)

        assertEquals("analytics", tabs[4].route)
        assertEquals("تحلیل", tabs[4].title)
    }
}
