package com.gratus.bsputility

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gratus.bsputility.data.models.EmployeeTypes
import com.gratus.bsputility.ui.components.DateNavigationBar
import com.gratus.bsputility.ui.preview.PreviewData
import com.gratus.bsputility.ui.screens.AttendanceScreen
import com.gratus.bsputility.ui.screens.AttendanceScreenContent
import com.gratus.bsputility.ui.screens.MasterDataScreen
import com.gratus.bsputility.ui.screens.MasterDataScreenContent
import com.gratus.bsputility.ui.screens.ReportsScreen
import com.gratus.bsputility.ui.screens.ReportsScreenContent
import com.gratus.bsputility.ui.screens.RoosterScreen
import com.gratus.bsputility.ui.screens.RoosterScreenContent
import com.gratus.bsputility.ui.screens.VerificationScreen
import com.gratus.bsputility.ui.screens.VerificationScreenContent
import com.gratus.bsputility.ui.theme.IndustrialAmber600
import com.gratus.bsputility.ui.theme.IndustrialNavy900
import com.gratus.bsputility.ui.theme.MyApplicationTheme
import com.gratus.bsputility.ui.viewmodel.ManpowerViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDark = androidx.compose.foundation.isSystemInDarkTheme()

            LaunchedEffect(isDark) {
                enableEdgeToEdge(
                    // Top brand bar is always dark IndustrialNavy900, so status bar icons must be light (white) for maximum contrast
                    statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
                    navigationBarStyle = if (isDark) {
                        SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(
                            android.graphics.Color.TRANSPARENT,
                            android.graphics.Color.TRANSPARENT
                        )
                    }
                )
            }
            MyApplicationTheme {
                val viewModel: ManpowerViewModel = viewModel()
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

enum class NavigationTab(val route: String, val title: String, val icon: ImageVector) {
    Attendance("attendance", "Attendance", Icons.Default.FactCheck),
    Rooster("rooster", "Rooster", Icons.Default.People),
    Verification("verification", "Verify", Icons.Default.VerifiedUser),
    Reports("reports", "Reports", Icons.Default.Assessment),
    Settings("settings", "Settings", Icons.Default.Settings);
}

@Composable
fun MainAppScreen(
    viewModel: ManpowerViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(NavigationTab.Attendance) }
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()

    MainAppContent(
        selectedTab = selectedTab,
        onTabSelected = { selectedTab = it },
        selectedDate = selectedDate,
        onPreviousDay = { viewModel.selectPreviousDay() },
        onNextDay = { viewModel.selectNextDay() },
        onDateSelected = { viewModel.selectDate(it) },
        modifier = modifier
    ) {
        when (selectedTab) {
            NavigationTab.Attendance -> AttendanceScreen(viewModel = viewModel)
            NavigationTab.Rooster -> RoosterScreen(viewModel = viewModel)
            NavigationTab.Verification -> VerificationScreen(viewModel = viewModel)
            NavigationTab.Reports -> ReportsScreen(viewModel = viewModel)
            NavigationTab.Settings -> MasterDataScreen(viewModel = viewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(
    selectedTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit,
    selectedDate: String,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Top Brand Bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = IndustrialNavy900,
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(IndustrialAmber600),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Factory,
                                    contentDescription = null,
                                    tint = IndustrialNavy900,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "BSP Metatech LLP",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "Chakan • Contract Labour & Staff Attendance",
                                    fontSize = 11.sp,
                                    color = IndustrialAmber600,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Date Navigation Bar (visible on Attendance, Verification, and Reports)
                if (selectedTab != NavigationTab.Settings && selectedTab != NavigationTab.Rooster) {
                    DateNavigationBar(
                        selectedDate = selectedDate,
                        onPreviousDay = onPreviousDay,
                        onNextDay = onNextDay,
                        onDateSelected = onDateSelected
                    )
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                windowInsets = NavigationBarDefaults.windowInsets,
                tonalElevation = 6.dp,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                NavigationTab.entries.forEach { tab ->
                    val isSelected = selectedTab == tab
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        selected = isSelected,
                        onClick = { onTabSelected(tab) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.secondary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_tab_${tab.route}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            content()
        }
    }
}

// ==========================================
// PREVIEWS - ALL APP STATES / TABS
// ==========================================

@Preview(name = "Main App - Attendance Tab", showBackground = true)
@Composable
fun MainAppScreenPreview_Attendance() {
    MyApplicationTheme(darkTheme = false) {
        MainAppContent(
            selectedTab = NavigationTab.Attendance,
            onTabSelected = {},
            selectedDate = "2026-09-10",
            onPreviousDay = {},
            onNextDay = {},
            onDateSelected = {}
        ) {
            AttendanceScreenContent(
                items = PreviewData.sampleAttendanceItems,
                summary = PreviewData.sampleSummary,
                contractors = PreviewData.sampleContractors,
                departments = listOf("Laser Cutting", "Fabrication", "Welding Shop"),
                roles = listOf("Helper", "Welder", "Operator"),
                units = listOf("Unit I", "Unit II"),
                searchQuery = "",
                onSearchQueryChange = {},
                filterType = "All",
                onFilterTypeChange = {},
                filterContractor = null,
                onFilterContractorChange = {},
                filterDepartment = null,
                onFilterDepartmentChange = {},
                onMarkAllPresent = {},
                onTogglePresence = {},
                onSaveStaffAttendance = { _, _, _, _ -> },
                onSaveLabourAssignment = { _, _, _, _, _, _, _, _ -> }
            )
        }
    }
}

@Preview(name = "Main App - Rooster Tab", showBackground = true)
@Composable
fun MainAppScreenPreview_Rooster() {
    MyApplicationTheme {
        MainAppContent(
            selectedTab = NavigationTab.Rooster,
            onTabSelected = {},
            selectedDate = "2026-09-10",
            onPreviousDay = {},
            onNextDay = {},
            onDateSelected = {}
        ) {
            RoosterScreenContent(
                allEmployees = PreviewData.sampleEmployees,
                contractors = PreviewData.sampleContractors,
                departments = listOf("Laser Cutting", "Fabrication", "Welding Shop"),
                roles = listOf("Helper", "Welder", "Operator"),
                designations = listOf("Production Supervisor", "Shift In-charge"),
                units = listOf("Unit I", "Unit II"),
                searchQuery = "",
                onSearchQueryChange = {},
                filterStatus = "All",
                onFilterStatusChange = {},
                collapsedGroups = emptyMap(),
                onToggleCollapse = {},
                onAddEmployee = {},
                onUpdateEmployee = {},
                onDeleteEmployee = {},
                onImportPastedNames = { _, _, _, _, _, _ -> 0 },
                onExportJson = { "{}" },
                onExportCsv = { "" },
                onImportJson = { true }
            )
        }
    }
}

@Preview(name = "Main App - Verification Tab", showBackground = true)
@Composable
fun MainAppScreenPreview_Verification() {
    val presentLabourers = PreviewData.sampleAttendanceItems.filter { it.isPresent && it.employee.type != EmployeeTypes.STAFF }
    val deptMap = presentLabourers.groupBy { it.effectiveDepartment }
    val verifMap = PreviewData.sampleVerifications.associateBy { it.departmentName }
    val depts = listOf("Welding Shop", "Laser Cutting", "Fabrication", "Press Shop")
    val staffList = PreviewData.sampleEmployees.filter { it.type == EmployeeTypes.STAFF }

    MyApplicationTheme {
        MainAppContent(
            selectedTab = NavigationTab.Verification,
            onTabSelected = {},
            selectedDate = "2026-09-10",
            onPreviousDay = {},
            onNextDay = {},
            onDateSelected = {}
        ) {
            VerificationScreenContent(
                allDepts = depts,
                deptMap = deptMap,
                verifMap = verifMap,
                staffList = staffList,
                verifiedCount = 2,
                onVerify = { _, _, _, _, _ -> }
            )
        }
    }
}

@Preview(name = "Main App - Reports Tab", showBackground = true)
@Composable
fun MainAppScreenPreview_Reports() {
    MyApplicationTheme {
        MainAppContent(
            selectedTab = NavigationTab.Reports,
            onTabSelected = {},
            selectedDate = "2026-09-10",
            onPreviousDay = {},
            onNextDay = {},
            onDateSelected = {}
        ) {
            ReportsScreenContent(
                summary = PreviewData.sampleSummary,
                date = "2026-09-10",
                verifications = PreviewData.sampleVerifications,
                onCopyWhatsAppReport = {},
                onShareWhatsAppReport = {},
                onCopySummaryCsv = {},
                onCopyDetailedCsv = {}
            )
        }
    }
}

@Preview(name = "Main App - Settings Tab", showBackground = true)
@Composable
fun MainAppScreenPreview_Settings() {
    MyApplicationTheme {
        MainAppContent(
            selectedTab = NavigationTab.Settings,
            onTabSelected = {},
            selectedDate = "2026-09-10",
            onPreviousDay = {},
            onNextDay = {},
            onDateSelected = {}
        ) {
            MasterDataScreenContent(
                contractors = PreviewData.sampleContractors,
                allConfigs = PreviewData.sampleConfigItems,
                allEmployees = PreviewData.sampleEmployees,
                onAddContractor = {},
                onUpdateContractor = {},
                onDeleteContractor = {},
                onAddConfigItem = { _, _, _ -> },
                onDeleteConfigItem = {},
                initialTab = "Contractors"
            )
        }
    }
}

@Preview(name = "Main App - Dark Theme", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MainAppScreenPreview_DarkTheme() {
    MyApplicationTheme(darkTheme = true) {
        MainAppContent(
            selectedTab = NavigationTab.Attendance,
            onTabSelected = {},
            selectedDate = "2026-09-10",
            onPreviousDay = {},
            onNextDay = {},
            onDateSelected = {}
        ) {
            AttendanceScreenContent(
                items = PreviewData.sampleAttendanceItems,
                summary = PreviewData.sampleSummary,
                contractors = PreviewData.sampleContractors,
                departments = listOf("Laser Cutting", "Fabrication", "Welding Shop"),
                roles = listOf("Helper", "Welder", "Operator"),
                units = listOf("Unit I", "Unit II"),
                searchQuery = "",
                onSearchQueryChange = {},
                filterType = "All",
                onFilterTypeChange = {},
                filterContractor = null,
                onFilterContractorChange = {},
                filterDepartment = null,
                onFilterDepartmentChange = {},
                onMarkAllPresent = {},
                onTogglePresence = {},
                onSaveStaffAttendance = { _, _, _, _ -> },
                onSaveLabourAssignment = { _, _, _, _, _, _, _, _ -> }
            )
        }
    }
}
