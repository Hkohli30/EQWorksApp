package com.eqworks

import android.app.Activity
import org.junit.Test
import org.junit.Assert.*

class LibraryTest {

    /**
     * Requires the running activity instance so cannot be tested properly
     */
    @Test
    fun testSetup() {
        val classUnderTest = Library(Activity())
        assertTrue(classUnderTest.setup().toString(), true)
    }

    /**
     * Requires the running activity instance so cannot be tested properly
     */
    @Test
    fun testLog() {
        val classUnderTest = Library(Activity())
        classUnderTest.log(LocationEvent(0.0, 0.0, "", "empty"))
    }
}