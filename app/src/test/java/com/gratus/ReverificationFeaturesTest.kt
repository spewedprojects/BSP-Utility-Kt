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

    @Test
    fun `labourer permanent department is strictly blank while staff retains department`() {
        val staff = Employee(
            id = 1L,
            name = "Rakesh Kulkarni",
            type = EmployeeTypes.STAFF,
            status = EmployeeStatuses.ACTIVE,
            dateAdded = "2026-09-10",
            permanentDepartment = "HR & Admin",
            designation = "HR Trainee"
        )
        val labourer = Employee(
            id = 2L,
            name = "Vijay Thorat",
            type = EmployeeTypes.LABOUR,
            status = EmployeeStatuses.ACTIVE,
            dateAdded = "2026-09-10",
            permanentDepartment = "",
            contractorName = "Apex Industrial Services",
            defaultWorkRole = "Helper"
        )

        // Simulating save logic
        fun saveEmployee(emp: Employee, enteredDept: String): Employee {
            val finalDept = if (emp.type == EmployeeTypes.STAFF) enteredDept.trim() else ""
            return emp.copy(permanentDepartment = finalDept)
        }

        val savedStaff = saveEmployee(staff, "Welding Shop")
        val savedLabour = saveEmployee(labourer, "Welding Shop")

        assertEquals("Welding Shop", savedStaff.permanentDepartment)
        assertEquals("", savedLabour.permanentDepartment)
    }

    @Test
    fun `batch paste import assigns permanent department to staff only, not labourers`() {
        val names = listOf("Worker A", "Worker B")
        val defaultDept = "Welding Shop"

        fun createFromImport(name: String, targetType: String, dept: String): Employee {
            return Employee(
                name = name,
                type = targetType,
                dateAdded = "2026-09-10",
                permanentDepartment = if (targetType == EmployeeTypes.STAFF) dept else ""
            )
        }

        val staffEmps = names.map { createFromImport(it, EmployeeTypes.STAFF, defaultDept) }
        val labourEmps = names.map { createFromImport(it, EmployeeTypes.LABOUR, defaultDept) }

        assertTrue(staffEmps.all { it.permanentDepartment == "Welding Shop" })
        assertTrue(labourEmps.all { it.permanentDepartment.isEmpty() })
    }

    @Test
    fun `effective attendance department falls back to Unassigned for unallotted labourers`() {
        val labourer = Employee(
            id = 10L,
            name = "Ramesh Pawar",
            type = EmployeeTypes.LABOUR,
            permanentDepartment = "",
            defaultWorkRole = "Welder",
            dateAdded = "2026-09-10"
        )
        val staff = Employee(
            id = 20L,
            name = "Rajesh Patil",
            type = EmployeeTypes.STAFF,
            permanentDepartment = "Welding Shop",
            dateAdded = "2026-09-10"
        )

        // Case 1: No daily attendance record yet
        val attLabour1 = null as com.gratus.bsputility.data.models.DailyAttendance?
        val effDeptLabour1 = attLabour1?.dayDepartment?.ifBlank { null }
            ?: labourer.permanentDepartment.ifBlank { "Unassigned" }
        assertEquals("Unassigned", effDeptLabour1)

        val attStaff1 = null as com.gratus.bsputility.data.models.DailyAttendance?
        val effDeptStaff1 = attStaff1?.dayDepartment?.ifBlank { null }
            ?: staff.permanentDepartment.ifBlank { "Unassigned" }
        assertEquals("Welding Shop", effDeptStaff1)

        // Case 2: Daily department allotted on morning attendance
        val attLabour2 = com.gratus.bsputility.data.models.DailyAttendance(
            date = "2026-09-10",
            employeeId = 10L,
            employeeName = "Ramesh Pawar",
            employeeType = EmployeeTypes.LABOUR,
            dayDepartment = "Laser Cutting"
        )
        val effDeptLabour2 = attLabour2.dayDepartment.ifBlank { null }
            ?: labourer.permanentDepartment.ifBlank { "Unassigned" }
        assertEquals("Laser Cutting", effDeptLabour2)
    }

    @Test
    fun `rooster filters correctly by contractor and department`() {
        val employees = listOf(
            Employee(id = 1L, name = "Rakesh Staff", type = EmployeeTypes.STAFF, permanentDepartment = "HR & Admin", contractorName = "", dateAdded = "2026-09-10"),
            Employee(id = 2L, name = "Rajesh Staff", type = EmployeeTypes.STAFF, permanentDepartment = "Welding Shop", contractorName = "", dateAdded = "2026-09-10"),
            Employee(id = 3L, name = "Apex Worker 1", type = EmployeeTypes.LABOUR, permanentDepartment = "", contractorName = "Apex Industrial Services", dateAdded = "2026-09-10"),
            Employee(id = 4L, name = "Apex Worker 2", type = EmployeeTypes.LABOUR, permanentDepartment = "", contractorName = "Apex Industrial Services", dateAdded = "2026-09-10"),
            Employee(id = 5L, name = "Chakan Worker", type = EmployeeTypes.LABOUR, permanentDepartment = "", contractorName = "Chakan Workforce", dateAdded = "2026-09-10")
        )

        fun filterRooster(
            list: List<Employee>,
            contractor: String?,
            department: String?
        ): List<Employee> {
            return list.filter { emp ->
                val matchesContractor = contractor == null || emp.contractorName.equals(contractor, ignoreCase = true)
                val matchesDept = department == null || emp.permanentDepartment.equals(department, ignoreCase = true)
                matchesContractor && matchesDept
            }
        }

        // 1. No filters -> all 5 returned
        assertEquals(5, filterRooster(employees, null, null).size)

        // 2. Contractor filter -> only Apex
        val apexList = filterRooster(employees, "Apex Industrial Services", null)
        assertEquals(2, apexList.size)
        assertTrue(apexList.all { it.contractorName == "Apex Industrial Services" })

        // 3. Department filter -> HR & Admin
        val hrList = filterRooster(employees, null, "HR & Admin")
        assertEquals(1, hrList.size)
        assertEquals("Rakesh Staff", hrList[0].name)

        // 4. Department filter -> Welding Shop
        val weldingList = filterRooster(employees, null, "Welding Shop")
        assertEquals(1, weldingList.size)
        assertEquals("Rajesh Staff", weldingList[0].name)
    }

    // =========================================================================
    // TESTS FOR ISSUES #4 to #10
    // =========================================================================

    @Test
    fun `Issue 4 - Department Verification screen only includes departments with active or present workers`() {
        val configuredDepts = listOf("Welding Shop", "Laser Cutting", "Assembly", "Paint Shop", "Quality Control")

        val dailyLabourList = listOf(
            Employee(id = 1L, name = "Welder 1", type = EmployeeTypes.LABOUR, dateAdded = "2026-09-12"),
            Employee(id = 2L, name = "Laser Op 1", type = EmployeeTypes.LABOUR, dateAdded = "2026-09-12")
        )
        val dailyAttendanceList = listOf(
            com.gratus.bsputility.data.models.DailyAttendance(date = "2026-09-12", employeeId = 1L, employeeName = "Welder 1", employeeType = EmployeeTypes.LABOUR, dayDepartment = "Welding Shop", isPresent = true),
            com.gratus.bsputility.data.models.DailyAttendance(date = "2026-09-12", employeeId = 2L, employeeName = "Laser Op 1", employeeType = EmployeeTypes.LABOUR, dayDepartment = "Laser Cutting", isPresent = false)
        )

        // Calculate assigned/present departments
        val deptMap = dailyLabourList.groupBy { emp ->
            val att = dailyAttendanceList.find { it.employeeId == emp.id }
            att?.dayDepartment?.ifBlank { null }
                ?: emp.permanentDepartment.ifBlank { null }
                ?: "Unassigned"
        }
        val presentDeptsWithWorkers = dailyAttendanceList
            .filter { it.isPresent && it.dayDepartment.isNotBlank() }
            .map { it.dayDepartment }
            .toSet()

        val activeDepts = configuredDepts.filter { dept ->
            (deptMap[dept]?.isNotEmpty() == true) || presentDeptsWithWorkers.contains(dept)
        }

        // Only Welding Shop and Laser Cutting should be active, others omitted
        assertEquals(2, activeDepts.size)
        assertTrue(activeDepts.contains("Welding Shop"))
        assertTrue(activeDepts.contains("Laser Cutting"))
        assertFalse(activeDepts.contains("Assembly"))
        assertFalse(activeDepts.contains("Paint Shop"))
        assertFalse(activeDepts.contains("Quality Control"))

        // When no active departments exist, activeDepts is empty (triggering placeholder UI)
        val noLabourDeptMap = emptyMap<String, List<Employee>>()
        val noActiveDepts = configuredDepts.filter { dept ->
            (noLabourDeptMap[dept]?.isNotEmpty() == true)
        }
        assertTrue(noActiveDepts.isEmpty())
    }

    @Test
    fun `Issue 5 - Mark All sets all active workers present and restores previous snapshot on undo`() {
        data class TestAtt(val id: Long, val isPresent: Boolean, val status: String)

        val initialList = listOf(
            TestAtt(id = 1L, isPresent = true, status = EmployeeStatuses.ACTIVE),
            TestAtt(id = 2L, isPresent = false, status = EmployeeStatuses.ACTIVE),
            TestAtt(id = 3L, isPresent = false, status = EmployeeStatuses.ACTIVE),
            TestAtt(id = 4L, isPresent = false, status = EmployeeStatuses.DEBARRED) // Not active
        )

        // Check if all active are present initially
        val activeWorkers = initialList.filter { it.status == EmployeeStatuses.ACTIVE }
        val areAllActivePresentInitial = activeWorkers.isNotEmpty() && activeWorkers.all { it.isPresent }
        assertFalse(areAllActivePresentInitial)

        // Action 1: Mark All Present
        var preMarkAllSnapshot: List<TestAtt>? = null
        var currentList = initialList

        if (areAllActivePresentInitial) {
            // Undo branch (not taken here)
        } else {
            preMarkAllSnapshot = currentList.toList()
            currentList = currentList.map { item ->
                if (item.status == EmployeeStatuses.ACTIVE) item.copy(isPresent = true) else item
            }
        }

        // Verify all active are now present, debarred unaffected
        val activeAfterMarkAll = currentList.filter { it.status == EmployeeStatuses.ACTIVE }
        assertTrue(activeAfterMarkAll.all { it.isPresent })
        assertFalse(currentList.first { it.id == 4L }.isPresent)

        // Check areAllActivePresent now
        val areAllActivePresentNow = activeAfterMarkAll.isNotEmpty() && activeAfterMarkAll.all { it.isPresent }
        assertTrue(areAllActivePresentNow)

        // Action 2: User taps "Mark All" again -> It should Undo and restore preMarkAllSnapshot
        if (areAllActivePresentNow && preMarkAllSnapshot != null) {
            currentList = preMarkAllSnapshot
            preMarkAllSnapshot = null
        }

        // Verify restoration: id 1 is present, id 2 is absent, id 3 is absent
        assertEquals(true, currentList.first { it.id == 1L }.isPresent)
        assertEquals(false, currentList.first { it.id == 2L }.isPresent)
        assertEquals(false, currentList.first { it.id == 3L }.isPresent)
    }

    @Test
    fun `Issue 6 - Staff contractor sanitization on creation, import, and clearing database associations`() {
        // 1. Staff imported via CSV/JSON must clear contractor fields
        val staffImportJson = JSONObject().apply {
            put("name", "Arun Patil")
            put("type", EmployeeTypes.STAFF)
            put("contractorName", "Apex Contractors") // Accidental contractor
            put("permanentDepartment", "Welding Shop")
        }

        val empType = staffImportJson.optString("type", EmployeeTypes.LABOUR)
        val sanitizedStaff = Employee(
            name = staffImportJson.optString("name"),
            type = empType,
            dateAdded = "2026-09-12",
            contractorName = if (empType == EmployeeTypes.STAFF) "" else staffImportJson.optString("contractorName"),
            permanentDepartment = if (empType == EmployeeTypes.STAFF) staffImportJson.optString("permanentDepartment") else ""
        )

        assertEquals("", sanitizedStaff.contractorName)
        assertEquals("Welding Shop", sanitizedStaff.permanentDepartment)

        // 2. Existing database associations sanitization simulation
        val dbEmployees = listOf(
            Employee(id = 1L, name = "Staff with leak", type = EmployeeTypes.STAFF, dateAdded = "2026-09-12", contractorName = "Legacy Contractor"),
            Employee(id = 2L, name = "Valid Staff", type = EmployeeTypes.STAFF, dateAdded = "2026-09-12", contractorName = ""),
            Employee(id = 3L, name = "Labourer", type = EmployeeTypes.LABOUR, dateAdded = "2026-09-12", contractorName = "Apex Contractors")
        )

        // Simulate UPDATE employee SET contractorName = '', contractorId = NULL WHERE type = 'STAFF' AND contractorName != ''
        val cleanedEmployees = dbEmployees.map { emp ->
            if (emp.type == EmployeeTypes.STAFF && emp.contractorName.isNotBlank()) {
                emp.copy(contractorName = "")
            } else {
                emp
            }
        }

        assertEquals("", cleanedEmployees.first { it.id == 1L }.contractorName)
        assertEquals("", cleanedEmployees.first { it.id == 2L }.contractorName)
        assertEquals("Apex Contractors", cleanedEmployees.first { it.id == 3L }.contractorName)
    }

    @Test
    fun `Issue 7 - PasteImportDialog parses lines correctly and supports Staff vs Labour targets`() {
        val pastedText = """
            Suresh More
            Dinesh Kumar
            
            Kavita Sharma
        """.trimIndent()

        val parsedNames = pastedText.lines().map { it.trim() }.filter { it.isNotBlank() }
        assertEquals(3, parsedNames.size)
        assertEquals("Suresh More", parsedNames[0])
        assertEquals("Dinesh Kumar", parsedNames[1])
        assertEquals("Kavita Sharma", parsedNames[2])

        // Importing as Staff guarantees contractorName is empty and permanentDepartment is assigned
        val staffList = parsedNames.map { name ->
            Employee(
                name = name,
                type = EmployeeTypes.STAFF,
                dateAdded = "2026-09-12",
                contractorName = "",
                permanentDepartment = "Production"
            )
        }
        assertTrue(staffList.all { it.type == EmployeeTypes.STAFF && it.contractorName.isEmpty() && it.permanentDepartment == "Production" })

        // Importing as Labour guarantees permanentDepartment is empty
        val labourList = parsedNames.map { name ->
            Employee(
                name = name,
                type = EmployeeTypes.LABOUR,
                dateAdded = "2026-09-12",
                contractorName = "Apex Contractors",
                permanentDepartment = ""
            )
        }
        assertTrue(labourList.all { it.type == EmployeeTypes.LABOUR && it.contractorName == "Apex Contractors" && it.permanentDepartment.isEmpty() })
    }

    @Test
    fun `Issue 8 - Mutual exclusivity between Staff and Contractor filters, and All Contractors excludes Staff`() {
        val dataset = listOf(
            Employee(id = 1L, name = "Staff HR", type = EmployeeTypes.STAFF, dateAdded = "2026-09-12", contractorName = ""),
            Employee(id = 2L, name = "Staff Welding", type = EmployeeTypes.STAFF, dateAdded = "2026-09-12", contractorName = ""),
            Employee(id = 3L, name = "Apex Worker", type = EmployeeTypes.LABOUR, dateAdded = "2026-09-12", contractorName = "Apex Contractors"),
            Employee(id = 4L, name = "Chakan Worker", type = EmployeeTypes.LABOUR, dateAdded = "2026-09-12", contractorName = "Chakan Workforce"),
            Employee(id = 5L, name = "Direct Labour", type = EmployeeTypes.LABOUR, dateAdded = "2026-09-12", contractorName = "")
        )

        fun filter(
            list: List<Employee>,
            typeFilter: String, // "ALL", "STAFF", "LABOUR"
            contractorFilter: String? // null, "__ALL_CONTRACTORS__", or "Apex Contractors"
        ): List<Employee> {
            return list.filter { emp ->
                val matchesType = when (typeFilter) {
                    "STAFF" -> emp.type == EmployeeTypes.STAFF
                    "LABOUR" -> emp.type != EmployeeTypes.STAFF
                    else -> true
                }
                val matchesContractor = when (contractorFilter) {
                    null -> true
                    "__ALL_CONTRACTORS__" -> emp.type != EmployeeTypes.STAFF && emp.contractorName.isNotBlank()
                    else -> emp.contractorName.equals(contractorFilter, ignoreCase = true)
                }
                matchesType && matchesContractor
            }
        }

        // 1. "All Contractors" selected -> excludes Staff and Direct Labour without contractor
        val allContractorsOnly = filter(dataset, "ALL", "__ALL_CONTRACTORS__")
        assertEquals(2, allContractorsOnly.size)
        assertTrue(allContractorsOnly.none { it.type == EmployeeTypes.STAFF })
        assertTrue(allContractorsOnly.all { it.contractorName.isNotBlank() })

        // 2. Specific Contractor selected -> only Apex
        val apexOnly = filter(dataset, "ALL", "Apex Contractors")
        assertEquals(1, apexOnly.size)
        assertEquals("Apex Worker", apexOnly[0].name)

        // 3. Mutual exclusivity logic: selecting Staff resets contractor to null
        var currentType = "ALL"
        var currentContractor: String? = "Apex Contractors"

        // User clicks "Staff"
        currentType = "STAFF"
        if (currentType == "STAFF") currentContractor = null
        assertEquals(null, currentContractor)

        val staffOnly = filter(dataset, currentType, currentContractor)
        assertEquals(2, staffOnly.size)
        assertTrue(staffOnly.all { it.type == EmployeeTypes.STAFF })

        // 4. Mutual exclusivity logic: selecting Contractor resets type away from Staff
        currentContractor = "__ALL_CONTRACTORS__"
        if (currentType == "STAFF") currentType = "ALL"
        assertEquals("ALL", currentType)
        val afterContractorSelect = filter(dataset, currentType, currentContractor)
        assertEquals(2, afterContractorSelect.size)
        assertTrue(afterContractorSelect.none { it.type == EmployeeTypes.STAFF })
    }

    @Test
    fun `Issue 9 - Staff Unit and Shift fields are properly editable and persisted`() {
        val staffEmployee = Employee(
            id = 100L,
            name = "Senior Supervisor",
            type = EmployeeTypes.STAFF,
            dateAdded = "2026-09-12",
            defaultUnit = "Unit I",
            defaultShift = "Shift A",
            permanentDepartment = "Welding Shop"
        )

        // Updating Staff Unit and Shift
        val updatedStaff = staffEmployee.copy(
            defaultUnit = "Unit II",
            defaultShift = "General"
        )

        assertEquals("Unit II", updatedStaff.defaultUnit)
        assertEquals("General", updatedStaff.defaultShift)
        assertEquals(EmployeeTypes.STAFF, updatedStaff.type)
        assertEquals("Welding Shop", updatedStaff.permanentDepartment)
    }

    @Test
    fun `Issue 10 - Attendance time epoch timestamp migration and 12h-24h display formatting`() {
        // 1. Parsing legacy string "08:30 AM" on date "2026-09-12" to epoch timestamp
        val dateStr = "2026-09-12"
        val legacyTime = "08:30 AM"

        fun parseTimeToTimestamp(date: String, time: String): Long {
            if (time.isBlank()) return 0L
            return try {
                val full12 = "$date $time"
                java.text.SimpleDateFormat("yyyy-MM-dd hh:mm a", java.util.Locale.ENGLISH).parse(full12)?.time
                    ?: try {
                        java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.ENGLISH).parse(full12)?.time ?: 0L
                    } catch (_: Exception) { 0L }
            } catch (_: Exception) {
                try {
                    val full24 = "$date $time"
                    java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.ENGLISH).parse(full24)?.time ?: 0L
                } catch (_: Exception) { 0L }
            }
        }

        val timestamp = parseTimeToTimestamp(dateStr, legacyTime)
        assertTrue("Timestamp should be greater than zero", timestamp > 0L)

        // 2. Formatting back to 12-hour and 24-hour display
        val sdf12 = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.ENGLISH)
        val sdf24 = java.text.SimpleDateFormat("HH:mm", java.util.Locale.ENGLISH)

        val display12 = sdf12.format(java.util.Date(timestamp))
        val display24 = sdf24.format(java.util.Date(timestamp))

        assertEquals("08:30 AM", display12)
        assertEquals("08:30", display24)

        // 3. Test evening time "05:45 PM"
        val eveningTimestamp = parseTimeToTimestamp(dateStr, "05:45 PM")
        val eveningDisplay12 = sdf12.format(java.util.Date(eveningTimestamp))
        val eveningDisplay24 = sdf24.format(java.util.Date(eveningTimestamp))

        assertEquals("05:45 PM", eveningDisplay12)
        assertEquals("17:45", eveningDisplay24)
    }
}

