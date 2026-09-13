package com.gratus.bsputility.ui.viewmodel

import android.app.Application
import android.content.Context
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

    // SharedPreferences for persistent settings
    private val prefs = application.getSharedPreferences("bsp_manpower_prefs", Context.MODE_PRIVATE)
    private val _is24HourFormat = MutableStateFlow(prefs.getBoolean("pref_is_24_hour", false))
    val is24HourFormat: StateFlow<Boolean> = _is24HourFormat.asStateFlow()

    fun set24HourFormat(enabled: Boolean) {
        _is24HourFormat.value = enabled
        prefs.edit().putBoolean("pref_is_24_hour", enabled).apply()
    }

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

    // Mutual exclusivity mutators
    fun setAttendanceFilterType(type: String) {
        attendanceFilterType.value = type
        if (type == "Staff") {
            attendanceFilterContractor.value = null
        }
    }

    fun setAttendanceFilterContractor(contractor: String?) {
        attendanceFilterContractor.value = contractor
        if (contractor != null && attendanceFilterType.value == "Staff") {
            attendanceFilterType.value = "All"
        }
    }

    fun setRoosterFilterStatus(status: String) {
        roosterFilterStatus.value = status
        if (status == "Staff") {
            roosterFilterContractor.value = null
        }
    }

    fun setRoosterFilterContractor(contractor: String?) {
        roosterFilterContractor.value = contractor
        if (contractor != null && roosterFilterStatus.value == "Staff") {
            roosterFilterStatus.value = "All"
        }
    }

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

    private var preMarkAllSnapshot: List<DailyAttendance>? = null

    init {
        // Collect attendance whenever selectedDate changes using collectLatest to cancel previous date
        viewModelScope.launch {
            selectedDate.collectLatest { date ->
                preMarkAllSnapshot = null
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
        val attendanceTimestamp: Long = 0L,
        val dayRemarks: String,
        val attendanceId: Long?
    )

    // Unfiltered daily attendance items for the active roster on selectedDate (Issue #12)
    val dailyAttendanceItems: StateFlow<List<EmployeeAttendanceItem>> = combine(
        allEmployees,
        _attendanceStream
    ) { employees, attendances ->
        val attendanceMap = attendances.associateBy { it.employeeId }
        val activeEmployees = employees.filter { it.status != EmployeeStatuses.OUT }

        activeEmployees.map { emp ->
            val att = attendanceMap[emp.id]
            EmployeeAttendanceItem(
                employee = emp,
                isPresent = att?.isPresent ?: false,
                effectiveDepartment = att?.dayDepartment?.ifBlank { null }
                    ?: emp.permanentDepartment.ifBlank { "Unassigned" },
                effectiveWorkRole = att?.dayWorkRole ?: emp.defaultWorkRole,
                effectiveContractor = if (emp.type == EmployeeTypes.STAFF) "" else (att?.dayContractorName ?: emp.contractorName),
                effectiveUnit = att?.dayUnit ?: emp.defaultUnit,
                effectiveShift = att?.dayShift ?: emp.defaultShift,
                attendanceTime = att?.attendanceTime ?: "",
                attendanceTimestamp = att?.attendanceTimestamp ?: 0L,
                dayRemarks = att?.dayRemarks ?: "",
                attendanceId = att?.id
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered attendance items for AttendanceScreen UI
    val effectiveAttendanceItems: StateFlow<List<EmployeeAttendanceItem>> = combine(
        dailyAttendanceItems,
        attendanceSearchQuery,
        attendanceFilterType,
        attendanceFilterContractor,
        attendanceFilterDepartment
    ) { items, query, filterType, filterContractor, filterDept ->
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

            val matchesContractor = when (filterContractor) {
                null -> true
                "ALL_CONTRACTORS" -> item.effectiveContractor.isNotBlank() && item.employee.type != EmployeeTypes.STAFF
                else -> item.effectiveContractor.equals(filterContractor, ignoreCase = true) && item.employee.type != EmployeeTypes.STAFF
            }
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
    val areAllActivePresent: StateFlow<Boolean> = combine(
        allEmployees,
        _attendanceStream
    ) { employees, attendance ->
        val activeEmps = employees.filter { it.status == EmployeeStatuses.ACTIVE }
        if (activeEmps.isEmpty()) false
        else {
            val attMap = attendance.associateBy { it.employeeId }
            activeEmps.all { attMap[it.id]?.isPresent == true }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun toggleAttendance(item: EmployeeAttendanceItem) {
        if (isDateInFuture(_selectedDate.value)) return
        viewModelScope.launch {
            val newPresence = !item.isPresent
            val nowMillis = System.currentTimeMillis()
            val timeFormat = if (_is24HourFormat.value) SimpleDateFormat("HH:mm", Locale.getDefault()) else SimpleDateFormat("hh:mm a", Locale.getDefault())
            val defaultTime = if (newPresence && item.employee.type == EmployeeTypes.STAFF && item.attendanceTime.isBlank()) {
                timeFormat.format(Date(nowMillis))
            } else if (!newPresence) {
                ""
            } else item.attendanceTime

            val newTimestamp = if (newPresence && item.employee.type == EmployeeTypes.STAFF) {
                if (item.attendanceTimestamp != 0L) item.attendanceTimestamp else nowMillis
            } else if (!newPresence) {
                0L
            } else item.attendanceTimestamp

            val record = DailyAttendance(
                id = item.attendanceId ?: 0,
                date = _selectedDate.value,
                employeeId = item.employee.id,
                employeeName = item.employee.name,
                employeeType = item.employee.type,
                isPresent = newPresence,
                dayDepartment = item.effectiveDepartment,
                dayWorkRole = item.effectiveWorkRole,
                dayContractorName = if (item.employee.type == EmployeeTypes.STAFF) "" else item.effectiveContractor,
                dayUnit = item.effectiveUnit,
                dayShift = item.effectiveShift,
                attendanceTime = defaultTime,
                attendanceTimestamp = newTimestamp,
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
                attendanceTimestamp = existing?.attendanceTimestamp ?: 0L,
                dayRemarks = remarks
            )
            repository.markAttendance(record)
        }
    }

    fun updateStaffDailyAttendance(
        employee: Employee,
        isPresent: Boolean,
        attendanceTime: String,
        remarks: String,
        attendanceTimestamp: Long = 0L
    ) {
        if (isDateInFuture(_selectedDate.value)) return
        viewModelScope.launch {
            val existing = _attendanceStream.value.find { it.employeeId == employee.id && it.date == _selectedDate.value }
            val nowMillis = System.currentTimeMillis()
            val finalTimestamp = if (isPresent) {
                if (attendanceTimestamp != 0L) attendanceTimestamp
                else {
                    var parsedTs = 0L
                    try {
                        val format = if (attendanceTime.contains("AM", ignoreCase = true) || attendanceTime.contains("PM", ignoreCase = true)) {
                            SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault())
                        } else {
                            SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                        }
                        val d = format.parse("${_selectedDate.value} ${attendanceTime.trim()}")
                        if (d != null) parsedTs = d.time
                    } catch (_: Exception) {}
                    if (parsedTs != 0L) parsedTs else nowMillis
                }
            } else 0L

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
                attendanceTimestamp = finalTimestamp,
                dayRemarks = remarks
            )
            repository.markAttendance(record)
        }
    }

    fun markAllActivePresent() {
        if (isDateInFuture(_selectedDate.value)) return
        viewModelScope.launch {
            val activeEmps = allEmployees.value.filter { it.status == EmployeeStatuses.ACTIVE }
            if (activeEmps.isEmpty()) return@launch

            val currentMap = _attendanceStream.value.associateBy { it.employeeId }
            val isAlreadyAllPresent = activeEmps.all { currentMap[it.id]?.isPresent == true }

            if (isAlreadyAllPresent) {
                // Undo action: restore pre-Mark-All snapshot if available, or revert active back to absent
                if (preMarkAllSnapshot != null) {
                    repository.saveAttendanceBatch(preMarkAllSnapshot!!)
                    preMarkAllSnapshot = null
                } else {
                    val reverted = activeEmps.map { emp ->
                        val att = currentMap[emp.id]
                        DailyAttendance(
                            id = att?.id ?: 0,
                            date = _selectedDate.value,
                            employeeId = emp.id,
                            employeeName = emp.name,
                            employeeType = emp.type,
                            isPresent = false,
                            dayDepartment = att?.dayDepartment ?: emp.permanentDepartment,
                            dayWorkRole = att?.dayWorkRole ?: emp.defaultWorkRole,
                            dayContractorName = if (emp.type == EmployeeTypes.STAFF) "" else (att?.dayContractorName ?: emp.contractorName),
                            dayUnit = att?.dayUnit ?: emp.defaultUnit,
                            dayShift = att?.dayShift ?: emp.defaultShift,
                            attendanceTime = "",
                            attendanceTimestamp = 0L,
                            dayRemarks = att?.dayRemarks ?: ""
                        )
                    }
                    repository.saveAttendanceBatch(reverted)
                }
            } else {
                // Save snapshot before performing bulk mark-all
                preMarkAllSnapshot = _attendanceStream.value

                val nowMillis = System.currentTimeMillis()
                val timeFormat = if (_is24HourFormat.value) SimpleDateFormat("HH:mm", Locale.getDefault()) else SimpleDateFormat("hh:mm a", Locale.getDefault())
                val timeNow = timeFormat.format(Date(nowMillis))

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
                        dayContractorName = if (emp.type == EmployeeTypes.STAFF) "" else (att?.dayContractorName ?: emp.contractorName),
                        dayUnit = att?.dayUnit ?: emp.defaultUnit,
                        dayShift = att?.dayShift ?: emp.defaultShift,
                        attendanceTime = if (emp.type == EmployeeTypes.STAFF) (att?.attendanceTime?.ifBlank { timeNow } ?: timeNow) else "",
                        attendanceTimestamp = if (emp.type == EmployeeTypes.STAFF) (if ((att?.attendanceTimestamp ?: 0L) != 0L) att!!.attendanceTimestamp else nowMillis) else 0L,
                        dayRemarks = att?.dayRemarks ?: ""
                    )
                }
                repository.saveAttendanceBatch(list)
            }
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
                permanentDepartment = defaultDept,
                designation = if (targetType == EmployeeTypes.STAFF) defaultRole else "",
                contractorId = if (targetType == EmployeeTypes.STAFF) null else contractorId,
                contractorName = if (targetType == EmployeeTypes.STAFF) "" else contractorName,
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
                        permanentDepartment = dept,
                        designation = if (type == EmployeeTypes.STAFF) role else "",
                        contractorName = if (type == EmployeeTypes.STAFF) "" else contractor,
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

    fun syncLabourerDefaultsFromAttendance(onComplete: ((Int) -> Unit)? = null) {
        viewModelScope.launch {
            val count = repository.syncLabourerDefaultsFromAttendance()
            onComplete?.invoke(count)
        }
    }

    fun clearStaffContractorAssociations(onComplete: ((Int) -> Unit)? = null) {
        viewModelScope.launch {
            val count = repository.clearStaffContractorAssociations()
            onComplete?.invoke(count)
        }
    }

    fun migrateLegacyTimeRecords(onComplete: ((Int) -> Unit)? = null) {
        viewModelScope.launch {
            val count = repository.migrateLegacyTimeRecords()
            onComplete?.invoke(count)
        }
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
                        val empType = obj.optString("type", EmployeeTypes.LABOUR)
                        list.add(
                            Employee(
                                name = obj.optString("name", "Unnamed"),
                                type = empType,
                                status = obj.optString("status", EmployeeStatuses.ACTIVE),
                                dateAdded = obj.optString("dateAdded", todayStr),
                                permanentDepartment = obj.optString("permanentDepartment", "Welding Shop"),
                                designation = if (empType == EmployeeTypes.STAFF) obj.optString("designation", "") else "",
                                contractorName = if (empType == EmployeeTypes.STAFF) "" else obj.optString("contractorName", ""),
                                defaultWorkRole = if (empType != EmployeeTypes.STAFF) obj.optString("defaultWorkRole", "Helper") else "",
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

        val allEmps = allEmployees.value.filter { it.status != EmployeeStatuses.OUT }
        val activeStaff = allEmps.filter { it.type == EmployeeTypes.STAFF }
        val activeLabour = allEmps.filter { it.type != EmployeeTypes.STAFF }

        val attendances = _attendanceStream.value
        val attMap = attendances.associateBy { it.employeeId }

        val staffPresentCount = activeStaff.count { attMap[it.id]?.isPresent == true }
        val labourPresentCount = activeLabour.count { attMap[it.id]?.isPresent == true }
        val totalOnFloor = staffPresentCount + labourPresentCount

        val presentLabourers = activeLabour.filter { attMap[it.id]?.isPresent == true }

        // Contractor breakdown
        val contractorCounts = mutableMapOf<String, Int>()
        presentLabourers.forEach { emp ->
            val att = attMap[emp.id]
            val contractor = att?.dayContractorName?.ifBlank { null }
                ?: emp.contractorName.ifBlank { null }
                ?: "Direct / In-house"
            contractorCounts[contractor] = (contractorCounts[contractor] ?: 0) + 1
        }

        // Work assigned breakdown (labor present)
        val roleCounts = mutableMapOf<String, Int>()
        presentLabourers.forEach { emp ->
            val att = attMap[emp.id]
            val role = att?.dayWorkRole?.ifBlank { null }
                ?: emp.defaultWorkRole.ifBlank { null }
            if (!role.isNullOrBlank()) {
                roleCounts[role] = (roleCounts[role] ?: 0) + 1
            }
        }

        // Shift split (present)
        var dayShiftCount = 0
        var nightShiftCount = 0
        val allPresentWorkers = allEmps.filter { attMap[it.id]?.isPresent == true }
        allPresentWorkers.forEach { emp ->
            val att = attMap[emp.id]
            val shift = att?.dayShift?.ifBlank { null } ?: emp.defaultShift.ifBlank { "Shift A" }
            val s = shift.lowercase()
            if (s.contains("night") || s.contains("shift b") || s.contains("shift c") || s.contains("2nd") || s.contains("3rd")) {
                nightShiftCount++
            } else {
                dayShiftCount++
            }
        }

        // Absent staff
        val absentStaffNames = activeStaff
            .filter { attMap[it.id]?.isPresent != true }
            .map { it.name }
        val absentStaffStr = if (absentStaffNames.isEmpty()) "None" else absentStaffNames.joinToString(", ")

        val sb = StringBuilder()
        sb.append("BSP Metatech LLP — Chakan\n")
        sb.append("Attendance Report — $date\n\n")

        sb.append("Staff present: $staffPresentCount / ${activeStaff.size}\n")
        sb.append("Labor present: $labourPresentCount / ${activeLabour.size}\n")
        sb.append("Total on floor: $totalOnFloor\n\n")

        sb.append("--- By contractor ---\n")
        if (contractorCounts.isEmpty()) {
            sb.append("None present\n")
        } else {
            contractorCounts.forEach { (c, count) ->
                sb.append("$c: $count\n")
            }
        }
        sb.append("\n")

        sb.append("--- By work assigned (labor present) ---\n")
        if (roleCounts.isEmpty()) {
            sb.append("None present\n")
        } else {
            roleCounts.forEach { (r, count) ->
                sb.append("$r: $count\n")
            }
        }
        sb.append("\n")

        sb.append("--- Shift split (present) ---\n")
        sb.append("Day: $dayShiftCount   Night: $nightShiftCount\n\n")

        sb.append("--- Absent ---\n")
        sb.append("Staff: $absentStaffStr\n\n")

        sb.append("--------------\n")
        sb.append("Prepared by: HR Dept - BSP Metatech")
        return sb.toString()
    }

    fun generateDetailedCsv(): String {
        val sb = StringBuilder()
        sb.append("Date,Employee Name,Type,Status,Present,Department,Work/Role,Contractor,Unit,Shift,Time,Remarks\n")
        dailyAttendanceItems.value.forEach { item ->
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
