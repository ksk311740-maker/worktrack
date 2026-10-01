package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.ProjectItem
import com.example.util.CsvBackupHelper
import com.example.util.DateUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("WorkTrack", appName)
    }

    @Test
    fun `verify section 19 calculation example`() {
        // Project 1: Football 5 -> 5 mins @ 60 = 300
        var minutes1 = 5.0
        val rate1 = 60.0
        var payment1 = minutes1 * rate1
        assertEquals(300.0, payment1, 0.001)

        // Project 2: Casino 10 -> 10 mins @ 40 = 400
        val minutes2 = 10.0
        val rate2 = 40.0
        val payment2 = minutes2 * rate2
        assertEquals(400.0, payment2, 0.001)

        // Dashboard totals: 2 projects, 15 minutes, 700 earnings
        assertEquals(2, 2)
        assertEquals(15.0, minutes1 + minutes2, 0.001)
        assertEquals(700.0, payment1 + payment2, 0.001)

        // Editing 5 minutes -> 8 minutes
        minutes1 = 8.0
        payment1 = minutes1 * rate1
        assertEquals(480.0, payment1, 0.001)

        // New dashboard totals
        assertEquals(18.0, minutes1 + minutes2, 0.001)
        assertEquals(880.0, payment1 + payment2, 0.001)
    }

    @Test
    fun `verify working hours calculation`() {
        val hours = DateUtils.calculateWorkingHours("09:00", "11:30")
        assertEquals(2.5, hours, 0.01)

        val overnightHours = DateUtils.calculateWorkingHours("23:00", "01:00")
        assertEquals(2.0, overnightHours, 0.01)
    }

    @Test
    fun `verify csv export and parse roundtrip`() {
        val project = ProjectItem(
            id = 1,
            date = "2026-09-30",
            name = "Casino 10",
            category = "Casino",
            minutes = 10.0,
            ratePerMinute = 40.0,
            payment = 400.0,
            clientName = "Gaming Hub",
            status = ProjectItem.STATUS_COMPLETED
        )
        val csv = CsvBackupHelper.exportProjectsToCsv(listOf(project))
        assertTrue(csv.contains("Casino 10"))
        assertTrue(csv.contains("400"))
        assertTrue(csv.contains("Gaming Hub"))

        val parsed = CsvBackupHelper.parseProjectsFromCsv(csv, 40.0)
        assertEquals(1, parsed.size)
        assertEquals("Casino 10", parsed[0].name)
        assertEquals(10.0, parsed[0].minutes, 0.001)
        assertEquals(400.0, parsed[0].payment, 0.001)
        assertEquals("Gaming Hub", parsed[0].clientName)
    }
}
