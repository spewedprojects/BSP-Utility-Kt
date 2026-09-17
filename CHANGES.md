# CHANGES v2.2.0: Implementation of Issues #21, #22

Resolved all 2 requested issues.

---

## Key Changes Summary

### 1. Issue #21: Editing Pre-Existing Master Data Roles & Config Items (Backwards Compatible)
- **Edit Option for Master Items**: Added an Edit icon button to all Config Item cards (Departments, Designations, Units, Shifts, Labour Roles, Custom Fields) on the [`MasterDataScreen`](app/src/main/java/com/gratus/bsputility/ui/screens/MasterDataScreen.kt).
- **Edit Dialog**: Updated `AddConfigDialog` to support both Add and Edit modes. When editing an existing Labour Role, its current associated departments are pre-selected in chips and dropdowns. When editing Custom Fields, the field type and target audience are pre-loaded.
- **Backwards Compatibility**:
  - Existing/seeded roles with blank or `"ALL"` `extraType` remain applicable to all departments by default.
  - Editing a role immediately updates its `ConfigItem.extraType` and dynamically takes effect across [`AttendanceLabourDialog`](app/src/main/java/com/gratus/bsputility/ui/components/AttendanceLabourDialog.kt), [`AttendanceScreen`](app/src/main/java/com/gratus/bsputility/ui/screens/AttendanceScreen.kt), and [`RoosterScreen`](app/src/main/java/com/gratus/bsputility/ui/screens/RoosterScreen.kt).
- **DAO & ViewModel Support**: Added `@Update suspend fun updateConfigItem` in [`EmployeeDao`](app/src/main/java/com/gratus/bsputility/data/db/EmployeeDao.kt), [`ManpowerRepository`](app/src/main/java/com/gratus/bsputility/data/repository/ManpowerRepository.kt), and [`ManpowerViewModel`](app/src/main/java/com/gratus/bsputility/ui/viewmodel/ManpowerViewModel.kt).

### 2. Issue #22: Importing CSV Schema Mismatch & Contractor Link Preservation
- **Bug Discovery**: `generateRoosterCsv()` exported 11 columns (`Name, Type, Status, Department, Designation, Contractor, Default Role, Default Unit, Default Shift, Date Added, Remarks`), whereas legacy `importCsv()` expected an 8-column layout without `Designation`. When re-importing an exported CSV:
  - Column 4 (`Designation`, empty for labour) was read as Contractor name (clearing contractor links).
  - Column 5 (`Contractor`) was read as Role (making Contractor name become the Role).
  - Column 6 (`Default Role`) was read as Unit.
  - Column 7 (`Default Unit`) was read as Shift.
- **Fix**:
  - Re-engineered `importCsv()` in [`ManpowerViewModel.kt`](app/src/main/java/com/gratus/bsputility/ui/viewmodel/ManpowerViewModel.kt) to be dynamically **header-aware** (case-insensitive name matching for all columns).
  - Added full RFC-4180 parsing supporting quotes, escaped quotes (`""`), and commas within fields.
  - Added backwards-compatibility fallbacks for legacy 8-column CSVs as well as new 11-column CSVs.
  - Matched and linked `contractorId` with `allContractors` during CSV import.
  - Updated [`RoosterScreen.kt`](app/src/main/java/com/gratus/bsputility/ui/screens/RoosterScreen.kt) to allow importing from both pasted CSV and pasted JSON in the text restore dialog.

---