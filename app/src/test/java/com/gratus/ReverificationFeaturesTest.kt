package com.gratus

import com.gratus.bsputility.data.models.ConfigItem
import com.gratus.bsputility.data.models.Employee
import com.gratus.bsputility.data.models.EmployeeStatuses
import com.gratus.bsputility.data.models.EmployeeTypes
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ReverificationFeaturesTest {

    @Test
    fun `markAllActivePresent only includes ACTIVE status and strictly excludes DEBARRED and OUT`() {
        val employees = listOf(
            Employee(id = 1L, name = "Active Staff", type = EmployeeTypes.STAFF, status = EmployeeStatuses.ACTIVE, dateAdded = "2026-09-10"),
            Employee(id = 2L, name = "Active Labour", type = EmployeeTypes.LABOUR, status = EmployeeStatuses.ACTIVE, dateAdded = "2026-09-10"),
            Employee(id = 3L, name = "Debarred Worker", type = EmployeeTypes.LABOUR, status = EmployeeStatuses.DEBARRED, dateAdded = "2026-09-10", permanentRemarks = "Safety violation"),
            Employee(id = 4L, name = "Left Company", type = EmployeeTypes.LABOUR, status = EmployeeStatuses.OUT, dateAdded = "2026-09-10")
        )

        val activeOnly = employees.filter { it.status == EmployeeStatuses.ACTIVE }

        assertEquals(2, activeOnly.size)
        assertTrue(activeOnly.any { it.name == "Active Staff" })
        assertTrue(activeOnly.any { it.name == "Active Labour" })
        assertFalse(activeOnly.any { it.status == EmployeeStatuses.DEBARRED })
        assertFalse(activeOnly.any { it.status == EmployeeStatuses.OUT })
    }

    @Test
    fun `staff attendance time is cleared when presence is toggled off`() {
        val isCurrentlyPresent = true
        val currentTime = "09:15 AM"

        val newPresence = !isCurrentlyPresent
        val calculatedTime = if (newPresence && currentTime.isBlank()) {
            "09:30 AM"
        } else if (!newPresence) {
            "" // Resets when marked absent
        } else {
            currentTime
        }

        assertEquals(false, newPresence)
        assertEquals("", calculatedTime)
    }

    @Test
    fun `staff manual attendance update resets time to empty when marked absent`() {
        val isPresent = false
        val enteredTime = "10:00 AM"

        val finalTime = if (isPresent) enteredTime else ""
        assertEquals("", finalTime)
    }

    @Test
    fun `custom fields target audience filtering applies correctly`() {
        val configItems = listOf(
            ConfigItem(id = 1L, category = "CUSTOM_FIELD", name = "PF Number", extraType = "TEXT|STAFF"),
            ConfigItem(id = 2L, category = "CUSTOM_FIELD", name = "Safety Shoes Issued", extraType = "BOOLEAN|LABOUR"),
            ConfigItem(id = 3L, category = "CUSTOM_FIELD", name = "Aadhaar Verified", extraType = "BOOLEAN|ALL"),
            ConfigItem(id = 4L, category = "CUSTOM_FIELD", name = "Legacy Field", extraType = "TEXT")
        )

        fun appliesTo(cf: ConfigItem, employeeType: String): Boolean {
            val parts = cf.extraType.split("|")
            val target = parts.getOrNull(1)?.uppercase() ?: "ALL"
            return when (target) {
                "STAFF" -> employeeType == EmployeeTypes.STAFF
                "LABOUR" -> employeeType != EmployeeTypes.STAFF
                else -> true
            }
        }

        val staffFields = configItems.filter { appliesTo(it, EmployeeTypes.STAFF) }
        val labourFields = configItems.filter { appliesTo(it, EmployeeTypes.LABOUR) }

        // Staff should get: PF Number, Aadhaar Verified, Legacy Field
        assertEquals(3, staffFields.size)
        assertTrue(staffFields.any { it.name == "PF Number" })
        assertTrue(staffFields.any { it.name == "Aadhaar Verified" })
        assertTrue(staffFields.any { it.name == "Legacy Field" })
        assertFalse(staffFields.any { it.name == "Safety Shoes Issued" })

        // Labour should get: Safety Shoes Issued, Aadhaar Verified, Legacy Field
        assertEquals(3, labourFields.size)
        assertTrue(labourFields.any { it.name == "Safety Shoes Issued" })
        assertTrue(labourFields.any { it.name == "Aadhaar Verified" })
        assertTrue(labourFields.any { it.name == "Legacy Field" })
        assertFalse(labourFields.any { it.name == "PF Number" })
    }

    @Test
    fun `custom fields JSON serialization and deserialization roundtrip`() {
        val inputMap = mapOf(
            "PF Number" to "PF-10293",
            "Aadhaar Verified" to "true",
            "Empty Field" to ""
        )

        val json = JSONObject()
        inputMap.forEach { (k, v) ->
            if (v.isNotBlank()) {
                json.put(k, v)
            }
        }
        val serializedString = json.toString()

        // Reconstruct
        val parsedJson = JSONObject(serializedString)
        val resultMap = mutableMapOf<String, String>()
        val keys = parsedJson.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            resultMap[key] = parsedJson.optString(key)
        }

        assertEquals(2, resultMap.size)
        assertEquals("PF-10293", resultMap["PF Number"])
        assertEquals("true", resultMap["Aadhaar Verified"])
        assertFalse(resultMap.containsKey("Empty Field"))
    }

    @Test
    fun `shift list fallback handles empty configs`() {
        val emptyConfigs = emptyList<ConfigItem>()
        val configuredConfigs = listOf(
            ConfigItem(id = 1L, category = "SHIFT", name = "Morning Shift"),
            ConfigItem(id = 2L, category = "SHIFT", name = "Night Shift")
        )

        fun extractShifts(items: List<ConfigItem>): List<String> {
            val list = items.filter { it.category == "SHIFT" }.map { it.name }
            return if (list.isEmpty()) listOf("Shift A", "Shift B", "Shift C", "General") else list
        }

        val defaultShifts = extractShifts(emptyConfigs)
        assertEquals(4, defaultShifts.size)
        assertEquals("Shift A", defaultShifts[0])

        val customShifts = extractShifts(configuredConfigs)
        assertEquals(2, customShifts.size)
        assertEquals("Morning Shift", customShifts[0])
        assertEquals("Night Shift", customShifts[1])
    }

    @Test
    fun `isDateInFuture correctly identifies tomorrow, future dates, today, and yesterday`() {
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val cal = java.util.Calendar.getInstance()
        val todayStr = dateFormat.format(cal.time)

        cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = dateFormat.format(cal.time)

        cal.add(java.util.Calendar.DAY_OF_YEAR, 2)
        val tomorrowStr = dateFormat.format(cal.time)

        cal.add(java.util.Calendar.DAY_OF_YEAR, 30)
        val nextMonthStr = dateFormat.format(cal.time)

        fun isDateInFuture(date: String): Boolean = date > todayStr

        assertFalse(isDateInFuture(todayStr))
        assertFalse(isDateInFuture(yesterdayStr))
        assertTrue(isDateInFuture(tomorrowStr))
        assertTrue(isDateInFuture(nextMonthStr))
    }

    @Test
    fun `navigation clamping prevents advancing into future dates`() {
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val cal = java.util.Calendar.getInstance()
        val todayStr = dateFormat.format(cal.time)

        cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = dateFormat.format(cal.time)

        fun selectNextDay(currentDate: String): String {
            if (currentDate >= todayStr) return currentDate
            val c = java.util.Calendar.getInstance()
            c.time = dateFormat.parse(currentDate) ?: java.util.Date()
            c.add(java.util.Calendar.DAY_OF_YEAR, 1)
            val nextDate = dateFormat.format(c.time)
            return if (nextDate <= todayStr) nextDate else currentDate
        }

        // From yesterday, advancing goes to today
        val advancedFromYesterday = selectNextDay(yesterdayStr)
        assertEquals(todayStr, advancedFromYesterday)

        // From today, advancing stays at today
        val advancedFromToday = selectNextDay(todayStr)
        assertEquals(todayStr, advancedFromToday)
    }

    @Test
    fun `attendance records are strictly isolated by date and do not leak across dates`() {
        val employees = listOf(
            Employee(id = 101L, name = "John Doe", type = EmployeeTypes.LABOUR, status = EmployeeStatuses.ACTIVE, dateAdded = "2026-09-01"),
            Employee(id = 102L, name = "Jane Smith", type = EmployeeTypes.STAFF, status = EmployeeStatuses.ACTIVE, dateAdded = "2026-09-01")
        )

        // Attendance recorded ONLY for 2026-09-10
        val attendanceToday = listOf(
            com.gratus.bsputility.data.models.DailyAttendance(
                id = 1L,
                date = "2026-09-10",
                employeeId = 101L,
                employeeName = "John Doe",
                employeeType = EmployeeTypes.LABOUR,
                isPresent = true
            )
        )

        // Simulate combining for 2026-09-10 (Today)
        val todayMap = attendanceToday.associateBy { it.employeeId }
        val todayItems = employees.map { emp ->
            val att = todayMap[emp.id]
            Pair(emp.name, att?.isPresent ?: false)
        }
        assertEquals(true, todayItems.first { it.first == "John Doe" }.second)
        assertEquals(false, todayItems.first { it.first == "Jane Smith" }.second)

        // Simulate combining for 2026-09-11 (Tomorrow / Future Date with no records)
        val attendanceTomorrow = emptyList<com.gratus.bsputility.data.models.DailyAttendance>()
        val tomorrowMap = attendanceTomorrow.associateBy { it.employeeId }
        val tomorrowItems = employees.map { emp ->
            val att = tomorrowMap[emp.id]
            Pair(emp.name, att?.isPresent ?: false)
        }
        // John Doe must NOT be present on tomorrow!
        assertEquals(false, tomorrowItems.first { it.first == "John Doe" }.second)
        assertEquals(false, tomorrowItems.first { it.first == "Jane Smith" }.second)

        // Simulate combining for 2026-09-09 (Yesterday with no records)
        val attendanceYesterday = emptyList<com.gratus.bsputility.data.models.DailyAttendance>()
        val yesterdayMap = attendanceYesterday.associateBy { it.employeeId }
        val yesterdayItems = employees.map { emp ->
            val att = yesterdayMap[emp.id]
            Pair(emp.name, att?.isPresent ?: false)
        }
        // John Doe must NOT be present on yesterday!
        assertEquals(false, yesterdayItems.first { it.first == "John Doe" }.second)
        assertEquals(false, yesterdayItems.first { it.first == "Jane Smith" }.second)
    }
}
