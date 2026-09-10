package com.gratus.bsputility.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

object EmployeeTypes {
    const val STAFF = "Staff"
    const val LABOUR = "Contract Labour"
    const val HOUSEKEEPING = "Housekeeping"

    val all = listOf(STAFF, LABOUR, HOUSEKEEPING)
}

object EmployeeStatuses {
    const val ACTIVE = "Active"
    const val OUT = "Out" // Left the company: remains in library, omitted from next day active attendance
    const val DEBARRED = "Debarred" // Temporary ban: keeps appearing until changed

    val all = listOf(ACTIVE, OUT, DEBARRED)
}

@Entity(tableName = "employees")
data class Employee(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: String = EmployeeTypes.LABOUR, // Staff, Contract Labour, Housekeeping
    val status: String = EmployeeStatuses.ACTIVE, // Active, Out, Debarred
    val dateAdded: String, // Auto recorded date string (e.g. "2026-09-10")
    val permanentDepartment: String = "Welding Shop",
    val designation: String = "", // For staff (e.g. HR Trainee, Supervisor)
    val contractorId: Long? = null,
    val contractorName: String = "", // Associated contractor
    val defaultWorkRole: String = "Helper", // Welder, Operator, Helper, Bender, Painter, Laser Operator
    val defaultUnit: String = "Unit I", // Unit I, Unit II, Unit III
    val defaultShift: String = "Shift A", // Shift A, Shift B, General
    val permanentRemarks: String = "",
    val customFieldsJson: String = "{}"
)

@Entity(tableName = "daily_attendance")
data class DailyAttendance(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // YYYY-MM-DD
    val employeeId: Long,
    val employeeName: String,
    val employeeType: String,
    val isPresent: Boolean = false,
    // Dynamic daily overrides (applies only to this day's attendance without modifying master record)
    val dayDepartment: String = "",
    val dayWorkRole: String = "",
    val dayContractorName: String = "",
    val dayUnit: String = "",
    val dayShift: String = "",
    val attendanceTime: String = "", // For staff (e.g. "08:45 AM")
    val dayRemarks: String = "", // e.g. "Assigned to Laser today"
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "department_verifications")
data class DepartmentVerification(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // YYYY-MM-DD
    val departmentName: String,
    val isVerified: Boolean = false,
    val verifiedByStaffId: Long? = null,
    val verifiedByStaffName: String = "",
    val verifiedAtTime: String = "",
    val remarks: String = ""
)

@Entity(tableName = "contractors")
data class Contractor(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val contactPerson: String = "",
    val phone: String = "",
    val notes: String = ""
)

@Entity(tableName = "config_items")
data class ConfigItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String, // DEPARTMENT, DESIGNATION, UNIT, LABOUR_ROLE, CUSTOM_FIELD
    val name: String,
    val extraType: String = "" // For custom fields: TEXT, NUMBER, DROPDOWN, DATE
)

data class ManpowerSummary(
    val totalStaffPresent: Int = 0,
    val totalLabourersPresent: Int = 0,
    val totalHousekeepingPresent: Int = 0,
    val grandTotalPresent: Int = 0,
    val contractorCounts: Map<String, Int> = emptyMap(),
    val departmentCounts: Map<String, Int> = emptyMap(),
    val unitCounts: Map<String, Int> = emptyMap(),
    val roleCounts: Map<String, Int> = emptyMap(),
    val shiftCounts: Map<String, Int> = emptyMap(),
    val verifiedDepartmentsCount: Int = 0,
    val totalDepartmentsCount: Int = 0
)
