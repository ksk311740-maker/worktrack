package com.example.ui.screens

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CategoryItem
import com.example.model.ProjectItem
import com.example.model.UserSettings
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentRose
import com.example.util.DateUtils
import java.util.Locale

data class BulkRowState(
    var date: String,
    var name: String,
    var category: String,
    var minutes: String,
    var rate: String,
    var client: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BulkEntryScreen(
    categories: List<CategoryItem>,
    settings: UserSettings,
    onSaveAll: (List<ProjectItem>) -> Unit,
    onNavigateBack: () -> Unit
) {
    val currency = settings.currencySymbol
    val today = DateUtils.todayIso()

    val rows = remember {
        mutableStateListOf(
            BulkRowState(date = today, name = "Football 5", category = "Football", minutes = "5", rate = "60", client = ""),
            BulkRowState(date = today, name = "Casino 10", category = "Casino", minutes = "10", rate = "40", client = ""),
            BulkRowState(date = today, name = "Documentary 8", category = "Documentary", minutes = "8", rate = "70", client = "")
        )
    }

    // Totals
    var totalMins = 0.0
    var totalEarnings = 0.0
    for (r in rows) {
        val m = r.minutes.toDoubleOrNull() ?: 0.0
        val rate = r.rate.toDoubleOrNull() ?: 0.0
        totalMins += m
        totalEarnings += (m * rate)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bulk Project Entry") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            val projectList = rows.mapNotNull { r ->
                                val mins = r.minutes.toDoubleOrNull() ?: 0.0
                                val rate = r.rate.toDoubleOrNull() ?: settings.defaultRatePerMinute
                                if (r.name.isNotBlank() && mins > 0) {
                                    ProjectItem(
                                        date = r.date.ifBlank { today },
                                        name = r.name.trim(),
                                        category = r.category.trim().ifBlank { "General" },
                                        minutes = mins,
                                        itemsCount = 1,
                                        ratePerMinute = rate,
                                        payment = mins * rate,
                                        clientName = r.client.trim(),
                                        status = ProjectItem.STATUS_COMPLETED
                                    )
                                } else null
                            }

                            if (projectList.isNotEmpty()) {
                                onSaveAll(projectList)
                                onNavigateBack()
                            }
                        },
                        modifier = Modifier
                            .testTag("bulk_save_all_btn")
                            .padding(end = 8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save All (${rows.size})")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .testTag("bulk_entry_screen"),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Summary Card - Highlighting Video Minutes
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.VideoLibrary, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "TOTAL VIDEO MINUTES",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                text = "${totalMins} min",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${rows.size} projects",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$currency${String.format(Locale.US, "%,.0f", totalEarnings)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AccentEmerald
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Projects List",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedButton(
                        onClick = {
                            rows.add(
                                BulkRowState(
                                    date = today,
                                    name = "",
                                    category = "",
                                    minutes = "5",
                                    rate = settings.defaultRatePerMinute.toString(),
                                    client = ""
                                )
                            )
                        },
                        modifier = Modifier.testTag("bulk_add_row_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Row")
                    }
                }
            }

            itemsIndexed(rows) { index, rowState ->
                BulkRowCard(
                    index = index + 1,
                    row = rowState,
                    categories = categories,
                    currency = currency,
                    onUpdate = { updated ->
                        rows[index] = updated
                    },
                    onDelete = {
                        if (rows.size > 1) {
                            rows.removeAt(index)
                        }
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        val projectList = rows.mapNotNull { r ->
                            val mins = r.minutes.toDoubleOrNull() ?: 0.0
                            val rate = r.rate.toDoubleOrNull() ?: settings.defaultRatePerMinute
                            if (r.name.isNotBlank() && mins > 0) {
                                ProjectItem(
                                    date = r.date.ifBlank { today },
                                    name = r.name.trim(),
                                    category = r.category.trim().ifBlank { "General" },
                                    minutes = mins,
                                    itemsCount = 1,
                                    ratePerMinute = rate,
                                    payment = mins * rate,
                                    clientName = r.client.trim(),
                                    status = ProjectItem.STATUS_COMPLETED
                                )
                            } else null
                        }

                        if (projectList.isNotEmpty()) {
                            onSaveAll(projectList)
                            onNavigateBack()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("bulk_bottom_save_btn")
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save All ${rows.size} Projects")
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun BulkRowCard(
    index: Int,
    row: BulkRowState,
    categories: List<CategoryItem>,
    currency: String,
    onUpdate: (BulkRowState) -> Unit,
    onDelete: () -> Unit
) {
    val mins = row.minutes.toDoubleOrNull() ?: 0.0
    val rate = row.rate.toDoubleOrNull() ?: 0.0
    val payment = mins * rate

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "#$index",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${mins} min",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$currency${String.format(Locale.US, "%,.0f", payment)} (${mins}m × $currency$rate)",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = AccentEmerald
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Remove Row",
                            tint = AccentRose,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Project Name & Category (Free text so user can enter any category!)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = row.name,
                    onValueChange = { onUpdate(row.copy(name = it)) },
                    label = { Text("Project Name") },
                    placeholder = { Text("e.g. Football 5") },
                    singleLine = true,
                    modifier = Modifier.weight(1.3f)
                )

                OutlinedTextField(
                    value = row.category,
                    onValueChange = { onUpdate(row.copy(category = it)) },
                    label = { Text("Category") },
                    placeholder = { Text("e.g. Casino") },
                    singleLine = true,
                    modifier = Modifier.weight(1.1f)
                )
            }

            // Quick Category Suggestions if user created any
            if (categories.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (cat in categories) {
                        FilterChip(
                            selected = row.category.equals(cat.name, ignoreCase = true),
                            onClick = { onUpdate(row.copy(category = cat.name)) },
                            label = { Text(cat.name, fontSize = 11.sp) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Minutes, Rate, Date & Client Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = row.minutes,
                    onValueChange = { onUpdate(row.copy(minutes = it)) },
                    label = { Text("Mins *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(0.9f)
                )

                OutlinedTextField(
                    value = row.rate,
                    onValueChange = { onUpdate(row.copy(rate = it)) },
                    label = { Text("Rate *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(0.9f)
                )

                OutlinedTextField(
                    value = row.client,
                    onValueChange = { onUpdate(row.copy(client = it)) },
                    label = { Text("Client") },
                    placeholder = { Text("Optional") },
                    singleLine = true,
                    modifier = Modifier.weight(1.1f)
                )

                OutlinedTextField(
                    value = row.date,
                    onValueChange = { onUpdate(row.copy(date = it)) },
                    label = { Text("Date") },
                    singleLine = true,
                    modifier = Modifier.weight(1.1f)
                )
            }
        }
    }
}
