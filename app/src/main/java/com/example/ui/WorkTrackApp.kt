package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.ProjectItem
import com.example.ui.screens.AddEditProjectScreen
import com.example.ui.screens.BulkEntryScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.InvoiceScreen
import com.example.ui.screens.MonthlyReportScreen
import com.example.ui.screens.QuickAddDialog
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.WorkHistoryScreen
import com.example.ui.theme.rememberPressScaleModifier
import com.example.util.DateUtils

enum class AppScreen(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    DASHBOARD("Dashboard", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
    ADD_PROJECT("Add", Icons.Filled.AddCircle, Icons.Outlined.AddCircleOutline),
    WORK_HISTORY("History", Icons.Filled.History, Icons.Outlined.History),
    REPORTS("Reports", Icons.Filled.BarChart, Icons.Outlined.BarChart),
    INVOICE("Invoice", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong),
    SETTINGS("Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

@Composable
fun WorkTrackApp(
    viewModel: WorkTrackViewModel,
    modifier: Modifier = Modifier
) {
    var currentScreen by remember { mutableStateOf(AppScreen.DASHBOARD) }
    var editingProject by remember { mutableStateOf<ProjectItem?>(null) }
    var isBulkEntryOpen by remember { mutableStateOf(false) }
    var isQuickAddOpen by remember { mutableStateOf(false) }
    var invoiceTargetMonth by remember { mutableStateOf(DateUtils.currentYearMonth()) }

    val allProjects by viewModel.allProjects.collectAsStateWithLifecycle()
    val allCategories by viewModel.allCategories.collectAsStateWithLifecycle()
    val allClients by viewModel.allClients.collectAsStateWithLifecycle()
    val userSettings by viewModel.userSettings.collectAsStateWithLifecycle()
    val customFields by viewModel.customFields.collectAsStateWithLifecycle()
    val dashboardMetrics by viewModel.dashboardMetrics.collectAsStateWithLifecycle()
    val filteredProjects by viewModel.filteredProjects.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategoryFilter by viewModel.selectedCategoryFilter.collectAsStateWithLifecycle()
    val selectedClientFilter by viewModel.selectedClientFilter.collectAsStateWithLifecycle()
    val selectedStatusFilter by viewModel.selectedStatusFilter.collectAsStateWithLifecycle()
    val selectedMonthFilter by viewModel.selectedMonthFilter.collectAsStateWithLifecycle()
    val sortOption by viewModel.sortOption.collectAsStateWithLifecycle()

    val selectedReportMonth by viewModel.selectedReportMonth.collectAsStateWithLifecycle()
    val monthlyReportResult by viewModel.monthlyReportData.collectAsStateWithLifecycle()

    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    // Android back navigation handler
    BackHandler(enabled = currentScreen != AppScreen.DASHBOARD || isBulkEntryOpen || editingProject != null) {
        if (isBulkEntryOpen) {
            isBulkEntryOpen = false
        } else if (editingProject != null) {
            editingProject = null
        } else if (currentScreen != AppScreen.DASHBOARD) {
            currentScreen = AppScreen.DASHBOARD
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (!isBulkEntryOpen && editingProject == null) {
                NavigationBar(
                    modifier = Modifier
                        .testTag("main_navigation_bar")
                        .windowInsetsPadding(WindowInsets.navigationBars),
                    tonalElevation = 8.dp
                ) {
                    for (screen in AppScreen.values()) {
                        val isSelected = currentScreen == screen
                        val iconScale by animateFloatAsState(
                            targetValue = if (isSelected) 1.15f else 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            ),
                            label = "nav_icon_scale"
                        )
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                editingProject = null
                                currentScreen = screen
                            },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                    contentDescription = screen.title,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .scale(iconScale)
                                )
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            modifier = Modifier.testTag("nav_item_${screen.name.lowercase()}")
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (!isBulkEntryOpen && editingProject == null && (currentScreen == AppScreen.DASHBOARD || currentScreen == AppScreen.WORK_HISTORY)) {
                val fabInteractionSource = remember { MutableInteractionSource() }
                val fabPressModifier = rememberPressScaleModifier(fabInteractionSource)

                FloatingActionButton(
                    onClick = { isQuickAddOpen = true },
                    interactionSource = fabInteractionSource,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .then(fabPressModifier)
                        .testTag("fab_quick_add")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Quick Add Project")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isBulkEntryOpen) {
                BulkEntryScreen(
                    categories = allCategories,
                    settings = userSettings,
                    onSaveAll = { list ->
                        viewModel.addBulkProjects(list)
                        isBulkEntryOpen = false
                        currentScreen = AppScreen.WORK_HISTORY
                    },
                    onNavigateBack = { isBulkEntryOpen = false }
                )
            } else if (editingProject != null) {
                AddEditProjectScreen(
                    editingProject = editingProject,
                    categories = allCategories,
                    settings = userSettings,
                    customFieldDefs = customFields,
                    onSave = { project ->
                        viewModel.saveProject(project)
                        editingProject = null
                    },
                    onAddCategory = { name, color ->
                        viewModel.addCategory(name, color)
                    },
                    onNavigateBack = { editingProject = null }
                )
            } else {
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = {
                        if (targetState == AppScreen.ADD_PROJECT) {
                            (fadeIn(animationSpec = tween(240, easing = FastOutSlowInEasing)) +
                             scaleIn(initialScale = 0.95f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)) +
                             slideInVertically(initialOffsetY = { 48 }, animationSpec = tween(260, easing = FastOutSlowInEasing)))
                                .togetherWith(
                                    fadeOut(animationSpec = tween(180, easing = FastOutSlowInEasing)) +
                                    scaleOut(targetScale = 0.98f, animationSpec = tween(180))
                                )
                        } else if (initialState == AppScreen.ADD_PROJECT) {
                            (fadeIn(animationSpec = tween(220, easing = FastOutSlowInEasing)))
                                .togetherWith(
                                    fadeOut(animationSpec = tween(180, easing = FastOutSlowInEasing)) +
                                    slideOutVertically(targetOffsetY = { 40 }, animationSpec = tween(180, easing = FastOutSlowInEasing)) +
                                    scaleOut(targetScale = 0.96f, animationSpec = tween(180))
                                )
                        } else {
                            (fadeIn(animationSpec = tween(200, easing = FastOutSlowInEasing)) +
                             scaleIn(initialScale = 0.98f, animationSpec = tween(200, easing = FastOutSlowInEasing)))
                                .togetherWith(
                                    fadeOut(animationSpec = tween(150, easing = FastOutSlowInEasing))
                                )
                        }
                    },
                    label = "screen_transition"
                ) { screen ->
                    when (screen) {
                        AppScreen.DASHBOARD -> {
                            DashboardScreen(
                                metrics = dashboardMetrics,
                                settings = userSettings,
                                onNavigateToAdd = { currentScreen = AppScreen.ADD_PROJECT },
                                onNavigateToBulk = { isBulkEntryOpen = true },
                                onNavigateToInvoice = { currentScreen = AppScreen.INVOICE },
                                onNavigateToHistory = { currentScreen = AppScreen.WORK_HISTORY },
                                onEditProject = { project -> editingProject = project },
                                onDuplicateProject = { project -> viewModel.duplicateProject(project) },
                                onOpenQuickAdd = { isQuickAddOpen = true }
                            )
                        }

                        AppScreen.ADD_PROJECT -> {
                            AddEditProjectScreen(
                                editingProject = null,
                                categories = allCategories,
                                settings = userSettings,
                                customFieldDefs = customFields,
                                onSave = { project ->
                                    viewModel.saveProject(project)
                                    currentScreen = AppScreen.WORK_HISTORY
                                },
                                onAddCategory = { name, color ->
                                    viewModel.addCategory(name, color)
                                },
                                onNavigateBack = { currentScreen = AppScreen.DASHBOARD }
                            )
                        }

                        AppScreen.WORK_HISTORY -> {
                            WorkHistoryScreen(
                                projects = filteredProjects,
                                categories = allCategories,
                                clients = allClients,
                                settings = userSettings,
                                customFields = customFields,
                                searchQuery = searchQuery,
                                selectedCategory = selectedCategoryFilter,
                                selectedClient = selectedClientFilter,
                                selectedStatus = selectedStatusFilter,
                                selectedMonth = selectedMonthFilter,
                                sortOption = sortOption,
                                onSearchChange = { viewModel.setSearchQuery(it) },
                                onCategoryChange = { viewModel.setCategoryFilter(it) },
                                onClientChange = { viewModel.setClientFilter(it) },
                                onStatusChange = { viewModel.setStatusFilter(it) },
                                onMonthChange = { viewModel.setMonthFilter(it) },
                                onSortChange = { viewModel.setSortOption(it) },
                                onEditProject = { project -> editingProject = project },
                                onDuplicateProject = { project -> viewModel.duplicateProject(project) },
                                onDeleteProject = { project -> viewModel.deleteProject(project) }
                            )
                        }

                        AppScreen.REPORTS -> {
                            MonthlyReportScreen(
                                selectedMonth = selectedReportMonth,
                                reportResult = monthlyReportResult,
                                settings = userSettings,
                                onSelectMonth = { viewModel.setSelectedReportMonth(it) },
                                onGenerateInvoice = { month ->
                                    invoiceTargetMonth = month
                                    currentScreen = AppScreen.INVOICE
                                },
                                onEditProject = { project -> editingProject = project }
                            )
                        }

                        AppScreen.INVOICE -> {
                            InvoiceScreen(
                                allProjects = allProjects,
                                clients = allClients,
                                settings = userSettings,
                                initialMonth = invoiceTargetMonth,
                                onUpdateSettings = { viewModel.updateSettings(it) }
                            )
                        }

                        AppScreen.SETTINGS -> {
                            SettingsScreen(
                                settings = userSettings,
                                categories = allCategories,
                                customFields = customFields,
                                allProjects = allProjects,
                                onUpdateSettings = { viewModel.updateSettings(it) },
                                onAddCategory = { name, color -> viewModel.addCategory(name, color) },
                                onRenameCategory = { cat, newName -> viewModel.renameCategory(cat, newName) },
                                onDeleteCategory = { cat -> viewModel.deleteCategory(cat) },
                                onUpdateCustomFields = { fields -> viewModel.updateCustomFields(fields) },
                                onImportCsv = { csv -> viewModel.importCsv(csv) },
                                onRestoreJson = { json -> viewModel.restoreBackupJson(json) },
                                onResetSampleData = { viewModel.resetToSampleData() }
                            )
                        }
                    }
                }
            }
        }
    }

    if (isQuickAddOpen) {
        QuickAddDialog(
            categories = allCategories,
            settings = userSettings,
            onSave = { project ->
                viewModel.saveProject(project)
            },
            onDismiss = { isQuickAddOpen = false }
        )
    }
}
