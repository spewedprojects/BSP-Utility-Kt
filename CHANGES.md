# CHANGES v2.1.0: Implementation of Issues #11, #12, and #13

All three new issues and the labourer defaults transfer requirement have been implemented and verified.

---

## Changes Summary

### 1. Issue #11: Permanent Department & Role for Labourers & Defaults Transfer
- **Files**:
  - [EmployeeDao.kt](app/src/main/java/com/gratus/bsputility/data/db/EmployeeDao.kt)
  - [ManpowerRepository.kt](app/src/main/java/com/gratus/bsputility/data/repository/ManpowerRepository.kt)
  - [AppDatabase.kt](app/src/main/java/com/gratus/bsputility/data/db/AppDatabase.kt)
  - [ManpowerViewModel.kt](app/src/main/java/com/gratus/bsputility/ui/viewmodel/ManpowerViewModel.kt)
  - [RoosterScreen.kt](app/src/main/java/com/gratus/bsputility/ui/screens/RoosterScreen.kt)
  - [MasterDataScreen.kt](app/src/main/java/com/gratus/bsputility/ui/screens/MasterDataScreen.kt)
- **Problem**: Labourers' permanent departments were previously omitted from rooster records, and any departments, roles, units, and shifts previously allotted on the daily attendance screen needed to be preserved and transferred back into their library profiles.
- **Solution**:
  - **Rooster UI & Dialogs**:
    - Made the "Permanent Department *" dropdown visible and selectable for all employees (both Staff and Labour) in `AddEditEmployeeDialog`.
    - Allowed setting permanent department and default role in `PasteImportDialog` for batch imports.
    - Updated rooster employee cards to display `"${employee.permanentDepartment} • ${employee.defaultWorkRole}"` for labourers.
    - Set default permanent department in the FAB template when creating new employees.
  - **Library Transfer / Backfill**:
    - Added `syncLabourerDefaultsFromAttendance()` query and repository method which scans the most recent daily attendance records and backfills `permanentDepartment`, `defaultWorkRole`, `defaultUnit`, and `defaultShift` for any labourer missing them.
    - Wired this auto-sync into `AppDatabase.onOpen` so existing user data is automatically restored.
    - Added a **"Sync Labour Defaults from Attendance"** button in Master Settings -> Preferences for on-demand manual synchronization.

### 2. Issue #12: Prevent Attendance Filters from Leaking into Verification Screen
- **Files**:
  - [ManpowerViewModel.kt](app/src/main/java/com/gratus/bsputility/ui/viewmodel/ManpowerViewModel.kt)
  - [VerificationScreen.kt](app/src/main/java/com/gratus/bsputility/ui/screens/VerificationScreen.kt)
- **Problem**: Applying a search text query or status/contractor chip filter in the Attendance Screen unintentionally reduced the attendance stream used by the Department Verification Screen and detailed CSV exports.
- **Solution**:
  - Created `dailyAttendanceItems: StateFlow<List<EmployeeAttendanceItem>>` combining `allEmployees` and `_attendanceStream` representing the complete, unfiltered daily attendance roster for that date.
  - Re-routed `effectiveAttendanceItems: StateFlow<List<EmployeeAttendanceItem>>` to apply Attendance Screen's search query and chip filters on `dailyAttendanceItems`.
  - Updated `VerificationScreen.kt` to observe `dailyAttendanceItems` rather than `effectiveAttendanceItems`.
  - Updated `generateDetailedCsv()` in `ManpowerViewModel.kt` to export `dailyAttendanceItems.value` so exports remain complete regardless of UI search state.

### 3. Issue #13: WhatsApp Report Summary Layout Update
- **File**:
  - [ManpowerViewModel.kt](app/src/main/java/com/gratus/bsputility/ui/viewmodel/ManpowerViewModel.kt)
- **Problem**: The WhatsApp report format was verbose and did not match the newly requested structured operational summary.
- **Solution**:
  - Updated `generateMorningReportWhatsApp()` to strictly match the requested template:
    ```text
    BSP Metatech LLP — Chakan
    Attendance Report — Sun, 13 Sept, 2026

    Staff present: a / b
    Labor present: c / d
    Total on floor: x

    --- By contractor ---
    [Contractor Name]: [Count]
    (or None present)

    --- By work assigned (labor present) ---
    [Role]: [Count]
    (or None present)

    --- Shift split (present) ---
    Day: y   Night: z

    --- Absent ---
    Staff: [Absent Staff Names, or None]

    --------------
    Prepared by: HR Dept - BSP Metatech
    ```
  - Formats the date with `EEE, d MMM, yyyy` (e.g. `Sun, 13 Sept, 2026`).
  - Correctly breaks down present labourers by contractor and assigned role (with `"None present"` fallbacks).
  - Categorizes shift splits dynamically between Day and Night shifts.
  - Lists absent active staff by name under `--- Absent ---` (`Staff: ...`), or `Staff: None` when all active staff are present.

---