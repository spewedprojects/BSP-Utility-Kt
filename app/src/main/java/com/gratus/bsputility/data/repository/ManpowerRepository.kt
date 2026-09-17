package com.gratus.bsputility.data.repository

import com.gratus.bsputility.data.db.EmployeeDao
import com.gratus.bsputility.data.models.ConfigItem
import com.gratus.bsputility.data.models.Contractor
import com.gratus.bsputility.data.models.DailyAttendance
import com.gratus.bsputility.data.models.DepartmentVerification
import com.gratus.bsputility.data.models.Employee
import com.gratus.bsputility.data.models.EmployeeStatuses
import com.gratus.bsputility.data.models.EmployeeTypes
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ManpowerRepository(private val dao: EmployeeDao) {

    val allEmployees: Flow<List<Employee>> = dao.getAllEmployees()
    val allContractors: Flow<List<Contractor>> = dao.getAllContractors()
    val allConfigItems: Flow<List<ConfigItem>> = dao.getAllConfigItems()

    fun getAttendanceForDate(date: String): Flow<List<DailyAttendance>> {
        return dao.getAttendanceForDate(date)
    }

    fun getVerificationsForDate(date: String): Flow<List<DepartmentVerification>> {
        return dao.getVerificationsForDate(date)
    }

    fun getConfigItemsByCategory(category: String): Flow<List<ConfigItem>> {
        return dao.getConfigItemsByCategory(category)
    }

    suspend fun insertEmployee(employee: Employee): Long {
        return dao.insertEmployee(employee)
    }

    suspend fun insertEmployees(employees: List<Employee>): List<Long> {
        return dao.insertEmployees(employees)
    }

    suspend fun updateEmployee(employee: Employee) {
        dao.updateEmployee(employee)
    }

    suspend fun deleteEmployee(employee: Employee) {
        dao.deleteEmployee(employee)
    }

    suspend fun markAttendance(attendance: DailyAttendance): Long {
        return dao.insertOrUpdateAttendance(attendance)
    }

    suspend fun saveAttendanceBatch(records: List<DailyAttendance>) {
        dao.insertAttendanceBatch(records)
    }

    suspend fun verifyDepartment(
        date: String,
        departmentName: String,
        staffId: Long?,
        staffName: String,
        isVerified: Boolean,
        remarks: String = ""
    ) {
        val existing = dao.getVerification(date, departmentName)
        val timeNow = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        val updated = existing?.copy(
            isVerified = isVerified,
            verifiedByStaffId = staffId,
            verifiedByStaffName = staffName,
            verifiedAtTime = if (isVerified) timeNow else "",
            remarks = remarks
        ) ?: DepartmentVerification(
            date = date,
            departmentName = departmentName,
            isVerified = isVerified,
            verifiedByStaffId = staffId,
            verifiedByStaffName = staffName,
            verifiedAtTime = if (isVerified) timeNow else "",
            remarks = remarks
        )
        dao.insertOrUpdateVerification(updated)
    }

    suspend fun insertContractor(contractor: Contractor): Long {
        return dao.insertContractor(contractor)
    }

    suspend fun updateContractor(contractor: Contractor) {
        dao.updateContractor(contractor)
    }

    suspend fun deleteContractor(contractor: Contractor) {
        dao.deleteContractor(contractor)
    }

    suspend fun insertConfigItem(item: ConfigItem): Long {
        return dao.insertConfigItem(item)
    }

    suspend fun clearStaffContractorAssociations(): Int {
        val empCount = dao.clearStaffContractorAssociations()
        val attCount = dao.clearStaffAttendanceContractors()
        return empCount + attCount
    }

    suspend fun migrateLegacyTimeRecords(): Int {
        val unmigrated = dao.getUnmigratedTimeRecords()
        if (unmigrated.isEmpty()) return 0

        val formats = listOf(
            SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault()),
            SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()),
            SimpleDateFormat("yyyy-MM-dd h:mm a", Locale.getDefault())
        )

        val updated = unmigrated.mapNotNull { att ->
            var timestamp = 0L
            for (format in formats) {
                try {
                    val d = format.parse("${att.date} ${att.attendanceTime.trim()}")
                    if (d != null) {
                        timestamp = d.time
                        break
                    }
                } catch (_: Exception) {}
            }
            if (timestamp != 0L) {
                att.copy(attendanceTimestamp = timestamp)
            } else null
        }

        if (updated.isNotEmpty()) {
            dao.updateDailyAttendanceBatch(updated)
        }
        return updated.size
    }

    suspend fun updateConfigItem(item: ConfigItem) {
        dao.updateConfigItem(item)
    }

    suspend fun deleteConfigItem(item: ConfigItem) {
        dao.deleteConfigItem(item)
    }

    suspend fun relinkAttendanceRecordsByName(): Int {
        val allEmps = dao.getAllEmployeesList()
        if (allEmps.isEmpty()) return 0
        val empByName = allEmps.associateBy { it.name.trim().lowercase() }
        val allAttendance = dao.getAllAttendanceRecords()
        if (allAttendance.isEmpty()) return 0

        val toUpdate = mutableListOf<DailyAttendance>()
        for (att in allAttendance) {
            val matched = empByName[att.employeeName.trim().lowercase()]
            if (matched != null && (att.employeeId != matched.id || att.employeeType != matched.type)) {
                toUpdate.add(
                    att.copy(
                        employeeId = matched.id,
                        employeeType = matched.type
                    )
                )
            }
        }
        if (toUpdate.isNotEmpty()) {
            dao.updateDailyAttendanceBatch(toUpdate)
        }
        return toUpdate.size
    }

    suspend fun syncEmployeesRosterUpsert(importedEmployees: List<Employee>): Int {
        val existingEmployees = dao.getAllEmployeesList()
        val existingById = existingEmployees.associateBy { it.id }
        val existingByComposite = existingEmployees.associateBy { "${it.name.trim().lowercase()}|${it.contractorName.trim().lowercase()}" }
        val existingByName = existingEmployees.associateBy { it.name.trim().lowercase() }

        val toInsert = mutableListOf<Employee>()
        val toUpdate = mutableListOf<Employee>()
        val matchedExistingIds = mutableSetOf<Long>()

        for (imported in importedEmployees) {
            val matched = (if (imported.id > 0) existingById[imported.id] else null)
                ?: existingByComposite["${imported.name.trim().lowercase()}|${imported.contractorName.trim().lowercase()}"]
                ?: existingByName[imported.name.trim().lowercase()]

            if (matched != null) {
                matchedExistingIds.add(matched.id)
                toUpdate.add(
                    imported.copy(
                        id = matched.id,
                        dateAdded = if (imported.dateAdded.isNotBlank()) imported.dateAdded else matched.dateAdded,
                        status = if (imported.status.isNotBlank()) imported.status else matched.status,
                        contractorId = imported.contractorId ?: matched.contractorId
                    )
                )
            } else {
                toInsert.add(imported.copy(id = 0L))
            }
        }

        // Archive (soft-delete) existing workers that were omitted from incoming roster
        val toArchive = existingEmployees.filter { it.id !in matchedExistingIds && it.status != EmployeeStatuses.OUT }.map {
            it.copy(status = EmployeeStatuses.OUT)
        }

        if (toUpdate.isNotEmpty() || toArchive.isNotEmpty()) {
            dao.updateEmployees(toUpdate + toArchive)
        }
        if (toInsert.isNotEmpty()) {
            dao.insertEmployees(toInsert)
        }

        relinkAttendanceRecordsByName()
        return toUpdate.size + toInsert.size
    }

    suspend fun getAllEmployeesList(): List<Employee> {
        return dao.getAllEmployeesList()
    }

    suspend fun getAllContractorsList(): List<Contractor> {
        return dao.getAllContractorsList()
    }

    suspend fun getAllConfigItemsList(): List<ConfigItem> {
        return dao.getAllConfigItemsList()
    }

    suspend fun getAllAttendanceList(): List<DailyAttendance> {
        return dao.getAllAttendanceRecords()
    }

    suspend fun getAllVerificationsList(): List<DepartmentVerification> {
        return dao.getAllVerificationsList()
    }

    suspend fun replaceEmployees(employees: List<Employee>) {
        syncEmployeesRosterUpsert(employees)
    }

    suspend fun replaceAllMasterData(
        employees: List<Employee>?,
        contractors: List<Contractor>?,
        configItems: List<ConfigItem>?
    ) {
        if (contractors != null) {
            dao.deleteAllContractors()
            dao.insertContractors(contractors)
        }
        if (configItems != null) {
            dao.deleteAllConfigItems()
            dao.insertConfigItems(configItems)
        }
        if (employees != null) {
            syncEmployeesRosterUpsert(employees)
        }
    }

    suspend fun restoreFullDatabase(
        employees: List<Employee>?,
        contractors: List<Contractor>?,
        configItems: List<ConfigItem>?,
        attendances: List<DailyAttendance>?,
        verifications: List<DepartmentVerification>?
    ) {
        if (contractors != null) {
            dao.deleteAllContractors()
            dao.insertContractors(contractors)
        }
        if (configItems != null) {
            dao.deleteAllConfigItems()
            dao.insertConfigItems(configItems)
        }
        if (employees != null) {
            dao.deleteAllEmployees()
            dao.insertEmployees(employees)
        }
        if (attendances != null) {
            dao.deleteAllDailyAttendance()
            dao.insertAttendanceBatch(attendances)
        }
        if (verifications != null) {
            dao.deleteAllDepartmentVerifications()
            dao.insertVerificationsBatch(verifications)
        }
        relinkAttendanceRecordsByName()
    }

    suspend fun getAttendanceListForDate(date: String): List<DailyAttendance> {
        return dao.getAttendanceListForDate(date)
    }

    suspend fun markDepartmentSameAsYesterday(
        targetDate: String,
        yesterdayDate: String,
        departmentName: String
    ): Int {
        val yesterdayRecords = dao.getAttendanceListForDate(yesterdayDate)
        val matchingRecords = yesterdayRecords.filter {
            it.isPresent && (departmentName.isBlank() || it.dayDepartment.equals(departmentName, ignoreCase = true))
        }
        if (matchingRecords.isEmpty()) return 0

        val currentRecords = dao.getAttendanceListForDate(targetDate).associateBy { it.employeeId }
        val now = System.currentTimeMillis()
        val toSave = matchingRecords.map { y ->
            val existing = currentRecords[y.employeeId]
            if (existing != null) {
                existing.copy(
                    isPresent = true,
                    dayDepartment = y.dayDepartment,
                    dayWorkRole = y.dayWorkRole,
                    dayContractorName = y.dayContractorName,
                    dayUnit = y.dayUnit,
                    dayShift = y.dayShift,
                    updatedAt = now
                )
            } else {
                DailyAttendance(
                    date = targetDate,
                    employeeId = y.employeeId,
                    employeeName = y.employeeName,
                    employeeType = y.employeeType,
                    isPresent = true,
                    dayDepartment = y.dayDepartment,
                    dayWorkRole = y.dayWorkRole,
                    dayContractorName = y.dayContractorName,
                    dayUnit = y.dayUnit,
                    dayShift = y.dayShift,
                    attendanceTime = y.attendanceTime,
                    attendanceTimestamp = y.attendanceTimestamp,
                    updatedAt = now
                )
            }
        }

        dao.insertAttendanceBatch(toSave)
        return toSave.size
    }

    suspend fun syncLabourerDefaultsFromAttendance(): Int {
        val attendances = dao.getLabourAttendanceWithDepartments()
        if (attendances.isEmpty()) return 0

        // Latest attendance record by employeeId (already sorted date DESC, id DESC)
        val latestByEmployee = attendances.groupBy { it.employeeId }
            .mapValues { (_, list) -> list.first() }

        val allEmployees = dao.getAllEmployeesList()
        val toUpdate = mutableListOf<Employee>()

        for (emp in allEmployees) {
            if (emp.type != EmployeeTypes.STAFF) {
                val latest = latestByEmployee[emp.id]
                if (latest != null) {
                    var changed = false
                    var newDept = emp.permanentDepartment
                    var newRole = emp.defaultWorkRole
                    var newUnit = emp.defaultUnit
                    var newShift = emp.defaultShift
                    var newContractor = emp.contractorName

                    if (latest.dayDepartment.isNotBlank() && !latest.dayDepartment.equals("Unassigned", ignoreCase = true) && latest.dayDepartment != emp.permanentDepartment) {
                        newDept = latest.dayDepartment
                        changed = true
                    }
                    if (latest.dayWorkRole.isNotBlank() && latest.dayWorkRole != emp.defaultWorkRole) {
                        newRole = latest.dayWorkRole
                        changed = true
                    }
                    if (latest.dayUnit.isNotBlank() && latest.dayUnit != emp.defaultUnit) {
                        newUnit = latest.dayUnit
                        changed = true
                    }
                    if (latest.dayShift.isNotBlank() && latest.dayShift != emp.defaultShift) {
                        newShift = latest.dayShift
                        changed = true
                    }
                    if (latest.dayContractorName.isNotBlank() && latest.dayContractorName != emp.contractorName) {
                        newContractor = latest.dayContractorName
                        changed = true
                    }

                    if (changed) {
                        toUpdate.add(emp.copy(
                            permanentDepartment = newDept,
                            defaultWorkRole = newRole,
                            defaultUnit = newUnit,
                            defaultShift = newShift,
                            contractorName = newContractor
                        ))
                    }
                }
            }
        }

        if (toUpdate.isNotEmpty()) {
            dao.updateEmployees(toUpdate)
        }
        return toUpdate.size
    }
}
