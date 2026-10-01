package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CategoryItem
import com.example.model.CustomFieldDef
import com.example.model.ProjectItem
import com.example.model.UserSettings
import com.example.ui.SortOption
import com.example.ui.components.DeleteConfirmationDialog
import com.example.ui.components.StatusBadge
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentRose
import com.example.util.DateUtils
import org.json.JSONObject
import java.util.Locale

@Composable
fun WorkHistoryScreen(
    projects: List<ProjectItem>,
    categories: List<CategoryItem>,
    clients: List<String>,
    settings: UserSettings,
    customFields: List<CustomFieldDef>,
    searchQuery: String,
    selectedCategory: String?,
    selectedClient: String?,
    selectedStatus: String?,
    selectedMonth: String?,
    sortOption: SortOption,
    onSearchChange: (String) -> Unit,
    onCategoryChange: (String?) -> Unit,
    onClientChange: (String?) -> Unit,
    onStatusChange: (String?) -> Unit,
    onMonthChange: (String?) -> Unit,
    onSortChange: (SortOption) -> Unit,
    onEditProject: (ProjectItem) -> Unit,
    onDuplicateProject: (ProjectItem) -> Unit,
    onDeleteProject: (ProjectItem) -> Unit
) {
    val currency = settings.currencySymbol
    var projectToDelete by remember { mutableStateOf<ProjectItem?>(null) }
    var sortMenuExpanded by remember { mutableStateOf(false) }
    var isSpreadsheetView by remember { mutableStateOf(false) }

    // Aggregate stats for filtered list
    val totalFilteredProjects = projects.size
    val totalFilteredMinutes = projects.sumOf { it.minutes }
    val totalFilteredEarnings = projects.sumOf { it.payment }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("work_history_screen")
    ) {
        // Search & View Mode Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search projects, category, client...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("history_search_input")
            )

            // Sort Dropdown Button
            Box {
                IconButton(
                    onClick = { sortMenuExpanded = true },
                    modifier = Modifier.testTag("history_sort_btn")
                ) {
                    Icon(imageVector = Icons.Default.Sort, contentDescription = "Sort")
                }

                DropdownMenu(
                    expanded = sortMenuExpanded,
                    onDismissRequest = { sortMenuExpanded = false }
                ) {
                    for (opt in SortOption.values()) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = opt.label,
                                    fontWeight = if (opt == sortOption) FontWeight.Bold else FontWeight.Normal,
                                    color = if (opt == sortOption) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            onClick = {
                                onSortChange(opt)
                                sortMenuExpanded = false
                            }
                        )
                    }
                }
            }

            // View toggle (Cards vs Compact Spreadsheet)
            IconButton(
                onClick = { isSpreadsheetView = !isSpreadsheetView },
                modifier = Modifier.testTag("history_toggle_view_btn")
            ) {
                Icon(
                    imageVector = if (isSpreadsheetView) Icons.Default.ViewAgenda else Icons.Default.TableChart,
                    contentDescription = "Toggle View",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Horizontal Category Filters
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategoryChange(null) },
                label = { Text("All Categories") },
                modifier = Modifier.testTag("filter_cat_all")
            )
            for (cat in categories) {
                FilterChip(
                    selected = selectedCategory.equals(cat.name, ignoreCase = true),
                    onClick = { onCategoryChange(if (selectedCategory.equals(cat.name, ignoreCase = true)) null else cat.name) },
                    label = { Text(cat.name) },
                    modifier = Modifier.testTag("filter_cat_${cat.name.lowercase()}")
                )
            }
        }

        // Horizontal Client & Status Filters
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Clients filter if any client exists
            if (clients.isNotEmpty()) {
                FilterChip(
                    selected = selectedClient == null,
                    onClick = { onClientChange(null) },
                    label = { Text("All Clients") }
                )
                for (cl in clients) {
                    FilterChip(
                        selected = selectedClient.equals(cl, ignoreCase = true),
                        onClick = { onClientChange(if (selectedClient.equals(cl, ignoreCase = true)) null else cl) },
                        label = { Text(cl) }
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
            }

            // Status filter chips
            FilterChip(
                selected = selectedStatus == ProjectItem.STATUS_COMPLETED,
                onClick = { onStatusChange(if (selectedStatus == ProjectItem.STATUS_COMPLETED) null else ProjectItem.STATUS_COMPLETED) },
                label = { Text("Completed") }
            )
            FilterChip(
                selected = selectedStatus == ProjectItem.STATUS_PENDING,
                onClick = { onStatusChange(if (selectedStatus == ProjectItem.STATUS_PENDING) null else ProjectItem.STATUS_PENDING) },
                label = { Text("Pending") }
            )
            FilterChip(
                selected = selectedStatus == ProjectItem.STATUS_CANCELLED,
                onClick = { onStatusChange(if (selectedStatus == ProjectItem.STATUS_CANCELLED) null else ProjectItem.STATUS_CANCELLED) },
                label = { Text("Cancelled") }
            )
        }

        // Filter Summary Banner (Highlighting Video Minutes)
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.VideoLibrary,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${if (totalFilteredMinutes % 1.0 == 0.0) totalFilteredMinutes.toInt() else totalFilteredMinutes} min total  •  $totalFilteredProjects projects",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = "$currency${String.format(Locale.US, "%,.0f", totalFilteredEarnings)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = AccentEmerald
                )
            }
        }

        if (projects.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No projects found",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (searchQuery.isNotBlank() || selectedCategory != null || selectedClient != null) "Try clearing filters or search terms" else "Add your completed video projects to see them here",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            if (isSpreadsheetView) {
                // Spreadsheet Table Layout with Horizontal Scroll
                SpreadsheetView(
                    projects = projects,
                    currency = currency,
                    onEdit = onEditProject,
                    onDuplicate = onDuplicateProject,
                    onDelete = { projectToDelete = it }
                )
            } else {
                // Responsive Expandable Mobile Card List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(projects, key = { it.id }) { project ->
                        ProjectHistoryCard(
                            project = project,
                            currency = currency,
                            customFieldDefs = customFields,
                            onEdit = { onEditProject(project) },
                            onDuplicate = { onDuplicateProject(project) },
                            onDelete = { projectToDelete = project }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }

    if (projectToDelete != null) {
        DeleteConfirmationDialog(
            itemName = projectToDelete?.name ?: "this project",
            onConfirm = {
                projectToDelete?.let { onDeleteProject(it) }
                projectToDelete = null
            },
            onDismiss = { projectToDelete = null }
        )
    }
}

@Composable
fun ProjectHistoryCard(
    project: ProjectItem,
    currency: String,
    customFieldDefs: List<CustomFieldDef>,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("history_card_${project.id}"),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Main Row: Name, Video Minutes Badge, Category, Client
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = project.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = DateUtils.formatDisplayDate(project.date),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = project.category,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (project.clientName.isNotBlank()) {
                            Text(
                                text = "• ${project.clientName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Minutes Badge & Status
                Column(horizontalAlignment = Alignment.End) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "${if (project.minutes % 1.0 == 0.0) project.minutes.toInt() else project.minutes} min",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    StatusBadge(status = project.status)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Rate & Payment Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Rate: $currency${project.ratePerMinute}/min  •  Payment: $currency${String.format(Locale.US, "%,.0f", project.payment)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (project.workingHours > 0) {
                    Text(
                        text = "Work: ${DateUtils.formatHours(project.workingHours)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Expandable details (Notes, times, custom fields)
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    if (project.startTime.isNotBlank() || project.endTime.isNotBlank()) {
                        Text(
                            text = "Time: ${project.startTime.ifBlank { "--" }} to ${project.endTime.ifBlank { "--" }} (${project.workingHours} hrs)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (project.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Notes: ${project.notes}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Render custom fields safely outside composables
                    val customFieldMap = remember(project.customFieldsJson) {
                        val map = mutableMapOf<String, String>()
                        if (project.customFieldsJson.isNotBlank() && project.customFieldsJson != "{}") {
                            try {
                                val json = JSONObject(project.customFieldsJson)
                                val keys = json.keys()
                                while (keys.hasNext()) {
                                    val k = keys.next()
                                    map[k] = json.optString(k)
                                }
                            } catch (_: Exception) {}
                        }
                        map
                    }

                    for (field in customFieldDefs) {
                        if (field.showInTable) {
                            val v = customFieldMap[field.id]
                            if (!v.isNullOrBlank()) {
                                Text(
                                    text = "${field.label}: $v",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.clickable { expanded = !expanded },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (expanded) "Less" else "More Details",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onDuplicate,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Duplicate",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            modifier = Modifier.size(16.dp),
                            tint = AccentRose
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SpreadsheetView(
    projects: List<ProjectItem>,
    currency: String,
    onEdit: (ProjectItem) -> Unit,
    onDuplicate: (ProjectItem) -> Unit,
    onDelete: (ProjectItem) -> Unit
) {
    val horizontalScroll = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .horizontalScroll(horizontalScroll)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header Row
            Row(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("DATE", Modifier.width(85.dp), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                Text("PROJECT", Modifier.width(130.dp), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                Text("CATEGORY", Modifier.width(100.dp), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                Text("CLIENT", Modifier.width(100.dp), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                Text("MINUTES", Modifier.width(75.dp), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                Text("RATE", Modifier.width(65.dp), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                Text("PAYMENT", Modifier.width(85.dp), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                Text("STATUS", Modifier.width(85.dp), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                Text("ACTIONS", Modifier.width(95.dp), fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }

            Spacer(modifier = Modifier.height(6.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(projects, key = { it.id }) { p ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(6.dp))
                            .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(DateUtils.formatDisplayDate(p.date), Modifier.width(85.dp), fontSize = 11.sp)
                        Text(p.name, Modifier.width(130.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(p.category, Modifier.width(100.dp), fontSize = 11.sp)
                        Text(p.clientName.ifBlank { "--" }, Modifier.width(100.dp), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${p.minutes}m", Modifier.width(75.dp), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                        Text("$currency${p.ratePerMinute}", Modifier.width(65.dp), fontSize = 11.sp)
                        Text("$currency${String.format(Locale.US, "%,.0f", p.payment)}", Modifier.width(85.dp), fontWeight = FontWeight.Bold, color = AccentEmerald, fontSize = 12.sp)
                        Box(Modifier.width(85.dp)) {
                            StatusBadge(status = p.status)
                        }
                        Row(Modifier.width(95.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            IconButton(onClick = { onDuplicate(p) }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate", modifier = Modifier.size(15.dp))
                            }
                            IconButton(onClick = { onEdit(p) }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(15.dp), tint = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = { onDelete(p) }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(15.dp), tint = AccentRose)
                            }
                        }
                    }
                }
            }
        }
    }
}
