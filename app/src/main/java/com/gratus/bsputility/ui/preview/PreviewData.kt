package com.gratus.bsputility.ui.preview

import com.gratus.bsputility.data.models.ConfigItem
import com.gratus.bsputility.data.models.Contractor
import com.gratus.bsputility.data.models.DepartmentVerification
import com.gratus.bsputility.data.models.Employee
import com.gratus.bsputility.data.models.EmployeeStatuses
import com.gratus.bsputility.data.models.EmployeeTypes
import com.gratus.bsputility.data.models.ManpowerSummary
import com.gratus.bsputility.ui.viewmodel.ManpowerViewModel

object PreviewData {

    val sampleContractors = listOf(
        Contractor(
            id = 1L,
            name = "Om Sai Enterprises",
            contactPerson = "Ramesh Patil",
            phone = "9876543210",
            notes = "Provides certified welders and fitters"
        ),
        Contractor(
            id = 2L,
            name = "Shree Ganesh Manpower",
            contactPerson = "Sunil Shinde",
            phone = "9823456789",
            notes = "Press operators and helpers"
        ),
        Contractor(
            id = 3L,
            name = "Apex Facilities & Services",
            contactPerson = "Mahesh Jadhav",
            phone = "9765432109",
            notes = "Housekeeping & sanitation team"
        )
    )

    val sampleConfigItems = listOf(
        // Departments
        ConfigItem(id = 1L, category = "DEPARTMENT", name = "Laser Cutting"),
        ConfigItem(id = 2L, category = "DEPARTMENT", name = "Fabrication"),
        ConfigItem(id = 3L, category = "DEPARTMENT", name = "Welding Shop"),
        ConfigItem(id = 4L, category = "DEPARTMENT", name = "Press Shop"),
        ConfigItem(id = 5L, category = "DEPARTMENT", name = "Maintenance"),
        ConfigItem(id = 6L, category = "DEPARTMENT", name = "Store / Dispatch"),

        // Roles
        ConfigItem(id = 7L, category = "LABOUR_ROLE", name = "Helper"),
        ConfigItem(id = 8L, category = "LABOUR_ROLE", name = "Welder"),
        ConfigItem(id = 9L, category = "LABOUR_ROLE", name = "Operator"),
        ConfigItem(id = 10L, category = "LABOUR_ROLE", name = "Laser Operator"),
        ConfigItem(id = 11L, category = "LABOUR_ROLE", name = "Fitter"),
        ConfigItem(id = 12L, category = "LABOUR_ROLE", name = "Bender"),
        ConfigItem(id = 13L, category = "LABOUR_ROLE", name = "Painter"),

        // Designations
        ConfigItem(id = 14L, category = "DESIGNATION", name = "Plant Manager"),
        ConfigItem(id = 15L, category = "DESIGNATION", name = "Production Supervisor"),
        ConfigItem(id = 16L, category = "DESIGNATION", name = "Quality In-charge"),
        ConfigItem(id = 17L, category = "DESIGNATION", name = "Shift In-charge"),
        ConfigItem(id = 18L, category = "DESIGNATION", name = "HR Trainee"),

        // Units
        ConfigItem(id = 19L, category = "UNIT", name = "Unit I"),
        ConfigItem(id = 20L, category = "UNIT", name = "Unit II"),
        ConfigItem(id = 21L, category = "UNIT", name = "Unit III"),

        // Custom Fields
        ConfigItem(id = 22L, category = "CUSTOM_FIELD", name = "PF Number", extraType = "TEXT"),
        ConfigItem(id = 23L, category = "CUSTOM_FIELD", name = "Safety Shoes Issued", extraType = "BOOLEAN")
    )

    val sampleEmployees = listOf(
        // Staff
        Employee(
            id = 1L,
            name = "Rahul Kulkarni",
            type = EmployeeTypes.STAFF,
            status = EmployeeStatuses.ACTIVE,
            dateAdded = "2026-09-01",
            permanentDepartment = "Fabrication",
            designation = "Production Supervisor",
            defaultUnit = "Unit I",
            defaultShift = "Shift A"
        ),
        Employee(
            id = 2L,
            name = "Sneha Deshmukh",
            type = EmployeeTypes.STAFF,
            status = EmployeeStatuses.ACTIVE,
            dateAdded = "2026-09-01",
            permanentDepartment = "Laser Cutting",
            designation = "Shift In-charge",
            defaultUnit = "Unit I",
            defaultShift = "Shift A"
        ),
        Employee(
            id = 3L,
            name = "Amit Sharma",
            type = EmployeeTypes.STAFF,
            status = EmployeeStatuses.ACTIVE,
            dateAdded = "2026-09-02",
            permanentDepartment = "Store / Dispatch",
            designation = "Store In-charge",
            defaultUnit = "Unit I",
            defaultShift = "General"
        ),

        // Contract Labour
        Employee(
            id = 4L,
            name = "Vijay Thorat",
            type = EmployeeTypes.LABOUR,
            status = EmployeeStatuses.ACTIVE,
            dateAdded = "2026-09-05",
            permanentDepartment = "Welding Shop",
            contractorId = 1L,
            contractorName = "Om Sai Enterprises",
            defaultWorkRole = "Welder",
            defaultUnit = "Unit I",
            defaultShift = "Shift A"
        ),
        Employee(
            id = 5L,
            name = "Pravin Shinde",
            type = EmployeeTypes.LABOUR,
            status = EmployeeStatuses.ACTIVE,
            dateAdded = "2026-09-05",
            permanentDepartment = "Laser Cutting",
            contractorId = 2L,
            contractorName = "Shree Ganesh Manpower",
            defaultWorkRole = "Laser Operator",
            defaultUnit = "Unit I",
            defaultShift = "Shift A"
        ),
        Employee(
            id = 6L,
            name = "Santosh More",
            type = EmployeeTypes.LABOUR,
            status = EmployeeStatuses.ACTIVE,
            dateAdded = "2026-09-06",
            permanentDepartment = "Press Shop",
            contractorId = 2L,
            contractorName = "Shree Ganesh Manpower",
            defaultWorkRole = "Operator",
            defaultUnit = "Unit II",
            defaultShift = "Shift B"
        ),
        Employee(
            id = 7L,
            name = "Deepak Pawar",
            type = EmployeeTypes.LABOUR,
            status = EmployeeStatuses.DEBARRED,
            dateAdded = "2026-09-07",
            permanentDepartment = "Fabrication",
            contractorId = 1L,
            contractorName = "Om Sai Enterprises",
            defaultWorkRole = "Helper",
            defaultUnit = "Unit I",
            defaultShift = "Shift A",
            permanentRemarks = "Safety violation warning"
        ),
        Employee(
            id = 8L,
            name = "Ganesh Gaikwad",
            type = EmployeeTypes.LABOUR,
            status = EmployeeStatuses.OUT,
            dateAdded = "2026-08-15",
            permanentDepartment = "Maintenance",
            contractorId = 2L,
            contractorName = "Shree Ganesh Manpower",
            defaultWorkRole = "Fitter",
            defaultUnit = "Unit I",
            defaultShift = "Shift A"
        ),

        // Housekeeping
        Employee(
            id = 9L,
            name = "Rekha Kamble",
            type = EmployeeTypes.HOUSEKEEPING,
            status = EmployeeStatuses.ACTIVE,
            dateAdded = "2026-09-01",
            permanentDepartment = "Plant",
            contractorId = 3L,
            contractorName = "Apex Facilities & Services",
            defaultWorkRole = "Cleaner",
            defaultUnit = "Unit I",
            defaultShift = "General"
        ),
        Employee(
            id = 10L,
            name = "Sunita Shinde",
            type = EmployeeTypes.HOUSEKEEPING,
            status = EmployeeStatuses.ACTIVE,
            dateAdded = "2026-09-01",
            permanentDepartment = "Plant",
            contractorId = 3L,
            contractorName = "Apex Facilities & Services",
            defaultWorkRole = "Cleaner",
            defaultUnit = "Unit I",
            defaultShift = "General"
        )
    )

    val sampleAttendanceItems = listOf(
        ManpowerViewModel.EmployeeAttendanceItem(
            employee = sampleEmployees[0], // Rahul Kulkarni (Staff)
            isPresent = true,
            effectiveDepartment = "Fabrication",
            effectiveWorkRole = "Production Supervisor",
            effectiveContractor = "",
            effectiveUnit = "Unit I",
            effectiveShift = "Shift A",
            attendanceTime = "08:45 AM",
            dayRemarks = "",
            attendanceId = 1L
        ),
        ManpowerViewModel.EmployeeAttendanceItem(
            employee = sampleEmployees[1], // Sneha Deshmukh (Staff)
            isPresent = true,
            effectiveDepartment = "Laser Cutting",
            effectiveWorkRole = "Shift In-charge",
            effectiveContractor = "",
            effectiveUnit = "Unit I",
            effectiveShift = "Shift A",
            attendanceTime = "09:00 AM",
            dayRemarks = "Laser floor round",
            attendanceId = 2L
        ),
        ManpowerViewModel.EmployeeAttendanceItem(
            employee = sampleEmployees[2], // Amit Sharma (Staff)
            isPresent = false,
            effectiveDepartment = "Store / Dispatch",
            effectiveWorkRole = "Store In-charge",
            effectiveContractor = "",
            effectiveUnit = "Unit I",
            effectiveShift = "General",
            attendanceTime = "",
            dayRemarks = "On leave",
            attendanceId = 3L
        ),
        ManpowerViewModel.EmployeeAttendanceItem(
            employee = sampleEmployees[3], // Vijay Thorat (Labour)
            isPresent = true,
            effectiveDepartment = "Welding Shop",
            effectiveWorkRole = "Welder",
            effectiveContractor = "Om Sai Enterprises",
            effectiveUnit = "Unit I",
            effectiveShift = "Shift A",
            attendanceTime = "",
            dayRemarks = "",
            attendanceId = 4L
        ),
        ManpowerViewModel.EmployeeAttendanceItem(
            employee = sampleEmployees[4], // Pravin Shinde (Labour)
            isPresent = true,
            effectiveDepartment = "Laser Cutting",
            effectiveWorkRole = "Laser Operator",
            effectiveContractor = "Shree Ganesh Manpower",
            effectiveUnit = "Unit I",
            effectiveShift = "Shift A",
            attendanceTime = "",
            dayRemarks = "Bystronic Laser",
            attendanceId = 5L
        ),
        ManpowerViewModel.EmployeeAttendanceItem(
            employee = sampleEmployees[5], // Santosh More (Labour)
            isPresent = false,
            effectiveDepartment = "Press Shop",
            effectiveWorkRole = "Operator",
            effectiveContractor = "Shree Ganesh Manpower",
            effectiveUnit = "Unit II",
            effectiveShift = "Shift B",
            attendanceTime = "",
            dayRemarks = "",
            attendanceId = 6L
        ),
        ManpowerViewModel.EmployeeAttendanceItem(
            employee = sampleEmployees[6], // Deepak Pawar (Debarred)
            isPresent = false,
            effectiveDepartment = "Fabrication",
            effectiveWorkRole = "Helper",
            effectiveContractor = "Om Sai Enterprises",
            effectiveUnit = "Unit I",
            effectiveShift = "Shift A",
            dayRemarks = "Safety violation",
            attendanceTime = "",
            attendanceId = 7L
        ),
        ManpowerViewModel.EmployeeAttendanceItem(
            employee = sampleEmployees[8], // Rekha Kamble (Housekeeping)
            isPresent = true,
            effectiveDepartment = "Plant",
            effectiveWorkRole = "Cleaner",
            effectiveContractor = "Apex Facilities & Services",
            effectiveUnit = "Unit I",
            effectiveShift = "General",
            attendanceTime = "",
            dayRemarks = "",
            attendanceId = 8L
        )
    )

    val sampleSummary = ManpowerSummary(
        totalStaffPresent = 2,
        totalLabourersPresent = 2,
        totalHousekeepingPresent = 1,
        grandTotalPresent = 5,
        contractorCounts = mapOf(
            "Om Sai Enterprises" to 1,
            "Shree Ganesh Manpower" to 1,
            "Apex Facilities & Services" to 1
        ),
        departmentCounts = mapOf(
            "Laser Cutting" to 2,
            "Fabrication" to 1,
            "Welding Shop" to 1,
            "Plant" to 1
        ),
        unitCounts = mapOf(
            "Unit I" to 4,
            "Unit II" to 1
        ),
        roleCounts = mapOf(
            "Production Supervisor" to 1,
            "Shift In-charge" to 1,
            "Welder" to 1,
            "Laser Operator" to 1,
            "Cleaner" to 1
        ),
        shiftCounts = mapOf(
            "Shift A" to 3,
            "Shift B" to 1,
            "General" to 1
        ),
        verifiedDepartmentsCount = 2,
        totalDepartmentsCount = 4
    )

    val sampleVerifications = listOf(
        DepartmentVerification(
            id = 1L,
            date = "2026-09-10",
            departmentName = "Welding Shop",
            isVerified = true,
            verifiedByStaffId = 1L,
            verifiedByStaffName = "Rahul Kulkarni",
            verifiedAtTime = "09:30 AM",
            remarks = "All welders present on line"
        ),
        DepartmentVerification(
            id = 2L,
            date = "2026-09-10",
            departmentName = "Laser Cutting",
            isVerified = true,
            verifiedByStaffId = 2L,
            verifiedByStaffName = "Sneha Deshmukh",
            verifiedAtTime = "09:45 AM",
            remarks = "2 shifts confirmed"
        ),
        DepartmentVerification(
            id = 3L,
            date = "2026-09-10",
            departmentName = "Fabrication",
            isVerified = false,
            verifiedByStaffId = null,
            verifiedByStaffName = "",
            verifiedAtTime = "",
            remarks = ""
        ),
        DepartmentVerification(
            id = 4L,
            date = "2026-09-10",
            departmentName = "Press Shop",
            isVerified = false,
            verifiedByStaffId = null,
            verifiedByStaffName = "",
            verifiedAtTime = "",
            remarks = ""
        )
    )
}
