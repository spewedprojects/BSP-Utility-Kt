package com.gratus.bsputility.data.repository

import com.gratus.bsputility.data.db.EmployeeDao
import com.gratus.bsputility.data.models.ConfigItem
import com.gratus.bsputility.data.models.Contractor
import com.gratus.bsputility.data.models.DailyAttendance
import com.gratus.bsputility.data.models.DepartmentVerification
import com.gratus.bsputility.data.models.Employee
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

    suspend fun deleteConfigItem(item: ConfigItem) {
        dao.deleteConfigItem(item)
    }
}
