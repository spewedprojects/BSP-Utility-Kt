# CHANGES v1.3.0: Debug-Only Dummy Data, Labour Dynamic Department & Rooster Filter Chips

All requested updates have been implemented and verified with automated tests, debug build, and release R8 minification.

---

## 1. Features Implemented & Changes Made

### 1. Dummy Dataset Gated to Debug Builds Only
- **Context**: In production/release builds, the app should start with a clean database for actual factory operations without dummy staff or sample contractors. Base configuration items (departments, designations, units, roles, shifts, custom fields) must remain available.
- **Changes**:
    - In [`AppDatabase.kt`](file:///c:/Users/rkhar/Documents/ANDROID%20APPS/BSPUtility/app/src/main/java/com/gratus/bsputility/data/db/AppDatabase.kt) (`populateInitialData`):
        - Base `ConfigItem` records (departments, designations, units, roles, shifts, custom fields) are seeded **unconditionally** for both debug and release.
        - Dummy contractors (`c1..c4`) and sample employees (`initialStaff`, `initialLabourers`) are wrapped in `if (com.gratus.bsputility.BuildConfig.DEBUG)`.
        - In release builds, the employee and contractor tables remain completely empty for genuine data entry.

### 2. Removal of Permanent Department for Labourers
- **Context**: Contract labourers in a manufacturing plant are not assigned to a fixed department in the Rooster; their department is allotted dynamically each morning during daily attendance. Only permanent Staff have a permanent department.
- **Changes**:
    - In [`Employee.kt`](file:///c:/Users/rkhar/Documents/ANDROID%20APPS/BSPUtility/app/src/main/java/com/gratus/bsputility/data/models/Employee.kt): Default value for `permanentDepartment` updated from `"Welding Shop"` to `""`.
    - In [`RoosterScreen.kt`](file:///c:/Users/rkhar/Documents/ANDROID%20APPS/BSPUtility/app/src/main/java/com/gratus/bsputility/ui/screens/RoosterScreen.kt):
        - **Add/Edit Dialog**: "Permanent Department" dropdown is only rendered when `type == EmployeeTypes.STAFF`.
        - **Save Validation**: When saving, `permanentDepartment = if (type == EmployeeTypes.STAFF) department.trim() else ""`.
        - **FAB Creation**: Newly created employees default to `permanentDepartment = ""`.
        - **Employee Card**: Only displays permanent department if `employee.type == EmployeeTypes.STAFF`. For contract labourers, displays their default work role and contractor info.
    - In [`ManpowerViewModel.kt`](file:///c:/Users/rkhar/Documents/ANDROID%20APPS/BSPUtility/app/src/main/java/com/gratus/bsputility/ui/viewmodel/ManpowerViewModel.kt):
        - `importPastedNames`: Sets `permanentDepartment = if (targetType == EmployeeTypes.STAFF) defaultDept else ""`.
        - `importCsv`: Sets `permanentDepartment = if (type == EmployeeTypes.STAFF) dept else ""`.
        - `effectiveAttendanceItems`: If a labourer has not yet had a department allotted for the day, `effectiveDepartment` cleanly falls back to `"Unassigned"`.
    - In [`AttendanceLabourDialog.kt`](file:///c:/Users/rkhar/Documents/ANDROID%20APPS/BSPUtility/app/src/main/java/com/gratus/bsputility/ui/components/AttendanceLabourDialog.kt):
        - When opening the daily assignment dialog for an unallotted labourer, `selectedDept` defaults to the first available department (`availableDepartments.firstOrNull() ?: "Welding Shop"`), allowing one-tap assignment.

### 3. Contractor and Department Filter Chips in Rooster Screen
- **Context**: The Attendance screen provides dropdown filter chips for Contractor and Department; the Rooster (Employee Library) screen needed matching filter chips for consistency.
- **Changes**:
    - In [`ManpowerViewModel.kt`](file:///c:/Users/rkhar/Documents/ANDROID%20APPS/BSPUtility/app/src/main/java/com/gratus/bsputility/ui/viewmodel/ManpowerViewModel.kt):
        - Added `val roosterFilterContractor = MutableStateFlow<String?>(null)`
        - Added `val roosterFilterDepartment = MutableStateFlow<String?>(null)`
    - In [`RoosterScreen.kt`](file:///c:/Users/rkhar/Documents/ANDROID%20APPS/BSPUtility/app/src/main/java/com/gratus/bsputility/ui/screens/RoosterScreen.kt):
        - Connected `roosterFilterContractor` and `roosterFilterDepartment` to `RoosterScreenContent`.
        - Added Contractor and Department dropdown `FilterChip`s with leading `Icons.Default.FilterList` in the Rooster filter row (`LazyRow`).
        - Menus include "All Contractors" and "All Departments" to easily clear filters.
        - Updated `filteredEmployees` to filter by `matchesContractor` and `matchesDepartment`.

---