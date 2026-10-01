package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.WorkTrackRepository
import com.example.model.CategoryItem
import com.example.model.CustomFieldDef
import com.example.model.ProjectItem
import com.example.model.UserSettings
import com.example.util.CsvBackupHelper
import com.example.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

enum class SortOption(val label: String) {
    DATE_DESC("Date (Newest First)"),
    DATE_ASC("Date (Oldest First)"),
    MINUTES_DESC("Video Minutes (High to Low)"),
    MINUTES_ASC("Video Minutes (Low to High)"),
    PAYMENT_DESC("Payment (High to Low)"),
    PAYMENT_ASC("Payment (Low to High)")
}

data class CategorySummary(
    val category: String,
    val count: Int,
    val totalMinutes: Double,
    val totalEarnings: Double,
    val colorHex: String
)

data class ClientSummary(
    val client: String,
    val count: Int,
    val totalMinutes: Double,
    val totalEarnings: Double
)

data class MonthlyTotals(
    val totalMinutes: Double = 0.0,
    val totalProjects: Int = 0,
    val totalHours: Double = 0.0,
    val totalEarnings: Double = 0.0,
    val avgMinutesPerProject: Double = 0.0,
    val avgPaymentPerProject: Double = 0.0
)

data class MonthlyReportResult(
    val projects: List<ProjectItem> = emptyList(),
    val categoryBreakdown: List<CategorySummary> = emptyList(),
    val clientBreakdown: List<ClientSummary> = emptyList(),
    val totals: MonthlyTotals = MonthlyTotals()
)

data class DashboardMetrics(
    val totalMinutes: Double = 0.0,
    val totalProjects: Int = 0,
    val totalHours: Double = 0.0,
    val totalEarnings: Double = 0.0,
    val thisMonthMinutes: Double = 0.0,
    val thisMonthProjects: Int = 0,
    val thisMonthEarnings: Double = 0.0,
    val avgPaymentPerProject: Double = 0.0,
    val avgMinutesPerProject: Double = 0.0,
    val categoryBreakdown: List<CategorySummary> = emptyList(),
    val clientBreakdown: List<ClientSummary> = emptyList(),
    val recentProjects: List<ProjectItem> = emptyList()
)

class WorkTrackViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: WorkTrackRepository
    val allProjects: StateFlow<List<ProjectItem>>
    val allCategories: StateFlow<List<CategoryItem>>
    val userSettings: StateFlow<UserSettings>

    // Dynamic list of unique clients from projects
    val allClients: StateFlow<List<String>>

    // Filter & Search states for Work History
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow<String?>(null)
    val selectedCategoryFilter = _selectedCategoryFilter.asStateFlow()

    private val _selectedClientFilter = MutableStateFlow<String?>(null)
    val selectedClientFilter = _selectedClientFilter.asStateFlow()

    private val _selectedStatusFilter = MutableStateFlow<String?>(null)
    val selectedStatusFilter = _selectedStatusFilter.asStateFlow()

    private val _selectedMonthFilter = MutableStateFlow<String?>(null)
    val selectedMonthFilter = _selectedMonthFilter.asStateFlow()

    private val _sortOption = MutableStateFlow(SortOption.DATE_DESC)
    val sortOption = _sortOption.asStateFlow()

    // Monthly Report state
    private val _selectedReportMonth = MutableStateFlow(DateUtils.currentYearMonth())
    val selectedReportMonth = _selectedReportMonth.asStateFlow()

    // Custom Fields configuration state
    private val _customFields = MutableStateFlow<List<CustomFieldDef>>(CustomFieldDef.defaultList())
    val customFields = _customFields.asStateFlow()

    // User Feedback Banner/Snackbar
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage = _userMessage.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = WorkTrackRepository(
            database.projectDao(),
            database.categoryDao(),
            database.settingsDao()
        )

        allProjects = repository.allProjects.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allCategories = repository.allCategories.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allClients = allProjects.map { list ->
            list.map { it.clientName.trim() }
                .filter { it.isNotBlank() }
                .distinct()
                .sorted()
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        userSettings = repository.userSettings.combine(MutableStateFlow(UserSettings())) { saved, default ->
            saved ?: default
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserSettings()
        )

        viewModelScope.launch {
            repository.ensureInitialData()
        }

        // Load custom fields schema from settings if present
        viewModelScope.launch {
            userSettings.collect { settings ->
                if (settings.customFieldsSchemaJson.isNotBlank() && settings.customFieldsSchemaJson != "[]") {
                    try {
                        val arr = JSONArray(settings.customFieldsSchemaJson)
                        val list = mutableListOf<CustomFieldDef>()
                        for (i in 0 until arr.length()) {
                            val obj = arr.getJSONObject(i)
                            list.add(
                                CustomFieldDef(
                                    id = obj.getString("id"),
                                    label = obj.getString("label"),
                                    isEnabled = obj.optBoolean("isEnabled", true),
                                    showInTable = obj.optBoolean("showInTable", true),
                                    showInInvoice = obj.optBoolean("showInInvoice", false)
                                )
                            )
                        }
                        if (list.isNotEmpty()) {
                            _customFields.value = list
                        }
                    } catch (_: Exception) {
                        // ignore and use default
                    }
                }
            }
        }
    }

    data class HistoryFilter(
        val query: String = "",
        val category: String? = null,
        val client: String? = null,
        val status: String? = null,
        val month: String? = null,
        val sort: SortOption = SortOption.DATE_DESC
    )

    private val filterState = combine(
        combine(_searchQuery, _selectedCategoryFilter, _selectedClientFilter) { q, cat, cl ->
            Triple(q, cat, cl)
        },
        combine(_selectedStatusFilter, _selectedMonthFilter, _sortOption) { stat, m, s ->
            Triple(stat, m, s)
        }
    ) { (q, cat, cl), (stat, m, s) ->
        HistoryFilter(q, cat, cl, stat, m, s)
    }

    // Filtered Projects for Work History
    val filteredProjects: StateFlow<List<ProjectItem>> = combine(
        allProjects,
        filterState
    ) { projects, filter ->
        var list = projects

        if (filter.query.isNotBlank()) {
            val q = filter.query.trim().lowercase()
            list = list.filter {
                it.name.lowercase().contains(q) ||
                it.category.lowercase().contains(q) ||
                it.clientName.lowercase().contains(q) ||
                it.notes.lowercase().contains(q)
            }
        }

        if (filter.category != null && filter.category.isNotBlank() && filter.category != "All") {
            list = list.filter { it.category.equals(filter.category, ignoreCase = true) }
        }

        if (filter.client != null && filter.client.isNotBlank() && filter.client != "All") {
            list = list.filter { it.clientName.equals(filter.client, ignoreCase = true) }
        }

        if (filter.status != null && filter.status.isNotBlank() && filter.status != "All") {
            list = list.filter { it.status.equals(filter.status, ignoreCase = true) }
        }

        if (filter.month != null && filter.month.isNotBlank() && filter.month != "All") {
            list = list.filter { it.date.startsWith(filter.month) }
        }

        when (filter.sort) {
            SortOption.DATE_DESC -> list.sortedWith(compareByDescending<ProjectItem> { it.date }.thenByDescending { it.id })
            SortOption.DATE_ASC -> list.sortedWith(compareBy<ProjectItem> { it.date }.thenBy { it.id })
            SortOption.MINUTES_DESC -> list.sortedByDescending { it.minutes }
            SortOption.MINUTES_ASC -> list.sortedBy { it.minutes }
            SortOption.PAYMENT_DESC -> list.sortedByDescending { it.payment }
            SortOption.PAYMENT_ASC -> list.sortedBy { it.payment }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Dashboard Metrics - Video Minutes is PRIMARY
    val dashboardMetrics: StateFlow<DashboardMetrics> = combine(
        allProjects,
        allCategories
    ) { projects, categories ->
        val currentMonthPrefix = DateUtils.currentYearMonth()
        val completedProjects = projects.filter { it.status == ProjectItem.STATUS_COMPLETED }
        val thisMonthCompleted = completedProjects.filter { it.date.startsWith(currentMonthPrefix) }

        val totalMinutes = completedProjects.sumOf { it.minutes }
        val totalProjects = completedProjects.size
        val totalHours = completedProjects.sumOf { it.workingHours }
        val totalEarnings = completedProjects.sumOf { it.payment }

        val thisMonthMinutes = thisMonthCompleted.sumOf { it.minutes }
        val thisMonthProjects = thisMonthCompleted.size
        val thisMonthEarnings = thisMonthCompleted.sumOf { it.payment }

        val avgMinutes = if (totalProjects > 0) totalMinutes / totalProjects else 0.0
        val avgPayment = if (totalProjects > 0) totalEarnings / totalProjects else 0.0

        val categoryColorMap = categories.associate { it.name to it.colorHex }

        val catBreakdown = completedProjects.groupBy { it.category }.map { (cat, list) ->
            CategorySummary(
                category = cat,
                count = list.size,
                totalMinutes = list.sumOf { it.minutes },
                totalEarnings = list.sumOf { it.payment },
                colorHex = categoryColorMap[cat] ?: "#6366F1"
            )
        }.sortedByDescending { it.totalMinutes }

        val clBreakdown = completedProjects
            .filter { it.clientName.isNotBlank() }
            .groupBy { it.clientName.trim() }
            .map { (client, list) ->
                ClientSummary(
                    client = client,
                    count = list.size,
                    totalMinutes = list.sumOf { it.minutes },
                    totalEarnings = list.sumOf { it.payment }
                )
            }.sortedByDescending { it.totalMinutes }

        val recent = projects.take(5)

        DashboardMetrics(
            totalMinutes = totalMinutes,
            totalProjects = totalProjects,
            totalHours = totalHours,
            totalEarnings = totalEarnings,
            thisMonthMinutes = thisMonthMinutes,
            thisMonthProjects = thisMonthProjects,
            thisMonthEarnings = thisMonthEarnings,
            avgPaymentPerProject = avgPayment,
            avgMinutesPerProject = avgMinutes,
            categoryBreakdown = catBreakdown,
            clientBreakdown = clBreakdown,
            recentProjects = recent
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardMetrics()
    )

    // Monthly Report Data
    val monthlyReportData: StateFlow<MonthlyReportResult> = combine(
        allProjects,
        _selectedReportMonth,
        allCategories
    ) { projects, month, categories ->
        val monthProjects = projects.filter { it.date.startsWith(month) }
        val categoryColorMap = categories.associate { it.name to it.colorHex }

        val catBreakdown = monthProjects.groupBy { it.category }.map { (cat, list) ->
            CategorySummary(
                category = cat,
                count = list.size,
                totalMinutes = list.sumOf { it.minutes },
                totalEarnings = list.sumOf { it.payment },
                colorHex = categoryColorMap[cat] ?: "#6366F1"
            )
        }.sortedByDescending { it.totalMinutes }

        val clBreakdown = monthProjects
            .filter { it.clientName.isNotBlank() }
            .groupBy { it.clientName.trim() }
            .map { (client, list) ->
                ClientSummary(
                    client = client,
                    count = list.size,
                    totalMinutes = list.sumOf { it.minutes },
                    totalEarnings = list.sumOf { it.payment }
                )
            }.sortedByDescending { it.totalMinutes }

        val totalCount = monthProjects.size
        val totalMinutes = monthProjects.sumOf { it.minutes }
        val totalHours = monthProjects.sumOf { it.workingHours }
        val totalEarnings = monthProjects.sumOf { it.payment }

        val avgMins = if (totalCount > 0) totalMinutes / totalCount else 0.0
        val avgPay = if (totalCount > 0) totalEarnings / totalCount else 0.0

        val totals = MonthlyTotals(
            totalMinutes = totalMinutes,
            totalProjects = totalCount,
            totalHours = totalHours,
            totalEarnings = totalEarnings,
            avgMinutesPerProject = avgMins,
            avgPaymentPerProject = avgPay
        )

        MonthlyReportResult(
            projects = monthProjects,
            categoryBreakdown = catBreakdown,
            clientBreakdown = clBreakdown,
            totals = totals
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MonthlyReportResult()
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategoryFilter(category: String?) {
        _selectedCategoryFilter.value = category
    }

    fun setClientFilter(client: String?) {
        _selectedClientFilter.value = client
    }

    fun setStatusFilter(status: String?) {
        _selectedStatusFilter.value = status
    }

    fun setMonthFilter(month: String?) {
        _selectedMonthFilter.value = month
    }

    fun setSortOption(option: SortOption) {
        _sortOption.value = option
    }

    fun setSelectedReportMonth(yearMonth: String) {
        _selectedReportMonth.value = yearMonth
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun showMessage(msg: String) {
        _userMessage.value = msg
    }

    fun saveProject(project: ProjectItem) {
        viewModelScope.launch {
            // If project has a category that isn't yet in allCategories, automatically register it!
            if (project.category.isNotBlank()) {
                val exists = allCategories.value.any { it.name.equals(project.category.trim(), ignoreCase = true) }
                if (!exists) {
                    repository.insertCategory(CategoryItem(name = project.category.trim()))
                }
            }

            if (project.id == 0L) {
                repository.insertProject(project)
                showMessage("Saved '${project.name}' (${project.minutes} min)")
            } else {
                repository.updateProject(project)
                showMessage("Updated '${project.name}'")
            }
        }
    }

    fun duplicateProject(project: ProjectItem) {
        viewModelScope.launch {
            val duplicate = project.copy(
                id = 0L,
                name = "${project.name} (Copy)",
                date = DateUtils.todayIso(),
                createdAt = System.currentTimeMillis()
            )
            repository.insertProject(duplicate)
            showMessage("Duplicated '${project.name}'")
        }
    }

    fun deleteProject(project: ProjectItem) {
        viewModelScope.launch {
            repository.deleteProject(project)
            showMessage("Deleted '${project.name}'")
        }
    }

    fun deleteProjectById(id: Long) {
        viewModelScope.launch {
            repository.deleteProjectById(id)
            showMessage("Project deleted")
        }
    }

    fun addBulkProjects(projects: List<ProjectItem>) {
        viewModelScope.launch {
            val valid = projects.filter { it.name.isNotBlank() && it.minutes > 0 }
            if (valid.isNotEmpty()) {
                // Ensure all categories from bulk entry are registered
                for (p in valid) {
                    if (p.category.isNotBlank()) {
                        val exists = allCategories.value.any { it.name.equals(p.category.trim(), ignoreCase = true) }
                        if (!exists) {
                            repository.insertCategory(CategoryItem(name = p.category.trim()))
                        }
                    }
                }
                repository.insertProjects(valid)
                val totalMins = valid.sumOf { it.minutes }
                showMessage("Added ${valid.size} projects ($totalMins min)!")
            } else {
                showMessage("No valid projects to add")
            }
        }
    }

    fun addCategory(name: String, colorHex: String = "#6366F1") {
        viewModelScope.launch {
            if (name.isNotBlank()) {
                repository.insertCategory(
                    CategoryItem(name = name.trim(), colorHex = colorHex)
                )
                showMessage("Category '$name' created")
            }
        }
    }

    fun renameCategory(category: CategoryItem, newName: String) {
        viewModelScope.launch {
            if (newName.isNotBlank()) {
                repository.renameCategory(category, newName)
                showMessage("Renamed to '$newName'")
            }
        }
    }

    fun deleteCategory(category: CategoryItem) {
        viewModelScope.launch {
            repository.deleteCategory(category)
            showMessage("Category '${category.name}' removed (historical records preserved)")
        }
    }

    fun updateSettings(settings: UserSettings) {
        viewModelScope.launch {
            repository.updateSettings(settings)
            showMessage("Settings updated")
        }
    }

    fun updateCustomFields(fields: List<CustomFieldDef>) {
        _customFields.value = fields
        viewModelScope.launch {
            val arr = JSONArray()
            for (f in fields) {
                val obj = JSONObject()
                obj.put("id", f.id)
                obj.put("label", f.label)
                obj.put("isEnabled", f.isEnabled)
                obj.put("showInTable", f.showInTable)
                obj.put("showInInvoice", f.showInInvoice)
                arr.put(obj)
            }
            val current = userSettings.value
            repository.updateSettings(current.copy(customFieldsSchemaJson = arr.toString()))
        }
    }

    fun importCsv(csvContent: String): Pair<Boolean, String> {
        return try {
            val defaultRate = userSettings.value.defaultRatePerMinute
            val projects = CsvBackupHelper.parseProjectsFromCsv(csvContent, defaultRate)
            if (projects.isEmpty()) {
                Pair(false, "No valid project rows found in CSV")
            } else {
                viewModelScope.launch {
                    repository.insertProjects(projects)
                    showMessage("Imported ${projects.size} projects from CSV!")
                }
                Pair(true, "Imported ${projects.size} projects successfully!")
            }
        } catch (e: Exception) {
            Pair(false, "Failed to import CSV: ${e.message}")
        }
    }

    fun restoreBackupJson(jsonContent: String): Pair<Boolean, String> {
        return try {
            val (projects, categories, settings) = CsvBackupHelper.parseBackupJson(jsonContent)
            viewModelScope.launch {
                if (categories.isNotEmpty()) {
                    for (c in categories) repository.insertCategory(c)
                }
                if (settings != null) {
                    repository.updateSettings(settings)
                }
                if (projects.isNotEmpty()) {
                    repository.insertProjects(projects)
                }
                showMessage("Restored ${projects.size} projects and configuration!")
            }
            Pair(true, "Successfully restored backup!")
        } catch (e: Exception) {
            Pair(false, "Failed to restore backup: ${e.message}")
        }
    }

    fun resetToSampleData() {
        viewModelScope.launch {
            repository.deleteAllProjects()
            repository.seedSampleProjects()
            showMessage("Loaded example projects!")
        }
    }
}
