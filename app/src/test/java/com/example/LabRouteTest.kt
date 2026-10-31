package com.example

import com.example.ui.navigation.BottomTab
import com.example.ui.navigation.NavDestinations
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Test

class LabRouteTest {

    @Test
    fun testLabRouteConstant() {
        assertEquals("lab", NavDestinations.LAB)
    }

    @Test
    fun testLabRouteDiffersFromBottomTabsAndSettings() {
        assertNotEquals(NavDestinations.SETTINGS, NavDestinations.LAB)
        val bottomTabRoutes = BottomTab.tabs.map { it.route }
        assertFalse(bottomTabRoutes.contains(NavDestinations.LAB))
        for (tab in BottomTab.tabs) {
            assertNotEquals(tab.route, NavDestinations.LAB)
        }
    }
}
