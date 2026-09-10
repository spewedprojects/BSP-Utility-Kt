package com.gratus.bsputility.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gratus.bsputility.data.db.AppDatabase
import com.gratus.bsputility.data.models.ConfigItem
import com.gratus.bsputility.data.models.Contractor
import com.gratus.bsputility.data.models.DailyAttendance
import com.gratus.bsputility.data.models.DepartmentVerification
import com.gratus.bsputility.data.models.Employee
import com.gratus.bsputility.data.models.EmployeeStatuses
import com.gratus.bsputility.data.models.EmployeeTypes
import com.gratus.bsputility.data.models.ManpowerSummary
import com.gratus.bsputility.data.repository.ManpowerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ManpowerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ManpowerRepository
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private val _selectedDate = MutableStateFlow(dateFormat.format(Date()))
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    // Search and filters for Attendance Screen
    val attendanceSearchQuery = MutableStateFlow("")
    val attendanceFilterType = MutableStateFlow("All") // All, Labour, Staff, Present, Absent
    val attendanceFilterContractor = MutableStateFlow<String?>(null)
    val attendanceFilterDepartment = MutableStateFlow<String?>(null)

    // Search and filters for Rooster (Employee Library)
    val roosterSearchQuery = MutableStateFlow("")
    val roosterFilterStatus = MutableStateFlow("All") // All, Staff, Labour, Housekeeping, Debarred, Out
    val roosterFilterContractor = MutableStateFlow<String?>(null)
    val roosterFilterDepartment = MutableStateFlow<String?>(null)
    val collapsedGroups = MutableStateFlow<Map<String, Boolean>>(emptyMap())

    init {
        val db = AppDatabase.getDatabase(application)
        repository = ManpowerRepository(db.employeeDao())
    }

    val allEmployees: StateFlow<List<Employee>> = repository.allEmployees
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allContractors: StateFlow<List<Contractor>> = repository.allContractors
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allConfigItems: StateFlow<List<ConfigItem>> = repository.allConfigItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Stream of daily attendance for the currently selected date
    private val _attendanceStream = MutableStateFlow<List<DailyAttendance>>(emptyList())
    val attendanceRecords: StateFlow<List<DailyAttendance>> = _attendanceStream.asStateFlow()

    // Stream of department verifications for currently selected date
    private val _verificationStream = MutableStateFlow<List<DepartmentVerification>>(emptyList())
    val verificationRecords: StateFlow<List<DepartmentVerification>> = _verificationStream.asStateFlow()

    init {
        // Collect attendance whenever selectedDate changes using collectLatest to cancel previous date
        viewModelScope.launch {
            selectedDate.collectLatest { date ->
                _attendanceStream.value = emptyList()
                repository.getAttendanceForDate(date).collect { list ->
                    _attendanceStream.value = list
                }
            }
        }
        viewModelScope.launch {
            selectedDate.collectLatest { date ->
                _verificationStream.value = emptyList()
                repository.getVerificationsForDate(date).collect { list ->
                    _verificationStream.value = list
                }
            }
        }
    }

    // --- DATE NAVIGATION & FUTURE GUARDS ---
    fun isDateInFuture(dateString: String): Boolean {
        val todayStr = dateFormat.format(Date())
        return dateString > todayStr
    }

    fun selectPreviousDay() {
        try {
            val cal = Calendar.getInstance()
            cal.time = dateFormat.parse(_selectedDate.value) ?: Date()
            cal.add(Calendar.DAY_OF_YEAR, -1)
            _selectedDate.value = dateFormat.format(cal.time)
        } catch (_: Exception) {}
    }

    fun selectNextDay() {
        try {
            val todayStr = dateFormat.format(Date())
            if (_selectedDate.value >= todayStr) return // Do not allow navigating into future

            val cal = Calendar.getInstance()
            cal.time = dateFormat.parse(_selectedDate.value) ?: Date()
            cal.add(Calendar.DAY_OF_YEAR, 1)
            val nextDate = dateFormat.format(cal.time)
            if (nextDate <= todayStr) {
                _selectedDate.value = nextDate
            }
        } catch (_: Exception) {}
    }

    fun selectDate(dateString: String) {
        val todayStr = dateFormat.format(Date())
        _selectedDate.value = if (dateString > todayStr) todayStr else dateString
    }

    fun setDateFromCalendar(year: Int, month: Int, dayOfMonth: Int) {
        val cal = Calendar.getInstance()
        cal.set(year, month, dayOfMonth)
        val formatted = dateFormat.format(cal.time)
        val todayStr = dateFormat.format(Date())
        _selectedDate.value = if (formatted > todayStr) todayStr else formatted
    }

    // --- EFFECTIVE ATTENDANCE COMBINATION ---
    // Merges Employee library with day's Attendance records
    data class EmployeeAttendanceItem(
        val employee: Employee,
        val isPresent: Boolean,
        val effectiveDepartment: String,
        val effectiveWorkRole: String,
        val effectiveContractor: String,
        val effectiveUnit: String,
        val effectiveShift: String,
        val attendanceTime: String,
        val dayRemarks: String,
        val attendanceId: Long?
    )

    val effectiveAttendanceItems: StateFlow<List<EmployeeAttendanceItem>> = combine(
        allEmployees,
        _attendanceStream,
        attendanceSearchQuery,
        attendanceFilterType,
        attendanceFilterContractor,
        attendanceFilterDepartment
    ) { args: Array<Any?> ->
        @Suppress("UNCHECKED_CAST")
        val employees = args[0] as List<Employee>
        @Suppress("UNCHECKED_CAST")
        val attendances = args[1] as List<DailyAttendance>
        val query = args[2] as String
        val filterType = args[3] as String
        val filterContractor = args[4] as? String
        val filterDept = args[5] as? String

        val attendanceMap = attendances.associateBy { it.employeeId }

        // Filter out "Out" status from active daily attendance (Out means left the company)
        // Debarred employees REMAIN visible on subsequent days until status is changed
        val activeEmployees = employees.filter { it.status != EmployeeStatuses.OUT }

        val items = activeEmployees.map { emp ->
            val att = attendanceMap[emp.id]
            EmployeeAttendanceItem(
                employee = emp,
                isPresent = att?.isPresent ?: false,
                effectiveDepartment = att?.dayDepartment?.ifBlank { null }
                    ?: emp.permanentDepartment.ifBlank { "Unassigned" },
                effectiveWorkRole = att?.dayWorkRole ?: emp.defaultWorkRole,
                effectiveContractor = att?.dayContractorName ?: emp.contractorName,
                effectiveUnit = att?.dayUnit ?: emp.defaultUnit,
                effectiveShift = att?.dayShift ?: emp.defaultShift,
                attendanceTime = att?.attendanceTime ?: "",
                dayRemarks = att?.dayRemarks ?: "",
                attendanceId = att?.id
            )
        }

        // Apply filters & search
        items.filter { item ->
            val matchesQuery = query.isBlank() ||
                item.employee.name.contains(query, ignoreCase = true) ||
                item.effectiveDepartment.contains(query, ignoreCase = true) ||
                item.effectiveWorkRole.contains(query, ignoreCase = true) ||
                item.effectiveContractor.contains(query, ignoreCase = true)

            val matchesType = when (filterType) {
                "Staff" -> item.employee.type == EmployeeTypes.STAFF
                "Labour" -> item.employee.type == EmployeeTypes.LABOUR || item.employee.type == EmployeeTypes.HOUSEKEEPING
                "Present" -> item.isPresent
                "Absent" -> !item.isPresent
                else -> true
            }

            val matchesContractor = filterContractor == null || item.effectiveContractor.equals(filterContractor, ignoreCase = true)
            val matchesDept = filterDept == null || item.effectiveDepartment.equals(filterDept, ignoreCase = true)

            matchesQuery && matchesType && matchesContractor && matchesDept
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- MANPOWER SUMMARY COMPUTATION ---
    val manpowerSummary: StateFlow<ManpowerSummary> = combine(
        allEmployees,
        _attendanceStream,
        _verificationStream,
        allConfigItems
    ) { employees, attendances, verifications, configs ->
        val presentRecords = attendances.filter { it.isPresent }
        val empMap = employees.associateBy { it.id }

        var staffCount = 0
        var labourCount = 0
        var housekeepingCount = 0

        val contractorMap = mutableMapOf<String, Int>()
        val deptMap = mutableMapOf<String, Int>()
        val unitMap = mutableMapOf<String, Int>()
        val roleMap = mutableMapOf<String, Int>()
        val shiftMap = mutableMapOf<String, Int>()

        presentRecords.forEach { att ->
            val emp = empMap[att.employeeId]
            val type = emp?.type ?: att.employeeType

            when (type) {
                EmployeeTypes.STAFF -> staffCount++
                EmployeeTypes.HOUSEKEEPING -> {
                    housekeepingCount++
                    labourCount++ // Counted as labour for manpower
                }
                else -> labourCount++
            }

            // Contractor breakdown (primarily labourers)
            val contractor = att.dayContractorName.ifBlank { "Direct / In-house" }
            if (type != EmployeeTypes.STAFF) {
                contractorMap[contractor] = (contractorMap[contractor] ?: 0) + 1
            }

            // Department breakdown
            val dept = att.dayDepartment.ifBlank { "Unassigned" }
            deptMap[dept] = (deptMap[dept] ?: 0) + 1

            // Unit breakdown
            val unit = att.dayUnit.ifBlank { "Unit I" }
            unitMap[unit] = (unitMap[unit] ?: 0) + 1

            // Role breakdown
            val role = att.dayWorkRole.ifBlank { "General" }
            roleMap[role] = (roleMap[role] ?: 0) + 1

            // Shift breakdown
            val shift = att.dayShift.ifBlank { "Shift A" }
            shiftMap[shift] = (shiftMap[shift] ?: 0) + 1
        }

        val allDepts = configs.filter { it.category == "DEPARTMENT" }.map { it.name }.toSet()
        val verifiedDepts = verifications.filter { it.isVerified }.map { it.departmentName }.toSet()

        ManpowerSummary(
            totalStaffPresent = staffCount,
            totalLabourersPresent = labourCount,
            totalHousekeepingPresent = housekeepingCount,
            grandTotalPresent = staffCount + labourCount,
            contractorCounts = contractorMap,
            departmentCounts = deptMap,
            unitCounts = unitMap,
            roleCounts = roleMap,
            shiftCounts = shiftMap,
            verifiedDepartmentsCount = verifiedDepts.size,
            totalDepartmentsCount = allDepts.size.coerceAtLeast(1)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ManpowerSummary())

    // --- ATTENDANCE ACTIONS ---
    fun toggleAttendance(item: EmployeeAttendanceItem) {
        if (isDateInFuture(_selectedDate.value)) return
        viewModelScope.launch {
            val newPresence = !item.isPresent
            val defaultTime = if (newPresence && item.employee.type == EmployeeTypes.STAFF && item.attendanceTime.isBlank()) {
                SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
            } else if (!newPresence) {
                ""
            } else item.attendanceTime

            val record = DailyAttendance(
                id = item.attendanceId ?: 0,
                date = _selectedDate.value,
                employeeId = item.employee.id,
                employeeName = item.employee.name,
                employeeType = item.employee.type,
                isPresent = newPresence,
                dayDepartment = item.effectiveDepartment,
                dayWorkRole = item.effectiveWorkRole,
                dayContractorName = item.effectiveContractor,
                dayUnit = item.effectiveUnit,
                dayShift = item.effectiveShift,
                attendanceTime = defaultTime,
                dayRemarks = item.dayRemarks
            )
            repository.markAttendance(record)
        }
    }

    fun updateLabourDailyAssignment(
        employee: Employee,
        isPresent: Boolean,
        department: String,
        workRole: String,
        contractor: String,
        unit: String,
        shift: String,
        remarks: String
    ) {
        if (isDateInFuture(_selectedDate.value)) return
        viewModelScope.launch {
            val existing = _attendanceStream.value.find { it.employeeId == employee.id && it.date == _selectedDate.value }
            val record = DailyAttendance(
                id = existing?.id ?: 0,
                date = _selectedDate.value,
                employeeId = employee.id,
                employeeName = employee.name,
                employeeType = employee.type,
                isPresent = isPresent,
                dayDepartment = department,
                dayWorkRole = workRole,
                dayContractorName = contractor,
                dayUnit = unit,
                dayShift = shift,
                attendanceTime = existing?.attendanceTime ?: "",
                dayRemarks = remarks
            )
            repository.markAttendance(record)
        }
    }

    fun updateStaffDailyAttendance(
        employee: Employee,
        isPresent: Boolean,
        attendanceTime: String,
        remarks: String
    ) {
        if (isDateInFuture(_selectedDate.value)) return
        viewModelScope.launch {
            val existing = _attendanceStream.value.find { it.employeeId == employee.id && it.date == _selectedDate.value }
            val record = DailyAttendance(
                id = existing?.id ?: 0,
                date = _selectedDate.value,
                employeeId = employee.id,
                employeeName = employee.name,
                employeeType = employee.type,
                isPresent = isPresent,
                dayDepartment = employee.permanentDepartment,
                dayWorkRole = employee.designation,
                dayContractorName = "",
                dayUnit = employee.defaultUnit,
                dayShift = employee.defaultShift,
                attendanceTime = if (isPresent) attendanceTime else "",
                dayRemarks = remarks
            )
            repository.markAttendance(record)
        }
    }

    fun markAllActivePresent() {
        if (isDateInFuture(_selectedDate.value)) return
        viewModelScope.launch {
            val activeEmps = allEmployees.value.filter { it.status == EmployeeStatuses.ACTIVE }
            val currentMap = _attendanceStream.value.associateBy { it.employeeId }
            val timeNow = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())

            val list = activeEmps.map { emp ->
                val att = currentMap[emp.id]
                DailyAttendance(
                    id = att?.id ?: 0,
                    date = _selectedDate.value,
                    employeeId = emp.id,
                    employeeName = emp.name,
                    employeeType = emp.type,
                    isPresent = true,
                    dayDepartment = att?.dayDepartment ?: emp.permanentDepartment,
                    dayWorkRole = att?.dayWorkRole ?: emp.defaultWorkRole,
                    dayContractorName = att?.dayContractorName ?: emp.contractorName,
                    dayUnit = att?.dayUnit ?: emp.defaultUnit,
                    dayShift = att?.dayShift ?: emp.defaultShift,
                    attendanceTime = if (emp.type == EmployeeTypes.STAFF) (att?.attendanceTime?.ifBlank { timeNow }
                        ?: timeNow) else "",
                    dayRemarks = att?.dayRemarks ?: ""
                )
            }
            repository.saveAttendanceBatch(list)
        }
    }

    // --- DEPARTMENT VERIFICATION ---
    fun verifyDepartment(
        deptName: String,
        staffId: Long?,
        staffName: String,
        isVerified: Boolean,
        remarks: String = ""
    ) {
        if (isDateInFuture(_selectedDate.value)) return
        viewModelScope.launch {
            repository.verifyDepartment(
                date = _selectedDate.value,
                departmentName = deptName,
                staffId = staffId,
                staffName = staffName,
                isVerified = isVerified,
                remarks = remarks
            )
        }
    }

    // --- ROOSTER (EMPLOYEE LIBRARY) ACTIONS ---
    fun addEmployee(employee: Employee) {
        viewModelScope.launch {
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val toInsert = employee.copy(dateAdded = todayStr)
            repository.insertEmployee(toInsert)
        }
    }

    fun updateEmployee(employee: Employee) {
        viewModelScope.launch {
            repository.updateEmployee(employee)
        }
    }

    fun deleteEmployee(employee: Employee) {
        viewModelScope.launch {
            repository.deleteEmployee(employee)
        }
    }

    fun toggleCategoryCollapse(categoryKey: String) {
        val current = _categoryCollapseState()
        val nextState = !(current[categoryKey] ?: false)
        val updated = current.toMutableMap()
        updated[categoryKey] = nextState
        collapsedGroups.value = updated
    }

    private fun _categoryCollapseState(): Map<String, Boolean> {
        return collapsedGroups.value
    }

    // Import names copy-paste (newline separated)
    fun importPastedNames(
        namesText: String,
        targetType: String,
        contractorId: Long?,
        contractorName: String,
        defaultDept: String,
        defaultRole: String
    ): Int {
        val lines = namesText.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }

        if (lines.isEmpty()) return 0

        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val newEmployees = lines.map { name ->
            Employee(
                name = name,
                type = targetType,
                status = EmployeeStatuses.ACTIVE,
                dateAdded = todayStr,
                permanentDepartment = if (targetType == EmployeeTypes.STAFF) defaultDept else "",
                designation = if (targetType == EmployeeTypes.STAFF) defaultRole else "",
                contractorId = contractorId,
                contractorName = contractorName,
                defaultWorkRole = defaultRole,
                defaultUnit = "Unit I",
                defaultShift = "Shift A"
            )
        }

        viewModelScope.launch {
            repository.insertEmployees(newEmployees)
        }
        return newEmployees.size
    }

    // Import from CSV string
    fun importCsv(csvContent: String): Int {
        val lines = csvContent.lines().filter { it.isNotBlank() }
        if (lines.size <= 1) return 0

        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val employeesToAdd = mutableListOf<Employee>()

        // Expected header: Name,Type,Status,Department,Contractor,Role,Unit,Shift
        val startIndex = if (lines[0].contains("Name", ignoreCase = true)) 1 else 0

        for (i in startIndex until lines.size) {
            val cols = lines[i].split(",").map { it.trim().removeSurrounding("\"") }
            if (cols.isNotEmpty() && cols[0].isNotBlank()) {
                val name = cols[0]
                val type = cols.getOrNull(1)?.ifBlank { EmployeeTypes.LABOUR } ?: EmployeeTypes.LABOUR
                val status = cols.getOrNull(2)?.ifBlank { EmployeeStatuses.ACTIVE } ?: EmployeeStatuses.ACTIVE
                val dept = cols.getOrNull(3)?.ifBlank { "Welding Shop" } ?: "Welding Shop"
                val contractor = cols.getOrNull(4) ?: ""
                val role = cols.getOrNull(5)?.ifBlank { "Helper" } ?: "Helper"
                val unit = cols.getOrNull(6)?.ifBlank { "Unit I" } ?: "Unit I"
                val shift = cols.getOrNull(7)?.ifBlank { "Shift A" } ?: "Shift A"

                employeesToAdd.add(
                    Employee(
                        name = name,
                        type = type,
                        status = status,
                        dateAdded = todayStr,
                        permanentDepartment = if (type == EmployeeTypes.STAFF) dept else "",
                        designation = if (type == EmployeeTypes.STAFF) role else "",
                        contractorName = contractor,
                        defaultWorkRole = role,
                        defaultUnit = unit,
                        defaultShift = shift
                    )
                )
            }
        }

        if (employeesToAdd.isNotEmpty()) {
            viewModelScope.launch {
                repository.insertEmployees(employeesToAdd)
            }
        }
        return employeesToAdd.size
    }

    // Full JSON Export
    fun exportAllDataJson(): String {
        val root = JSONObject()
        val employeesArray = JSONArray()
        allEmployees.value.forEach { emp ->
            val obj = JSONObject().apply {
                put("id", emp.id)
                put("name", emp.name)
                put("type", emp.type)
                put("status", emp.status)
                put("dateAdded", emp.dateAdded)
                put("permanentDepartment", emp.permanentDepartment)
                put("designation", emp.designation)
                put("contractorName", emp.contractorName)
                put("defaultWorkRole", emp.defaultWorkRole)
                put("defaultUnit", emp.defaultUnit)
                put("defaultShift", emp.defaultShift)
                put("permanentRemarks", emp.permanentRemarks)
            }
            employeesArray.put(obj)
        }
        root.put("employees", employeesArray)

        val contractorsArray = JSONArray()
        allContractors.value.forEach { c ->
            val obj = JSONObject().apply {
                put("id", c.id)
                put("name", c.name)
                put("contactPerson", c.contactPerson)
                put("phone", c.phone)
                put("notes", c.notes)
            }
            contractorsArray.put(obj)
        }
        root.put("contractors", contractorsArray)

        val configArray = JSONArray()
        allConfigItems.value.forEach { cfg ->
            val obj = JSONObject().apply {
                put("category", cfg.category)
                put("name", cfg.name)
                put("extraType", cfg.extraType)
            }
            configArray.put(obj)
        }
        root.put("configItems", configArray)

        return root.toString(2)
    }

    // Full JSON Import
    fun importAllDataJson(jsonStr: String): Boolean {
        return try {
            val root = JSONObject(jsonStr)
            viewModelScope.launch {
                // Import contractors
                if (root.has("contractors")) {
                    val arr = root.getJSONArray("contractors")
                    val list = mutableListOf<Contractor>()
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        list.add(
                            Contractor(
                                name = obj.optString("name", "Contractor"),
                                contactPerson = obj.optString("contactPerson", ""),
                                phone = obj.optString("phone", ""),
                                notes = obj.optString("notes", "")
                            )
                        )
                    }
                    if (list.isNotEmpty()) repository.insertContractor(list[0]) // repository insert batch or iterate
                    list.forEach { repository.insertContractor(it) }
                }

                // Import config items
                if (root.has("configItems")) {
                    val arr = root.getJSONArray("configItems")
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        repository.insertConfigItem(
                            ConfigItem(
                                category = obj.optString("category", "DEPARTMENT"),
                                name = obj.optString("name", ""),
                                extraType = obj.optString("extraType", "")
                            )
                        )
                    }
                }

                // Import employees
                if (root.has("employees")) {
                    val arr = root.getJSONArray("employees")
                    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    val list = mutableListOf<Employee>()
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        list.add(
                            Employee(
                                name = obj.optString("name", "Unnamed"),
                                type = obj.optString("type", EmployeeTypes.LABOUR),
                                status = obj.optString("status", EmployeeStatuses.ACTIVE),
                                dateAdded = obj.optString("dateAdded", todayStr),
                                permanentDepartment = obj.optString(
                                    "permanentDepartment",
                                    "Welding Shop"
                                ),
                                designation = obj.optString("designation", ""),
                                contractorName = obj.optString("contractorName", ""),
                                defaultWorkRole = obj.optString("defaultWorkRole", "Helper"),
                                defaultUnit = obj.optString("defaultUnit", "Unit I"),
                                defaultShift = obj.optString("defaultShift", "Shift A"),
                                permanentRemarks = obj.optString("permanentRemarks", "")
                            )
                        )
                    }
                    if (list.isNotEmpty()) repository.insertEmployees(list)
                }
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    // --- MASTER DATA MANAGEMENT ---
    fun addContractor(contractor: Contractor) {
        viewModelScope.launch {
            repository.insertContractor(contractor)
        }
    }

    fun updateContractor(contractor: Contractor) {
        viewModelScope.launch {
            repository.updateContractor(contractor)
        }
    }

    fun deleteContractor(contractor: Contractor) {
        viewModelScope.launch {
            repository.deleteContractor(contractor)
        }
    }

    fun addConfigItem(category: String, name: String, extraType: String = "") {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.insertConfigItem(
                ConfigItem(
                    category = category,
                    name = name.trim(),
                    extraType = extraType
                )
            )
        }
    }

    fun deleteConfigItem(item: ConfigItem) {
        viewModelScope.launch {
            repository.deleteConfigItem(item)
        }
    }

    // --- REPORT STRING BUILDERS ---
    fun generateMorningReportWhatsApp(): String {
        val date = _selectedDate.value
        val summary = manpowerSummary.value
        val verifications = _verificationStream.value.filter { it.isVerified }

        val sb = StringBuilder()
        sb.append("📋 *BSP METATECH LLP, CHAKAN*\n")
        sb.append("🏭 *MORNING MANPOWER REPORT*\n")
        sb.append("📅 *Date:* $date\n")
        sb.append("⏰ *Report Time:* ${SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())} (Sub: < 11:00 AM)\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("👥 *TOTAL MANPOWER: ${summary.grandTotalPresent}*\n")
        sb.append("  • Staff Present: ${summary.totalStaffPresent}\n")
        sb.append("  • Contract Labour: ${summary.totalLabourersPresent}\n")
        if (summary.totalHousekeepingPresent > 0) {
            sb.append("  • (Housekeeping Included: ${summary.totalHousekeepingPresent})\n")
        }
        sb.append("\n🏢 *CONTRACTOR-WISE BREAKDOWN:*\n")
        if (summary.contractorCounts.isEmpty()) {
            sb.append("  (No contract labourers marked present)\n")
        } else {
            summary.contractorCounts.forEach { (contractor, count) ->
                sb.append("  • $contractor: *$count*\n")
            }
        }

        sb.append("\n⚙️ *DEPARTMENT-WISE LABOUR ALLOCATION:*\n")
        if (summary.departmentCounts.isEmpty()) {
            sb.append("  (No allocation recorded)\n")
        } else {
            summary.departmentCounts.forEach { (dept, count) ->
                sb.append("  • $dept: *$count*\n")
            }
        }

        sb.append("\n📍 *UNIT-WISE DISTRIBUTION:*\n")
        summary.unitCounts.forEach { (unit, count) ->
            sb.append("  • $unit: *$count*\n")
        }

        sb.append("\n🛠️ *ROLE / CATEGORY BREAKDOWN:*\n")
        summary.roleCounts.forEach { (role, count) ->
            sb.append("  • $role: *$count*\n")
        }

        sb.append("\n✅ *DEPARTMENT VERIFICATION STATUS:*\n")
        if (verifications.isEmpty()) {
            sb.append("  ⚠️ Verification pending with department heads.\n")
        } else {
            verifications.forEach { v ->
                sb.append("  • ${v.departmentName}: Verified by *${v.verifiedByStaffName}* (${v.verifiedAtTime})\n")
            }
        }

        sb.append("━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("Prepared by: HR Dept - BSP Metatech")
        return sb.toString()
    }

    fun generateDetailedCsv(): String {
        val sb = StringBuilder()
        sb.append("Date,Employee Name,Type,Status,Present,Department,Work/Role,Contractor,Unit,Shift,Time,Remarks\n")
        effectiveAttendanceItems.value.forEach { item ->
            sb.append("\"${_selectedDate.value}\",")
            sb.append("\"${item.employee.name}\",")
            sb.append("\"${item.employee.type}\",")
            sb.append("\"${item.employee.status}\",")
            sb.append(if (item.isPresent) "\"Present\"" else "\"Absent\"",)
            sb.append(",\"${item.effectiveDepartment}\",")
            sb.append("\"${item.effectiveWorkRole}\",")
            sb.append("\"${item.effectiveContractor}\",")
            sb.append("\"${item.effectiveUnit}\",")
            sb.append("\"${item.effectiveShift}\",")
            sb.append("\"${item.attendanceTime}\",")
            sb.append("\"${item.dayRemarks}\"\n")
        }
        return sb.toString()
    }

    fun generateSummaryCsv(): String {
        val summary = manpowerSummary.value
        val sb = StringBuilder()
        sb.append("BSP METATECH LLP - MANPOWER SUMMARY\n")
        sb.append("Date,${_selectedDate.value}\n")
        sb.append("Total Staff Present,${summary.totalStaffPresent}\n")
        sb.append("Total Labourers Present,${summary.totalLabourersPresent}\n")
        sb.append("Grand Total Manpower,${summary.grandTotalPresent}\n\n")

        sb.append("Contractor,Count\n")
        summary.contractorCounts.forEach { (c, cnt) ->
            sb.append("\"$c\",$cnt\n")
        }

        sb.append("\nDepartment,Count\n")
        summary.departmentCounts.forEach { (d, cnt) ->
            sb.append("\"$d\",$cnt\n")
        }

        sb.append("\nUnit,Count\n")
        summary.unitCounts.forEach { (u, cnt) ->
            sb.append("\"$u\",$cnt\n")
        }

        sb.append("\nRole,Count\n")
        summary.roleCounts.forEach { (r, cnt) ->
            sb.append("\"$r\",$cnt\n")
        }

        return sb.toString()
    }

    fun generateRoosterCsv(): String {
        val sb = StringBuilder()
        sb.append("Name,Type,Status,Department,Designation,Contractor,Default Role,Default Unit,Default Shift,Date Added,Remarks\n")
        allEmployees.value.forEach { emp ->
            sb.append("\"${emp.name}\",")
            sb.append("\"${emp.type}\",")
            sb.append("\"${emp.status}\",")
            sb.append("\"${emp.permanentDepartment}\",")
            sb.append("\"${emp.designation}\",")
            sb.append("\"${emp.contractorName}\",")
            sb.append("\"${emp.defaultWorkRole}\",")
            sb.append("\"${emp.defaultUnit}\",")
            sb.append("\"${emp.defaultShift}\",")
            sb.append("\"${emp.dateAdded}\",")
            sb.append("\"${emp.permanentRemarks}\"\n")
        }
        return sb.toString()
    }
}
