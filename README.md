<div align="center">
<img width="200" height="200" alt="App Icon" src="docs/appIcon.svg" />
</div>

# BSP Utility Tool — Contract Labour & Staff Attendance Tracker

A mobile-first attendance management and manpower tracking application for **BSP Metatech LLP, Chakan**.
Build with Kotlin.

Designed specifically for HR trainees and site supervisors to eliminate manual logbook sifting and generate 
formatted **morning manpower reports before the 11:00 AM daily deadline**.

## Problem & Context

At BSP Metatech LLP (manufacturing plant in Chakan), contract labourers are supplied by **four contractors** (Contractor A, B, C, D) and assigned dynamically across work departments (Welding, Laser, Bending, Painting, Fitting, Fabrication, Assembly, etc.) under different supervisors and shifts (Day / Night).

Previously, tracking required manually flipping through four separate paper logbooks every morning. This application allows:
1. **Pre-maintaining the roster**: Adding staff and contract labourers with contractor, work type, supervisor, and shift details beforehand.
2. **Fast field verification**: Opening the app on a mobile phone during morning rounds and ticking workers present with a single tap.
3. **Instant tallies & reporting**: Automatically computing staff, labour, contractor, work-type, shift, and absent figures, and copying the final formatted morning report to the clipboard.

---

## Daily HR Morning Workflow (Before 11:00 AM)

1. **Advance Preparation (Roster Tab)**:
    - Tap **+ Staff** to add company staff with their reporting supervisor.
    - Tap **+ Laborer** to add contract labourers with Contractor, Work Assignment, Supervisor, and Shift.
2. **Morning Round (Today Tab)**:
    - As you visit departments or check in at the floor, simply tap each worker's card.
    - Tapping toggles their presence instantly and updates the top tally counts (Staff present, Labor present, Total on floor).
    - Data persists immediately to native storage.
3. **Generate Report (Report Tab)**:
    - Switch to **REPORT** to review the automatically organized breakdown:
        - Contractor-wise attendance
        - Work/department breakdown for present labourers
        - Day vs. Night shift split
        - Specific list of absent staff and labourers
    - Tap **Copy Report Text** to copy the report to the clipboard and paste it directly into WhatsApp, Email, or the HR morning submission portal before 11:00 AM.

## Screenshots _(to be updated)_