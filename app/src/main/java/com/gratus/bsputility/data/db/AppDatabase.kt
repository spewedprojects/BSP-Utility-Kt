package com.gratus.bsputility.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.gratus.bsputility.data.models.ConfigItem
import com.gratus.bsputility.data.models.Contractor
import com.gratus.bsputility.data.models.DailyAttendance
import com.gratus.bsputility.data.models.DepartmentVerification
import com.gratus.bsputility.data.models.Employee
import com.gratus.bsputility.data.models.EmployeeStatuses
import com.gratus.bsputility.data.models.EmployeeTypes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Database(
    entities = [
        Employee::class,
        DailyAttendance::class,
        DepartmentVerification::class,
        Contractor::class,
        ConfigItem::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun employeeDao(): EmployeeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE daily_attendance ADD COLUMN attendanceTimestamp INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bsp_manpower_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database.employeeDao())
                    }
                }
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            // Clear any existing contractor associations for Staff (Issue #6 requirement)
                            database.employeeDao().clearStaffContractorAssociations()
                            database.employeeDao().clearStaffAttendanceContractors()

                            // Auto-migrate legacy time strings to timestamps (Issue #10)
                            val unmigrated = database.employeeDao().getUnmigratedTimeRecords()
                            if (unmigrated.isNotEmpty()) {
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
                                    if (timestamp != 0L) att.copy(attendanceTimestamp = timestamp) else null
                                }
                                if (updated.isNotEmpty()) {
                                    database.employeeDao().updateDailyAttendanceBatch(updated)
                                }
                            }

                            // Auto-sync labourer permanent department/defaults from recent daily attendance
                            val labourAttendances = database.employeeDao().getLabourAttendanceWithDepartments()
                            if (labourAttendances.isNotEmpty()) {
                                val latestByEmployee = labourAttendances.groupBy { it.employeeId }
                                    .mapValues { (_, list) -> list.first() }
                                val allEmployees = database.employeeDao().getAllEmployeesList()
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
                                            if (latest.dayUnit.isNotBlank() && latest.dayUnit != emp.defaultUnit) {
                                                newUnit = latest.dayUnit
                                                changed = true
                                            }
                                            if (latest.dayShift.isNotBlank() && latest.dayShift != emp.defaultShift) {
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
                                    database.employeeDao().updateEmployees(toUpdate)
                                }
                            }
                        } catch (_: Exception) {}
                    }
                }
            }

            private suspend fun populateInitialData(dao: EmployeeDao) {
                val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                // 1. Initial Config Items (Always seeded for both debug and release builds)
                val departments = listOf(
                    "Welding Shop",
                    "Laser Cutting",
                    "Press & Bending",
                    "Paint Shop",
                    "Assembly & Quality",
                    "Maintenance",
                    "Housekeeping",
                    "HR & Admin"
                ).map { ConfigItem(category = "DEPARTMENT", name = it) }

                val designations = listOf(
                    "HR Trainee",
                    "Production Supervisor",
                    "Department Head",
                    "Quality Engineer",
                    "Maintenance Lead",
                    "Plant Manager"
                ).map { ConfigItem(category = "DESIGNATION", name = it) }

                val units = listOf(
                    "Unit I",
                    "Unit II",
                    "Unit III"
                ).map { ConfigItem(category = "UNIT", name = it) }

                val roles = listOf(
                    ConfigItem(category = "LABOUR_ROLE", name = "Helper", extraType = "ALL"),
                    ConfigItem(category = "LABOUR_ROLE", name = "Welder", extraType = "Welding Shop, Production"),
                    ConfigItem(category = "LABOUR_ROLE", name = "Fitter", extraType = "Welding Shop, Assembly & Quality, Production"),
                    ConfigItem(category = "LABOUR_ROLE", name = "Painter", extraType = "Paint Shop, Painting"),
                    ConfigItem(category = "LABOUR_ROLE", name = "Blaster", extraType = "Paint Shop, Painting"),
                    ConfigItem(category = "LABOUR_ROLE", name = "Operator", extraType = "Press & Bending, Laser Cutting, Maintenance"),
                    ConfigItem(category = "LABOUR_ROLE", name = "Laser Operator", extraType = "Laser Cutting"),
                    ConfigItem(category = "LABOUR_ROLE", name = "Bender", extraType = "Press & Bending"),
                    ConfigItem(category = "LABOUR_ROLE", name = "Housekeeping", extraType = "ALL")
                )

                val shifts = listOf(
                    "Shift A",
                    "Shift B",
                    "Shift C",
                    "General"
                ).map { ConfigItem(category = "SHIFT", name = it) }

                val customFields = listOf(
                    ConfigItem(
                        category = "CUSTOM_FIELD",
                        name = "Aadhaar Verified",
                        extraType = "BOOLEAN|ALL"
                    ),
                    ConfigItem(
                        category = "CUSTOM_FIELD",
                        name = "Safety Shoes Issued",
                        extraType = "BOOLEAN|LABOUR"
                    ),
                    ConfigItem(
                        category = "CUSTOM_FIELD",
                        name = "Skill Grade",
                        extraType = "DROPDOWN|LABOUR"
                    ),
                    ConfigItem(
                        category = "CUSTOM_FIELD",
                        name = "PF Number",
                        extraType = "TEXT|STAFF"
                    )
                )

                dao.insertConfigItems(departments)
                dao.insertConfigItems(designations)
                dao.insertConfigItems(units)
                dao.insertConfigItems(roles)
                dao.insertConfigItems(shifts)
                dao.insertConfigItems(customFields)

                // 2. Dummy Sample Dataset: Contractors and Employees (Visible in Debug builds only)
                if (com.gratus.bsputility.BuildConfig.DEBUG) {
                    val c1 = Contractor(
                        name = "Apex Industrial Services",
                        contactPerson = "Suresh Shinde",
                        phone = "9822011234",
                        notes = "Supplies welders & helpers"
                    )
                    val c2 = Contractor(
                        name = "Chakan Workforce Solutions",
                        contactPerson = "Mahesh Gaikwad",
                        phone = "9823055678",
                        notes = "Laser operators and fitters"
                    )
                    val c3 = Contractor(
                        name = "Sai Engineering Labour",
                        contactPerson = "Vikas Jadhav",
                        phone = "9822499102",
                        notes = "Bending & press operators"
                    )
                    val c4 = Contractor(
                        name = "Om Fabtech Services",
                        contactPerson = "Santosh Kadam",
                        phone = "9890123456",
                        notes = "Painting & general helpers"
                    )

                    val c1Id = dao.insertContractor(c1)
                    val c2Id = dao.insertContractor(c2)
                    val c3Id = dao.insertContractor(c3)
                    val c4Id = dao.insertContractor(c4)

                    // Initial Staff Employees (Permanent department belongs to staff)
                    val initialStaff = listOf(
                        Employee(
                            name = "Rakesh Kulkarni",
                            type = EmployeeTypes.STAFF,
                            status = EmployeeStatuses.ACTIVE,
                            dateAdded = todayStr,
                            permanentDepartment = "HR & Admin",
                            designation = "HR Trainee",
                            permanentRemarks = "Handles daily morning manpower reports"
                        ),
                        Employee(
                            name = "Rajesh Patil",
                            type = EmployeeTypes.STAFF,
                            status = EmployeeStatuses.ACTIVE,
                            dateAdded = todayStr,
                            permanentDepartment = "Welding Shop",
                            designation = "Department Head",
                            permanentRemarks = "Welding section in-charge"
                        ),
                        Employee(
                            name = "Anil Deshmukh",
                            type = EmployeeTypes.STAFF,
                            status = EmployeeStatuses.ACTIVE,
                            dateAdded = todayStr,
                            permanentDepartment = "Laser Cutting",
                            designation = "Department Head",
                            permanentRemarks = "Laser department supervisor"
                        ),
                        Employee(
                            name = "Sunil More",
                            type = EmployeeTypes.STAFF,
                            status = EmployeeStatuses.ACTIVE,
                            dateAdded = todayStr,
                            permanentDepartment = "Press & Bending",
                            designation = "Department Head",
                            permanentRemarks = "Press shop in-charge"
                        ),
                        Employee(
                            name = "Ganesh Chavan",
                            type = EmployeeTypes.STAFF,
                            status = EmployeeStatuses.ACTIVE,
                            dateAdded = todayStr,
                            permanentDepartment = "Paint Shop",
                            designation = "Department Head",
                            permanentRemarks = "Paint line supervisor"
                        ),
                        Employee(
                            name = "Sachin Jagtap",
                            type = EmployeeTypes.STAFF,
                            status = EmployeeStatuses.ACTIVE,
                            dateAdded = todayStr,
                            permanentDepartment = "Assembly & Quality",
                            designation = "Quality Engineer",
                            permanentRemarks = "Final inspection"
                        )
                    )

                    // Initial Contract Labourers (No permanent department; allotted daily each morning)
                    val initialLabourers = listOf(
                        Employee(
                            name = "Ramesh Pawar",
                            type = EmployeeTypes.LABOUR,
                            status = EmployeeStatuses.ACTIVE,
                            dateAdded = todayStr,
                            permanentDepartment = "Welding Shop",
                            contractorId = c1Id,
                            contractorName = "Apex Industrial Services",
                            defaultWorkRole = "Welder",
                            defaultUnit = "Unit I",
                            defaultShift = "Shift A"
                        ),
                        Employee(
                            name = "Vijay Thorat",
                            type = EmployeeTypes.LABOUR,
                            status = EmployeeStatuses.ACTIVE,
                            dateAdded = todayStr,
                            permanentDepartment = "Welding Shop",
                            contractorId = c1Id,
                            contractorName = "Apex Industrial Services",
                            defaultWorkRole = "Helper",
                            defaultUnit = "Unit I",
                            defaultShift = "Shift A"
                        ),
                        Employee(
                            name = "Pravin Shinde",
                            type = EmployeeTypes.LABOUR,
                            status = EmployeeStatuses.ACTIVE,
                            dateAdded = todayStr,
                            permanentDepartment = "Laser Cutting",
                            contractorId = c2Id,
                            contractorName = "Chakan Workforce Solutions",
                            defaultWorkRole = "Laser Operator",
                            defaultUnit = "Unit I",
                            defaultShift = "Shift A"
                        ),
                        Employee(
                            name = "Dnyaneshwar Kale",
                            type = EmployeeTypes.LABOUR,
                            status = EmployeeStatuses.ACTIVE,
                            dateAdded = todayStr,
                            permanentDepartment = "Laser Cutting",
                            contractorId = c2Id,
                            contractorName = "Chakan Workforce Solutions",
                            defaultWorkRole = "Helper",
                            defaultUnit = "Unit I",
                            defaultShift = "Shift A"
                        ),
                        Employee(
                            name = "Amol Bhosale",
                            type = EmployeeTypes.LABOUR,
                            status = EmployeeStatuses.ACTIVE,
                            dateAdded = todayStr,
                            permanentDepartment = "Press & Bending",
                            contractorId = c3Id,
                            contractorName = "Sai Engineering Labour",
                            defaultWorkRole = "Bender",
                            defaultUnit = "Unit II",
                            defaultShift = "Shift A"
                        ),
                        Employee(
                            name = "Nitin Salunkhe",
                            type = EmployeeTypes.LABOUR,
                            status = EmployeeStatuses.ACTIVE,
                            dateAdded = todayStr,
                            permanentDepartment = "Press & Bending",
                            contractorId = c3Id,
                            contractorName = "Sai Engineering Labour",
                            defaultWorkRole = "Helper",
                            defaultUnit = "Unit II",
                            defaultShift = "Shift A"
                        ),
                        Employee(
                            name = "Rahul Wagh",
                            type = EmployeeTypes.LABOUR,
                            status = EmployeeStatuses.ACTIVE,
                            dateAdded = todayStr,
                            permanentDepartment = "Paint Shop",
                            contractorId = c4Id,
                            contractorName = "Om Fabtech Services",
                            defaultWorkRole = "Painter",
                            defaultUnit = "Unit II",
                            defaultShift = "Shift B"
                        ),
                        Employee(
                            name = "Deepak Kamble",
                            type = EmployeeTypes.LABOUR,
                            status = EmployeeStatuses.ACTIVE,
                            dateAdded = todayStr,
                            permanentDepartment = "Paint Shop",
                            contractorId = c4Id,
                            contractorName = "Om Fabtech Services",
                            defaultWorkRole = "Helper",
                            defaultUnit = "Unit II",
                            defaultShift = "Shift B"
                        ),
                        Employee(
                            name = "Kishor Mane",
                            type = EmployeeTypes.HOUSEKEEPING,
                            status = EmployeeStatuses.ACTIVE,
                            dateAdded = todayStr,
                            permanentDepartment = "Housekeeping",
                            contractorId = c4Id,
                            contractorName = "Om Fabtech Services",
                            defaultWorkRole = "Housekeeping",
                            defaultUnit = "Unit I",
                            defaultShift = "General"
                        ),
                        Employee(
                            name = "Sanjay Gade",
                            type = EmployeeTypes.LABOUR,
                            status = EmployeeStatuses.DEBARRED,
                            dateAdded = todayStr,
                            permanentDepartment = "Welding Shop",
                            contractorId = c1Id,
                            contractorName = "Apex Industrial Services",
                            defaultWorkRole = "Helper",
                            defaultUnit = "Unit I",
                            defaultShift = "Shift A",
                            permanentRemarks = "Safety violation - under review"
                        )
                    )

                    dao.insertEmployees(initialStaff)
                    dao.insertEmployees(initialLabourers)
                }
            }
        }
    }
}
