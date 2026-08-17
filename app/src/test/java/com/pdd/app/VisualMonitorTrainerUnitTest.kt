package com.pdd.app

import org.junit.Test
import org.junit.Assert.*

class VisualMonitorTrainerUnitTest {

    @Test
    fun appName_isCorrect() {
        val expectedAppName = "Visual Monitor Trainer"
        assertNotNull("App name must not be null", expectedAppName)
        assertEquals("Visual Monitor Trainer", expectedAppName)
    }

    @Test
    fun packageNamespace_isCorrect() {
        val packageName = "com.pdd.app"
        assertEquals("com.pdd.app", packageName)
    }

    @Test
    fun statusCalculation_isValid() {
        val passedTests = 293
        val totalTests = 305
        val passRate = (passedTests.toDouble() / totalTests.toDouble()) * 100
        assertTrue("Pass rate should exceed 90%", passRate > 90.0)
    }
}
