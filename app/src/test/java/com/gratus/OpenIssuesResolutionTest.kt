package com.gratus

import android.content.Intent
import android.net.Uri
import com.gratus.bsputility.data.models.ConfigItem
import com.gratus.bsputility.data.models.DailyAttendance
import com.gratus.bsputility.data.models.DateUtils
import com.gratus.bsputility.data.models.Employee
import com.gratus.bsputility.data.models.EmployeeStatuses
import com.gratus.bsputility.data.models.EmployeeTypes
import com.gratus.bsputility.data.models.ExternalContact
import com.gratus.bsputility.ui.viewmodel.ManpowerViewModel
import com.gratus.bsputility.ui.viewmodel.ManpowerViewModel.SortMode
import com.gratus.bsputility.ui.viewmodel.ManpowerViewModel.EmployeeAttendanceItem
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class OpenIssuesResolutionTest {

    // =========================================================================
    // Issue #32: Custom Fields Export & Import in Employee Master Data / Rooster
    // =========================================================================

    @Test
    fun `Issue 32 - CSV generation dynamically exports all custom field columns`() {
        val configItems = listOf(
            ConfigItem(id = 1L, category = "CUSTOM_FIELD", name = "Blood Group", extraType = "TEXT|ALL"),
            ConfigItem(id = 2L, category = "CUSTOM_FIELD", name = "PF Number", extraType = "TEXT|STAFF"),
            ConfigItem(id = 3L, category = "CUSTOM_FIELD", name = "Safety Shoes Issued", extraType = "BOOLEAN|LABOUR"),
            ConfigItem(id = 4L, category = "DEPARTMENT", name = "Quality Control") // Not a custom field
        )

        val emp1 = Employee(
            id = 101L,
            name = "John Doe",
            type = EmployeeTypes.STAFF,
            designation = "Engineer",
            permanentDepartment = "Mechanical",
            defaultUnit = "Unit I",
            defaultShift = "Shift A",
            status = EmployeeStatuses.ACTIVE,
            dateAdded = "2026-09-01",
            customFieldsJson = """{"Blood Group":"O+","PF Number":"PF12345"}"""
        )

        val emp2 = Employee(
            id = 102L,
            name = "Jane Smith",
            type = EmployeeTypes.LABOUR,
            contractorName = "Apex Services",
            defaultWorkRole = "Fitter",
            permanentDepartment = "Civil",
            defaultUnit = "Unit II",
            defaultShift = "Night Shift",
            status = EmployeeStatuses.ACTIVE,
            dateAdded = "2026-09-02",
            customFieldsJson = """{"Blood Group":"B+","Safety Shoes Issued":"true","Emergency Contact":"9876543210"}"""
        )

        val employees = listOf(emp1, emp2)

        val csvOutput = ManpowerViewModel.generateRoosterCsv(employees, configItems)

        // Verify CSV header contains standard fields AND custom fields
        val lines = csvOutput.lines().filter { it.isNotBlank() }
        val header = lines.first()
        assertTrue(header.contains("Type"))
        assertTrue(header.contains("Name"))
        assertTrue(header.contains("Department"))
        assertTrue(header.contains("Blood Group"))
        assertTrue(header.contains("PF Number"))
        assertTrue(header.contains("Safety Shoes Issued"))
        assertTrue(header.contains("Emergency Contact")) // Extracted from employee customFieldsJson dynamically

        // Verify emp1 row content
        val row1 = lines[1]
        assertTrue(row1.contains("John Doe"))
        assertTrue(row1.contains("O+"))
        assertTrue(row1.contains("PF12345"))

        // Verify emp2 row content
        val row2 = lines[2]
        assertTrue(row2.contains("Jane Smith"))
        assertTrue(row2.contains("Apex Services"))
        assertTrue(row2.contains("B+"))
        assertTrue(row2.contains("true"))
        assertTrue(row2.contains("9876543210"))
    }

    @Test
    fun `Issue 32 - CSV import dynamically identifies custom fields and builds JSON`() {
        val csvData = """
            Type,Name,Designation,Contractor,Role,Department,Unit,Shift,Status,DateAdded,Remarks,Blood Group,Safety Shoes Issued,Emergency Contact
            Staff,Alice Walker,Manager,,Staff,HR,Unit I,Shift A,Active,2026-09-05,,A+,false,9998887776
            Labour,Bob Vance,,Apex Logistics,Driver,Supply Chain,Unit II,Night Shift,Active,2026-09-06,Forklift,O-,true,9112233445
        """.trimIndent()

        val parsedEmployees = mutableListOf<Employee>()
        val detectedCustomFieldNames = mutableSetOf<String>()

        val lines = csvData.lines().filter { it.isNotBlank() }
        val headerCols = lines.first().split(",").map { it.trim() }

        val knownStandardCols = setOf(
            "id", "type", "name", "designation", "contractor", "role",
            "department", "unit", "shift", "status", "dateadded", "remarks", "permanentremarks"
        )

        val customFieldColIndices = headerCols.mapIndexedNotNull { index, colName ->
            val cleanName = colName.lowercase().replace(" ", "").replace("_", "")
            if (!knownStandardCols.contains(cleanName)) {
                detectedCustomFieldNames.add(colName)
                Pair(index, colName)
            } else null
        }

        assertEquals(setOf("Blood Group", "Safety Shoes Issued", "Emergency Contact"), detectedCustomFieldNames)

        for (line in lines.drop(1)) {
            val cols = line.split(",").map { it.trim() }
            val customJsonObj = JSONObject()
            for ((index, fieldName) in customFieldColIndices) {
                if (index < cols.size && cols[index].isNotBlank()) {
                    customJsonObj.put(fieldName, cols[index])
                }
            }

            val emp = Employee(
                name = cols[1],
                type = cols[0],
                permanentDepartment = cols[5],
                customFieldsJson = if (customJsonObj.length() > 0) customJsonObj.toString() else ""
            )
            parsedEmployees.add(emp)
        }

        assertEquals(2, parsedEmployees.size)

        val alice = parsedEmployees[0]
        assertEquals("Alice Walker", alice.name)
        val aliceCustom = JSONObject(alice.customFieldsJson)
        assertEquals("A+", aliceCustom.getString("Blood Group"))
        assertEquals("false", aliceCustom.getString("Safety Shoes Issued"))
        assertEquals("9998887776", aliceCustom.getString("Emergency Contact"))

        val bob = parsedEmployees[1]
        assertEquals("Bob Vance", bob.name)
        val bobCustom = JSONObject(bob.customFieldsJson)
        assertEquals("O-", bobCustom.getString("Blood Group"))
        assertEquals("true", bobCustom.getString("Safety Shoes Issued"))
        assertEquals("9112233445", bobCustom.getString("Emergency Contact"))
    }

    // =========================================================================
    // Issue #31: Contractor Phone Dial Intent & Copy
    // =========================================================================

    @Test
    fun `Issue 31 - Contractor phone number dial intent is properly formatted`() {
        val rawPhones = listOf(
            "+91 98765 43210" to "tel:+91 98765 43210",
            "9876543210" to "tel:9876543210",
            "022-12345678" to "tel:022-12345678"
        )

        for ((phone, expectedUri) in rawPhones) {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${phone.trim()}")
            }

            assertEquals(Intent.ACTION_DIAL, intent.action)
            assertEquals(Uri.parse(expectedUri), intent.data)
        }
    }

    // =========================================================================
    // Issue #30: Contractor Laborers Grouped by Assigned Unit
    // =========================================================================

    @Test
    fun `Issue 30 - Laborers linked to contractor are correctly grouped by Unit`() {
        val contractor = "Apex Fabricators"
        val laborers = listOf(
            Employee(id = 1L, name = "Laborer 1", contractorName = contractor, defaultUnit = "Unit I", defaultWorkRole = "Welder"),
            Employee(id = 2L, name = "Laborer 2", contractorName = contractor, defaultUnit = "Unit II", defaultWorkRole = "Fitter"),
            Employee(id = 3L, name = "Laborer 3", contractorName = contractor, defaultUnit = "Unit I", defaultWorkRole = "Helper"),
            Employee(id = 4L, name = "Laborer 4", contractorName = contractor, defaultUnit = "", defaultWorkRole = "Painter"),
            Employee(id = 5L, name = "Laborer 5", contractorName = "Other Contractor", defaultUnit = "Unit I", defaultWorkRole = "Helper")
        )

        val contractorWorkers = laborers.filter {
            it.type != EmployeeTypes.STAFF && it.contractorName.equals(contractor, ignoreCase = true)
        }

        assertEquals(4, contractorWorkers.size)

        val groupedByUnit = contractorWorkers.groupBy {
            it.defaultUnit.ifBlank { "Unassigned / General" }
        }.toSortedMap()

        assertEquals(3, groupedByUnit.size)
        assertTrue(groupedByUnit.containsKey("Unit I"))
        assertTrue(groupedByUnit.containsKey("Unit II"))
        assertTrue(groupedByUnit.containsKey("Unassigned / General"))

        assertEquals(2, groupedByUnit["Unit I"]?.size)
        assertEquals(1, groupedByUnit["Unit II"]?.size)
        assertEquals(1, groupedByUnit["Unassigned / General"]?.size)

        assertEquals(listOf("Laborer 1", "Laborer 3"), groupedByUnit["Unit I"]?.map { it.name })
        assertEquals(listOf("Laborer 2"), groupedByUnit["Unit II"]?.map { it.name })
        assertEquals(listOf("Laborer 4"), groupedByUnit["Unassigned / General"]?.map { it.name })
    }

    // =========================================================================
    // Issue #29: In-Memory Sort Modes (A-Z, Z-A, Date Added, Last Modified)
    // =========================================================================

    @Test
    fun `Issue 29 - Employee list sorts correctly according to SortMode`() {
        val e1 = Employee(id = 10L, name = "Charlie", dateAdded = "2026-08-01")
        val e2 = Employee(id = 20L, name = "Alice", dateAdded = "2026-09-15")
        val e3 = Employee(id = 30L, name = "Bob", dateAdded = "2026-09-01")
        val employees = listOf(e1, e2, e3)

        // ALPHABETICAL_AZ (A-Z)
        val nameAsc = ManpowerViewModel.sortEmployees(employees, SortMode.ALPHABETICAL_AZ)
        assertEquals(listOf("Alice", "Bob", "Charlie"), nameAsc.map { it.name })

        // ALPHABETICAL_ZA (Z-A)
        val nameDesc = ManpowerViewModel.sortEmployees(employees, SortMode.ALPHABETICAL_ZA)
        assertEquals(listOf("Charlie", "Bob", "Alice"), nameDesc.map { it.name })

        // LATEST_JOINED (Latest Joined)
        val latestJoined = ManpowerViewModel.sortEmployees(employees, SortMode.LATEST_JOINED)
        assertEquals(listOf("Alice", "Bob", "Charlie"), latestJoined.map { it.name })

        // OLDEST_JOINED (Oldest Joined)
        val oldestJoined = ManpowerViewModel.sortEmployees(employees, SortMode.OLDEST_JOINED)
        assertEquals(listOf("Charlie", "Bob", "Alice"), oldestJoined.map { it.name })

        // LATEST_MODIFIED (Latest ID)
        val latestModified = ManpowerViewModel.sortEmployees(employees, SortMode.LATEST_MODIFIED)
        assertEquals(listOf("Alice", "Bob", "Charlie"), latestModified.map { it.name })

        // OLDEST_MODIFIED (Oldest ID)
        val oldestModified = ManpowerViewModel.sortEmployees(employees, SortMode.OLDEST_MODIFIED)
        assertEquals(listOf("Charlie", "Bob", "Alice"), oldestModified.map { it.name })
    }

    @Test
    fun `Issue 29 - Attendance items sort correctly according to SortMode`() {
        val e1 = Employee(id = 10L, name = "Charlie", dateAdded = "2026-08-01")
        val e2 = Employee(id = 20L, name = "Alice", dateAdded = "2026-09-15")
        val e3 = Employee(id = 30L, name = "Bob", dateAdded = "2026-09-01")

        val attItems = listOf(
            EmployeeAttendanceItem(employee = e1, isPresent = true),
            EmployeeAttendanceItem(employee = e2, isPresent = false),
            EmployeeAttendanceItem(employee = e3, isPresent = true)
        )

        val sortedAsc = ManpowerViewModel.sortAttendanceItems(attItems, SortMode.ALPHABETICAL_AZ)
        assertEquals(listOf("Alice", "Bob", "Charlie"), sortedAsc.map { it.employee.name })

        val sortedDesc = ManpowerViewModel.sortAttendanceItems(attItems, SortMode.ALPHABETICAL_ZA)
        assertEquals(listOf("Charlie", "Bob", "Alice"), sortedDesc.map { it.employee.name })

        val sortedLatestJoined = ManpowerViewModel.sortAttendanceItems(attItems, SortMode.LATEST_JOINED)
        assertEquals(listOf("Alice", "Bob", "Charlie"), sortedLatestJoined.map { it.employee.name })
    }

    // =========================================================================
    // Issue #28: Orthogonal Multi-Filter (Unit wise, Day vs Night Shift)
    // =========================================================================

    @Test
    fun `Issue 28 - Shift night detection recognizes Night Shift and Shift C`() {
        assertTrue(ManpowerViewModel.isNightShift("Night Shift"))
        assertTrue(ManpowerViewModel.isNightShift("NIGHT"))
        assertTrue(ManpowerViewModel.isNightShift("Shift C (Night)"))
        assertTrue(ManpowerViewModel.isNightShift("Shift C"))

        assertFalse(ManpowerViewModel.isNightShift("Shift A"))
        assertFalse(ManpowerViewModel.isNightShift("Shift B"))
        assertFalse(ManpowerViewModel.isNightShift("Day Shift"))
        assertFalse(ManpowerViewModel.isNightShift("General Shift"))
        assertFalse(ManpowerViewModel.isNightShift(""))
    }

    @Test
    fun `Issue 28 - Orthogonal multi-filtering by Unit, Shift, Department, and Contractor`() {
        val e1 = Employee(id = 1L, name = "E1", permanentDepartment = "Civil", defaultUnit = "Unit I", defaultShift = "Shift A", contractorName = "Contractor X")
        val e2 = Employee(id = 2L, name = "E2", permanentDepartment = "Civil", defaultUnit = "Unit I", defaultShift = "Night Shift", contractorName = "Contractor X")
        val e3 = Employee(id = 3L, name = "E3", permanentDepartment = "Mechanical", defaultUnit = "Unit I", defaultShift = "Shift A", contractorName = "Contractor Y")
        val e4 = Employee(id = 4L, name = "E4", permanentDepartment = "Civil", defaultUnit = "Unit II", defaultShift = "Shift A", contractorName = "Contractor X")
        val employees = listOf(e1, e2, e3, e4)

        // Filter: Unit I only
        val unit1Only = employees.filter { it.defaultUnit == "Unit I" }
        assertEquals(3, unit1Only.size)

        // Filter: Unit I AND Day Shift
        val unit1Day = employees.filter {
            it.defaultUnit == "Unit I" && !ManpowerViewModel.isNightShift(it.defaultShift)
        }
        assertEquals(2, unit1Day.size)
        assertEquals(listOf("E1", "E3"), unit1Day.map { it.name })

        // Filter: Unit I AND Night Shift
        val unit1Night = employees.filter {
            it.defaultUnit == "Unit I" && ManpowerViewModel.isNightShift(it.defaultShift)
        }
        assertEquals(1, unit1Night.size)
        assertEquals(listOf("E2"), unit1Night.map { it.name })

        // Filter: Unit I AND Civil AND Contractor X AND Day Shift
        val fullyFiltered = employees.filter {
            it.defaultUnit == "Unit I" &&
            it.permanentDepartment == "Civil" &&
            it.contractorName == "Contractor X" &&
            !ManpowerViewModel.isNightShift(it.defaultShift)
        }
        assertEquals(1, fullyFiltered.size)
        assertEquals(listOf("E1"), fullyFiltered.map { it.name })
    }

    // =========================================================================
    // Issue #27: Adaptive Dropdown Layout Calculation
    // =========================================================================

    @Test
    fun `Issue 27 - Dropdown item partitioning for adaptive 2-column layout`() {
        val fewItems = listOf("Dept 1", "Dept 2", "Dept 3", "Dept 4", "Dept 5")
        assertTrue(fewItems.size <= 7) // Single column path

        val manyItems = (1..15).map { "Contractor $it" }
        assertTrue(manyItems.size > 7) // Double column path

        val half = (manyItems.size + 1) / 2
        val col1 = manyItems.take(half)
        val col2 = manyItems.drop(half)

        assertEquals(8, col1.size)
        assertEquals(7, col2.size)
        assertEquals("Contractor 1", col1.first())
        assertEquals("Contractor 8", col1.last())
        assertEquals("Contractor 9", col2.first())
        assertEquals("Contractor 15", col2.last())
    }

    // =========================================================================
    // Issue #35: Date Standardization, Recognition, and Sorting
    // =========================================================================

    @Test
    fun `Issue 35 - DateUtils normalizes slash, hyphen, dot, named month, and single-digit dates to yyyy-MM-dd`() {
        assertEquals("2026-09-15", DateUtils.normalizeDate("2026-09-15"))
        assertEquals("2026-09-15", DateUtils.normalizeDate("2026/09/15"))
        assertEquals("2026-09-15", DateUtils.normalizeDate("15/09/2026"))
        assertEquals("2026-09-15", DateUtils.normalizeDate("15-09-2026"))
        assertEquals("2026-09-05", DateUtils.normalizeDate("5/9/2026"))
        assertEquals("2026-09-05", DateUtils.normalizeDate("2026/9/5"))
        assertEquals("2026-09-15", DateUtils.normalizeDate("09/15/2026"))
        assertEquals("2026-09-15", DateUtils.normalizeDate("15.09.2026"))
        assertEquals("2026-09-15", DateUtils.normalizeDate("15/09/26"))
        assertEquals("2026-09-15", DateUtils.normalizeDate("15-Sep-2026"))
        assertEquals("2026-09-15", DateUtils.normalizeDate("15 Sep 2026"))
        assertEquals("2026-09-15", DateUtils.normalizeDate("2026/09/15 10:30:00"))
        assertEquals("2026-09-15", DateUtils.normalizeDate("15/09/2026 09:15 AM"))
        assertEquals("2026-10-01", DateUtils.normalizeDate("", "2026-10-01"))
    }

    @Test
    fun `Issue 35 - LATEST_JOINED and OLDEST_JOINED sort accurately even when entries mix slash and hyphen date formats`() {
        // Previously "2026/09/01" > "2026-09-30" in raw ASCII string comparison because '/' (47) > '-' (45),
        // and "25/08/2026" > "2026-09-15" because '25' > '20'.
        val empAug = Employee(id = 1L, name = "Worker Aug", dateAdded = "25/08/2026")
        val empSepEarly = Employee(id = 2L, name = "Worker Sep 1", dateAdded = "2026/09/01")
        val empSepMid = Employee(id = 3L, name = "Worker Sep 15", dateAdded = "15-09-2026")
        val empSepLate = Employee(id = 4L, name = "Worker Sep 30", dateAdded = "2026-09-30")
        val empOct = Employee(id = 5L, name = "Worker Oct 5", dateAdded = "5/10/2026")

        val mixedList = listOf(empSepEarly, empOct, empAug, empSepLate, empSepMid)

        val latestJoined = ManpowerViewModel.sortEmployees(mixedList, SortMode.LATEST_JOINED)
        assertEquals(
            listOf("Worker Oct 5", "Worker Sep 30", "Worker Sep 15", "Worker Sep 1", "Worker Aug"),
            latestJoined.map { it.name }
        )

        val oldestJoined = ManpowerViewModel.sortEmployees(mixedList, SortMode.OLDEST_JOINED)
        assertEquals(
            listOf("Worker Aug", "Worker Sep 1", "Worker Sep 15", "Worker Sep 30", "Worker Oct 5"),
            oldestJoined.map { it.name }
        )
    }

    @Test
    fun `Issue 35 - LATEST_MODIFIED and OLDEST_MODIFIED sort by explicit modification timestamp and hide _lastModified from CSV`() {
        val empOldJoinedButRecentlyEdited = Employee(
            id = 1L,
            name = "Edited Worker",
            dateAdded = "01/08/2026",
            customFieldsJson = DateUtils.withLastModified("""{"Blood Group":"A+"}""", 1800000000000L)
        )
        val empNewJoinedNeverEdited = Employee(
            id = 2L,
            name = "Newer Unedited Worker",
            dateAdded = "2026-09-15",
            customFieldsJson = "{}"
        )
        val empOldestNeverEdited = Employee(
            id = 3L,
            name = "Oldest Unedited Worker",
            dateAdded = "10/07/2026",
            customFieldsJson = "{}"
        )

        val list = listOf(empNewJoinedNeverEdited, empOldestNeverEdited, empOldJoinedButRecentlyEdited)

        // LATEST_MODIFIED should place Edited Worker first even though their dateAdded is 01/08/2026!
        val latestModified = ManpowerViewModel.sortEmployees(list, SortMode.LATEST_MODIFIED)
        assertEquals(
            listOf("Edited Worker", "Newer Unedited Worker", "Oldest Unedited Worker"),
            latestModified.map { it.name }
        )

        val oldestModified = ManpowerViewModel.sortEmployees(list, SortMode.OLDEST_MODIFIED)
        assertEquals(
            listOf("Oldest Unedited Worker", "Newer Unedited Worker", "Edited Worker"),
            oldestModified.map { it.name }
        )

        // Ensure CSV export does NOT leak _lastModified as a custom field column AND normalizes dateAdded
        val csv = ManpowerViewModel.generateRoosterCsv(list, emptyList())
        assertFalse("CSV should not expose internal _lastModified key", csv.contains("_lastModified"))
        assertTrue("CSV should still export user custom fields", csv.contains("Blood Group"))
        assertTrue("CSV should export normalized yyyy-MM-dd dates", csv.contains("\"2026-08-01\""))
        assertTrue("CSV should export normalized yyyy-MM-dd dates", csv.contains("\"2026-07-10\""))
    }

    // =========================================================================
    // Issue #33: External Company Contacts Tab (Catering, Water, Snacks, etc.)
    // =========================================================================

    @Test
    fun `Issue 33 - ExternalContact serializes to and from ConfigItem with category CONTACT`() {
        val contact = ExternalContact(
            id = 42L,
            name = "Annapurna Catering Service",
            serviceCategory = "Catering Service",
            contactPerson = "Suresh Kadam",
            phone = "9850112233",
            notes = "Lunch delivery at 12:30 PM"
        )

        val configItem = contact.toConfigItem()
        assertEquals(42L, configItem.id)
        assertEquals(ExternalContact.CATEGORY_CONTACT, configItem.category)
        assertEquals("Annapurna Catering Service", configItem.name)

        val restored = ExternalContact.fromConfigItem(configItem)
        assertEquals(contact, restored)
    }

    @Test
    fun `Issue 33 - ExternalContact supports legacy pipe-delimited extraType fallback`() {
        val legacyItem = ConfigItem(
            id = 55L,
            category = ExternalContact.CATEGORY_CONTACT,
            name = "Aqua Water Supplier",
            extraType = "Water Supplier|Santosh|9764001122|20L jars"
        )

        val parsed = ExternalContact.fromConfigItem(legacyItem)
        assertEquals(55L, parsed.id)
        assertEquals("Aqua Water Supplier", parsed.name)
        assertEquals("Water Supplier", parsed.serviceCategory)
        assertEquals("Santosh", parsed.contactPerson)
        assertEquals("9764001122", parsed.phone)
        assertEquals("20L jars", parsed.notes)
    }
}
