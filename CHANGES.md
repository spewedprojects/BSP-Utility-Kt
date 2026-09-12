# CHANGES v2.0.0: Implementation of Issues #4 through #10

All 7 issues identified in `misc/open_issues_BSP_Manpower_export_1789187255240.json` plus the user requirement to clean existing staff-contractor associations have been implemented and verified.

---

## Changes Summary

### 1. Issue #4: Department Verification Cards Filtering
- **File**: [VerificationScreen.kt](app/src/main/java/com/gratus/bsputility/ui/screens/VerificationScreen.kt)
- **Problem**: Verification screen was rendering empty cards for departments that had no active or present workers on that date.
- **Solution**:
  - Filtered `allDepts` so that only departments having assigned labourers (`deptMap[dept]?.isNotEmpty() == true`) or workers marked present on that date (`presentDeptsWithWorkers.contains(dept)`) are displayed.
  - Added an informative placeholder state with `Icons.Default.VerifiedUser` and message when no departments have active workers for the selected date.

### 2. Issue #5: "Mark All" Highlight & Snapshot Undo
- **Files**:
  - [ManpowerViewModel.kt](app/src/main/java/com/gratus/bsputility/ui/viewmodel/ManpowerViewModel.kt)
  - [AttendanceScreen.kt](app/src/main/java/com/gratus/bsputility/ui/screens/AttendanceScreen.kt)
- **Problem**: "Mark All" did not clearly indicate when all active workers were present, and tapping it in that state did not restore previous marks, making accidental taps destructive.
- **Solution**:
  - Added `areAllActivePresent: StateFlow<Boolean>` derived from `_attendanceStream` and `allEmployees`.
  - Added `preMarkAllSnapshot` to cache attendance states immediately before a "Mark All" mass-mark.
  - If `areAllActivePresent == true`, tapping the button executes an Undo action, restoring `preMarkAllSnapshot` so previously absent/partial marks are restored without data loss.
  - In `AttendanceScreen.kt`, styled the button dynamically: when all active workers are present, it turns `PresentGreen` (`containerColor = PresentGreen`), text displays `"All Present (Undo)"`, and icon shows `Icons.Default.Undo`.

### 3. Issue #6 & User Requirement: Staff Contractor Sanitization & DB Cleanup
- **Files**:
  - [EmployeeDao.kt](app/src/main/java/com/gratus/bsputility/data/db/EmployeeDao.kt)
  - [AppDatabase.kt](app/src/main/java/com/gratus/bsputility/data/db/AppDatabase.kt)
  - [ManpowerRepository.kt](app/src/main/java/com/gratus/bsputility/data/repository/ManpowerRepository.kt)
  - [ManpowerViewModel.kt](app/src/main/java/com/gratus/bsputility/ui/viewmodel/ManpowerViewModel.kt)
  - [RoosterScreen.kt](app/src/main/java/com/gratus/bsputility/ui/screens/RoosterScreen.kt)
  - [MasterDataScreen.kt](app/src/main/java/com/gratus/bsputility/ui/screens/MasterDataScreen.kt)
- **Problem**: Accidental contractor links were leaking into Staff records through UI dialogues, import functions, and legacy data.
- **Solution**:
  - **Room Database Cleanup**: Added `clearStaffContractorAssociations()` and `clearStaffAttendanceContractors()` in `EmployeeDao` executing:
    ```sql
    UPDATE employees SET contractorName = '', contractorId = NULL WHERE type = 'Staff' AND (contractorName != '' OR contractorId IS NOT NULL)
    UPDATE daily_attendance SET contractorName = '' WHERE employeeType = 'Staff' AND contractorName != ''
    ```
  - **Automatic on-open cleanup**: Wired into `AppDatabase.kt` Room callback to automatically sanitize legacy staff data whenever the DB opens.
  - **Manual Cleanup Option**: Added a "Clear Staff Contractor Links" button in Master Settings under the new Preferences tab.
  - **Creation & Import Sanitization**: Ensured `importPastedNames()`, `importCsv()`, `importAllDataJson()`, FAB creation, and `AddEditEmployeeDialog` strictly clear `contractorId = null` and `contractorName = ""` whenever employee type is `Staff`.

### 4. Issue #7: Compact Dropdown in PasteImportDialog
- **File**: [RoosterScreen.kt](app/src/main/java/com/gratus/bsputility/ui/screens/RoosterScreen.kt)
- **Problem**: The radio-button list inside `PasteImportDialog` caused vertical bloat and wasted valuable dialog space.
- **Solution**:
  - Replaced the radio buttons with an interactive Compose `DropdownMenu` and `OutlinedCard` displaying the current selection and an arrow icon.
  - When Staff is selected, contractor selection is completely hidden, and a Department dropdown is shown instead.

### 5. Issue #8: "All Contractors" Filter & Mutual Exclusivity
- **Files**:
  - [ManpowerViewModel.kt](app/src/main/java/com/gratus/bsputility/ui/viewmodel/ManpowerViewModel.kt)
  - [RoosterScreen.kt](app/src/main/java/com/gratus/bsputility/ui/screens/RoosterScreen.kt)
  - [AttendanceScreen.kt](app/src/main/java/com/gratus/bsputility/ui/screens/AttendanceScreen.kt)
- **Problem**: Filtering by contractor could inadvertently show Staff or conflict when both "Staff" and contractor filters were active. There was also no option to view only contractor workers while excluding Staff.
- **Solution**:
  - Added constant `ALL_CONTRACTORS = "__ALL_CONTRACTORS__"` which matches all workers who are NOT Staff and have an assigned contractor.
  - Rendered "All Contractors" chip at the beginning of the contractor horizontal chip list in both Rooster and Attendance screens.
  - Enforced mutual exclusivity in ViewModel mutators (`setAttendanceFilterType`, `setAttendanceFilterContractor`, `setRoosterFilterStatus`, `setRoosterFilterContractor`):
    - When "Staff" is selected, contractor filter is automatically cleared (`null`).
    - When any contractor or "All Contractors" is selected, the filter automatically switches away from "Staff".

### 6. Issue #9: Staff Unit and Shift Editable & Saveable
- **File**: [RoosterScreen.kt](app/src/main/java/com/gratus/bsputility/ui/screens/RoosterScreen.kt)
- **Problem**: Unit and Shift dropdowns in `AddEditEmployeeDialog` were nested inside the labour-only branch and unavailable for Staff.
- **Solution**:
  - Moved the Unit and Shift dropdown selectors outside the labour conditional block so they are accessible and editable for both Staff and Labour.
  - Confirmed `defaultUnit` and `defaultShift` are properly saved on creation and edits.

### 7. Issue #10: Epoch Timestamp Migration, TimeInputComponent & Preferences
- **Files**:
  - [Employee.kt](app/src/main/java/com/gratus/bsputility/data/models/Employee.kt)
  - [AppDatabase.kt](app/src/main/java/com/gratus/bsputility/data/db/AppDatabase.kt)
  - [TimeInputComponent.kt](app/src/main/java/com/gratus/bsputility/ui/components/TimeInputComponent.kt)
  - [AttendanceStaffDialog.kt](app/src/main/java/com/gratus/bsputility/ui/components/AttendanceStaffDialog.kt)
  - [MasterDataScreen.kt](app/src/main/java/com/gratus/bsputility/ui/screens/MasterDataScreen.kt)
- **Problem**: Staff attendance time was stored only as ambiguous strings (e.g., "08:30 AM"), prone to formatting inconsistencies and unsuited for exact duration calculations.
- **Solution**:
  - **Database Migration**: Added `attendanceTimestamp: Long = 0L` to `DailyAttendance` table. Incremented Room DB version from 1 to 2 with `MIGRATION_1_2`:
    ```sql
    ALTER TABLE daily_attendance ADD COLUMN attendanceTimestamp INTEGER NOT NULL DEFAULT 0
    ```
  - **Auto-migration**: On open, legacy records with string times are converted to epoch timestamps based on date and parsed time strings.
  - **Custom TimeInputComponent**:
    - Interactive Hour LazyRow (1-12 in 12h mode, 0-23 in 24h mode)
    - Interactive Minute LazyRow with **all 60 minutes** (00 through 59)
    - AM/PM toggle chip in 12h mode
    - Direct numeric keypad toggle button (`Icons.Default.Keyboard`) allowing freehand typing
    - Quick preset buttons (`"Now"`, `"08:00 AM"`, `"08:30 AM"`, `"09:00 AM"`, `"05:00 PM"`)
  - **Master Settings Preferences Tab**:
    - Added "Preferences" tab to Master Settings.
    - Radio selector for 12-Hour vs 24-Hour time display format, persisted in `SharedPreferences`.
    - Data Maintenance buttons: "Clear Staff Contractor Links" and "Migrate Legacy Time Records" with user Toast feedback.
    - Floating Action Button is hidden when on the Preferences tab to avoid clutter.

---