package com.gratus.bsputility.data.repository

import com.gratus.bsputility.data.db.EmployeeDao
import com.gratus.bsputility.data.models.ConfigItem
import com.gratus.bsputility.data.models.Contractor
import com.gratus.bsputility.data.models.DailyAttendance
import com.gratus.bsputility.data.models.DepartmentVerification
import com.gratus.bsputility.data.models.Employee
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

    suspend fun deleteConfigItem(item: ConfigItem) {
        dao.deleteConfigItem(item)
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

                    if (emp.permanentDepartment.isBlank() || emp.permanentDepartment.equals("Unassigned", ignoreCase = true)) {
                        if (latest.dayDepartment.isNotBlank() && !latest.dayDepartment.equals("Unassigned", ignoreCase = true)) {
                            newDept = latest.dayDepartment
                            changed = true
                        }
                    }
                    if (emp.defaultWorkRole.isBlank() || emp.defaultWorkRole.equals("Helper", ignoreCase = true)) {
                        if (latest.dayWorkRole.isNotBlank()) {
                            newRole = latest.dayWorkRole
                            changed = true
                        }
                    }
                    if (emp.defaultUnit.isBlank() && latest.dayUnit.isNotBlank()) {
                        newUnit = latest.dayUnit
                        changed = true
                    }
                    if (emp.defaultShift.isBlank() && latest.dayShift.isNotBlank()) {
                        newShift = latest.dayShift
                        changed = true
                    }

                    if (changed) {
                        toUpdate.add(emp.copy(
                            permanentDepartment = newDept,
                            defaultWorkRole = newRole,
                            defaultUnit = newUnit,
                            defaultShift = newShift
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
