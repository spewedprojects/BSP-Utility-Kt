# CHANGES v2.2.0: Implementation of Issues #14 – #20

We have resolved all 7 requested features and issues from `misc/open_issues_BSP_Manpower_export_1789577349621.json`.

---

## Key Changes Summary

### 1. Serial #14: Export/Import to Device Storage (`Documents/BSPManpower`) & Clean Replace
- **Physical File Storage**: Added [`StorageHelper`](app/src/main/java/com/gratus/bsputility/utils/StorageHelper.kt) saving exports to the standard public `Documents/BSPManpower` directory (with fallbacks for legacy storage) while maintaining clipboard copying.
- **System File Picker**: Integrated `ActivityResultContracts.OpenDocument()` in [`RoosterScreen`](app/src/main/java/com/gratus/bsputility/ui/screens/RoosterScreen.kt) allowing users to select single `.json` or `.csv` files from their device storage.
- **Replace Semantics**: As per user directive (*"Importing will not merge. only replace"*), added `replaceEmployees()` and `replaceAllMasterData()` in [`ManpowerRepository`](app/src/main/java/com/gratus/bsputility/data/repository/ManpowerRepository.kt) and [`EmployeeDao`](app/src/main/java/com/gratus/bsputility/data/db/EmployeeDao.kt), completely clearing existing tables before inserting imported records.
- **Reports Screen Export**: Summary and Detailed CSV exports in [`ReportsScreen`](app/src/main/java/com/gratus/bsputility/ui/screens/ReportsScreen.kt) now save timestamped `.csv` files directly to `Documents/BSPManpower/` as well as copying to the clipboard.

### 2. Serial #15: Conditional / Pre-linked Department Roles
- **Config Storage**: Associated departments for `LABOUR_ROLE` config items are stored as comma-separated values (or `"ALL"`) in `ConfigItem.extraType`, preserving database schema compatibility.
- **Master Data Customization**: Updated [`MasterDataScreen`](app/src/main/java/com/gratus/bsputility/ui/screens/MasterDataScreen.kt) with an updated role creation/edit dialog featuring quick-filter chips and multi-select dropdowns to bind roles to specific departments. Role cards display `Applies to: [Departments]`.
- **Dynamic Dialog Filtering**: In [`AttendanceLabourDialog`](app/src/main/java/com/gratus/bsputility/ui/components/AttendanceLabourDialog.kt), [`AttendanceScreen`](app/src/main/java/com/gratus/bsputility/ui/screens/AttendanceScreen.kt), and [`RoosterScreen`](app/src/main/java/com/gratus/bsputility/ui/screens/RoosterScreen.kt), role dropdowns dynamically filter to show only roles permitted for the selected department.

### 3. Serial #16: Migration Tool Transferring Shift & Unit Details
- **Root Cause**: `Employee.defaultUnit` and `defaultShift` default to `"Unit I"` and `"Shift A"`. The previous migration check (`emp.defaultUnit.isBlank()`) evaluated to `false`, skipping updates.
- **Fix**: Updated [`ManpowerRepository.syncLabourerDefaultsFromAttendance`](app/src/main/java/com/gratus/bsputility/data/repository/ManpowerRepository.kt) and [`AppDatabase.kt`](app/src/main/java/com/gratus/bsputility/data/db/AppDatabase.kt) so that if `latest.dayUnit` and `latest.dayShift` are non-blank and differ from the worker's defaults, they update the worker's default unit and shift.

### 4. Serial #17: "Same as Yesterday" with `-1` and `-2` Day Offsets
- **Multi-Day Attendance Streams**: Added `twoDaysAgoDepartmentPresentCounts`, `getDayOfWeekAbbreviation(date, offsetDays)`, and `markDepartmentSameAsDay(departmentName, dayOffset)` in [`ManpowerViewModel`](app/src/main/java/com/gratus/bsputility/ui/viewmodel/ManpowerViewModel.kt) alongside the existing `yesterdayDepartmentPresentCounts`.
- **Handling Weekly Offs (e.g. Thursdays or Sundays)**:
  - When yesterday had 0 workers (e.g. on Friday morning after Thursday off, or Monday morning after Sunday off), the system looks back up to 2 days prior (e.g. Wednesday or Saturday).
  - Departments with active workers today, yesterday (`-1d`), OR 2 days ago (`-2d`) are all displayed.
  - The verification card shows a history summary and provides two distinct side-by-side action buttons:
    - **`Same as -1 (Day)`** (e.g. `Same as -1 (Thu) (12)`)
    - **`Same as -2 (Day)`** (e.g. `Same as -2 (Wed) (14)`)
  - Tapping either copies the respective day's attendance roster for that specific department with a single click.

### 5. Serial #18: Tap-to-Show List on Reports Screen
- **Interactive Metrics**: In [`ReportsScreen`](app/src/main/java/com/gratus/bsputility/ui/screens/ReportsScreen.kt):
  - Hero counters (*Total Manpower*, *Staff Present*, *Contract Labour*) are clickable to open a [`WorkersListDialog`](app/src/main/java/com/gratus/bsputility/ui/screens/ReportsScreen.kt).
  - Breakdown items (*Contractor*, *Unit*, *Shift*, *Role*) are clickable to filter and display the matching workers.
- **Worker Details**: Each item in the list modal displays worker name, type badge (Staff/Role), department, contractor, unit, and shift.

### 6. Serial #19: Two-Column Department-Wise Allocation
- **Staff vs Labour Segregation**: In [`Employee.kt`](app/src/main/java/com/gratus/bsputility/data/models/Employee.kt), added `departmentStaffCounts` and `departmentLabourCounts` to `ManpowerSummary`.
- **Table Card UI**: In [`ReportsScreen`](app/src/main/java/com/gratus/bsputility/ui/screens/ReportsScreen.kt), replaced single-count breakdown with `DepartmentAllocationCard` presenting a clean table:
  - Columns: **Department | Staff | Labour | Total**
  - Tapping Staff or Labour count opens the worker modal for that specific category in that department.

### 7. Serial #20: Role Headings and Night Shift Segregation
- **Grouped Labourer List**: In both [`VerificationScreen`](app/src/main/java/com/gratus/bsputility/ui/screens/VerificationScreen.kt) and the [`WorkersListDialog`](app/src/main/java/com/gratus/bsputility/ui/screens/ReportsScreen.kt):
  - Workers are segregated into **Day Shift** vs **🌙 Night Shift**.
  - If a group contains more than one role (e.g. Welder, Helper, Fitter), workers are grouped and displayed under distinct bold role subheadings with worker counts.

---