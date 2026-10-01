package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CategoryItem
import com.example.model.CustomFieldDef
import com.example.model.ProjectItem
import com.example.model.UserSettings
import com.example.ui.components.AddCategoryDialog
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentRose
import com.example.util.CsvBackupHelper
import com.example.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: UserSettings,
    categories: List<CategoryItem>,
    customFields: List<CustomFieldDef>,
    allProjects: List<ProjectItem>,
    onUpdateSettings: (UserSettings) -> Unit,
    onAddCategory: (String, String) -> Unit,
    onRenameCategory: (CategoryItem, String) -> Unit,
    onDeleteCategory: (CategoryItem) -> Unit,
    onUpdateCustomFields: (List<CustomFieldDef>) -> Unit,
    onImportCsv: (String) -> Pair<Boolean, String>,
    onRestoreJson: (String) -> Pair<Boolean, String>,
    onResetSampleData: () -> Unit
) {
    val context = LocalContext.current

    // Local form state
    var editorName by remember(settings) { mutableStateOf(settings.editorName) }
    var editorEmail by remember(settings) { mutableStateOf(settings.editorEmail) }
    var editorPhone by remember(settings) { mutableStateOf(settings.editorPhone) }
    var editorAddress by remember(settings) { mutableStateOf(settings.editorAddress) }
    var defaultRateText by remember(settings) { mutableStateOf(settings.defaultRatePerMinute.toString()) }
    var currencySymbol by remember(settings) { mutableStateOf(settings.currencySymbol) }
    var defaultClientName by remember(settings) { mutableStateOf(settings.defaultClientName) }
    var paymentInfo by remember(settings) { mutableStateOf(settings.paymentInfo) }
    var invoiceNotes by remember(settings) { mutableStateOf(settings.invoiceNotes) }
    var themeMode by remember(settings) { mutableStateOf(settings.themeMode) }

    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var categoryToRename by remember { mutableStateOf<CategoryItem?>(null) }
    var renameText by remember { mutableStateOf("") }

    var showImportCsvDialog by remember { mutableStateOf(false) }
    var showRestoreJsonDialog by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var showAddFieldDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_screen")
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "App Settings",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = {
                        val rate = defaultRateText.toDoubleOrNull() ?: 60.0
                        val updated = settings.copy(
                            editorName = editorName.trim(),
                            editorEmail = editorEmail.trim(),
                            editorPhone = editorPhone.trim(),
                            editorAddress = editorAddress.trim(),
                            defaultRatePerMinute = rate,
                            currencySymbol = currencySymbol.trim(),
                            defaultClientName = defaultClientName.trim(),
                            paymentInfo = paymentInfo.trim(),
                            invoiceNotes = invoiceNotes.trim(),
                            themeMode = themeMode
                        )
                        onUpdateSettings(updated)
                        Toast.makeText(context, "Settings saved!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.testTag("save_all_settings_btn")
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save Changes")
                }
            }
        }

        // Section: Editor Profile & Default Rate Suggestion
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Editor Profile & Initial Rate", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    OutlinedTextField(
                        value = editorName,
                        onValueChange = { editorName = it },
                        label = { Text("My Name / Business Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = defaultRateText,
                            onValueChange = { defaultRateText = it },
                            label = { Text("Default Rate / Min (New projects)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1.3f)
                        )

                        OutlinedTextField(
                            value = currencySymbol,
                            onValueChange = { currencySymbol = it },
                            label = { Text("Currency") },
                            placeholder = { Text("৳") },
                            singleLine = true,
                            modifier = Modifier.weight(0.7f)
                        )
                    }

                    Text(
                        text = "Note: Every project has its own rate. This default only pre-fills new entries.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = editorEmail,
                        onValueChange = { editorEmail = it },
                        label = { Text("Email Address") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editorPhone,
                        onValueChange = { editorPhone = it },
                        label = { Text("Phone Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editorAddress,
                        onValueChange = { editorAddress = it },
                        label = { Text("Address / City") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Section: Appearance Theme
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Appearance Theme", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (mode in listOf("SYSTEM", "DARK", "LIGHT")) {
                            FilterChip(
                                selected = themeMode == mode,
                                onClick = {
                                    themeMode = mode
                                    onUpdateSettings(settings.copy(themeMode = mode))
                                },
                                label = { Text(mode.lowercase().replaceFirstChar { it.uppercase() }) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Section: User-Defined Categories Manager (Create, Rename, Delete)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "My Categories (${categories.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Create, rename, or remove categories",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        OutlinedButton(
                            onClick = { showAddCategoryDialog = true },
                            modifier = Modifier.testTag("settings_add_category_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (categories.isEmpty()) {
                        Text(
                            text = "No categories created yet. Click 'New' to add one, or simply type a new category when logging projects.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            for (cat in categories) {
                                val catColor = try {
                                    Color(android.graphics.Color.parseColor(cat.colorHex))
                                } catch (_: Exception) {
                                    MaterialTheme.colorScheme.primary
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clip(CircleShape)
                                                .background(catColor)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = cat.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }

                                    Row {
                                        IconButton(
                                            onClick = {
                                                categoryToRename = cat
                                                renameText = cat.name
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Rename Category",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = { onDeleteCategory(cat) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete Category",
                                                tint = AccentRose,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section: Customizable Columns / Fields
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Custom Fields & Columns",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Choose which fields appear in table & invoice",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        OutlinedButton(
                            onClick = { showAddFieldDialog = true }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (field in customFields) {
                            ElevatedCard(
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = field.label,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )

                                        Switch(
                                            checked = field.isEnabled,
                                            onCheckedChange = { checked ->
                                                val updated = customFields.map {
                                                    if (it.id == field.id) it.copy(isEnabled = checked) else it
                                                }
                                                onUpdateCustomFields(updated)
                                            }
                                        )
                                    }

                                    if (field.isEnabled) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Checkbox(
                                                    checked = field.showInTable,
                                                    onCheckedChange = { checked ->
                                                        val updated = customFields.map {
                                                            if (it.id == field.id) it.copy(showInTable = checked) else it
                                                        }
                                                        onUpdateCustomFields(updated)
                                                    }
                                                )
                                                Text("Show in Table", style = MaterialTheme.typography.bodySmall)
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Checkbox(
                                                    checked = field.showInInvoice,
                                                    onCheckedChange = { checked ->
                                                        val updated = customFields.map {
                                                            if (it.id == field.id) it.copy(showInInvoice = checked) else it
                                                        }
                                                        onUpdateCustomFields(updated)
                                                    }
                                                )
                                                Text("Show in Invoice", style = MaterialTheme.typography.bodySmall)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section: Invoice & Payment Configuration
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Receipt, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Invoice & Payment Info", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    OutlinedTextField(
                        value = defaultClientName,
                        onValueChange = { defaultClientName = it },
                        label = { Text("Default Client / Company Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = paymentInfo,
                        onValueChange = { paymentInfo = it },
                        label = { Text("Default Payment Information (bKash/Nagad/Bank)") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = invoiceNotes,
                        onValueChange = { invoiceNotes = it },
                        label = { Text("Default Invoice Notes & Terms") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Section: Data Backup, CSV Export & Import
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Backup, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Data Storage & Backup", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    Text(
                        text = "You own your data! Export full work history to CSV or backup and restore your complete database anytime.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val csv = CsvBackupHelper.exportProjectsToCsv(allProjects)
                                CsvBackupHelper.shareTextFile(
                                    context,
                                    "worktrack_projects_${DateUtils.todayIso()}.csv",
                                    csv,
                                    "text/csv",
                                    "Export Projects CSV"
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("export_csv_btn")
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Export CSV")
                        }

                        OutlinedButton(
                            onClick = { showImportCsvDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("import_csv_btn")
                        ) {
                            Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Import CSV")
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val json = CsvBackupHelper.exportBackupJson(allProjects, categories, settings)
                                CsvBackupHelper.shareTextFile(
                                    context,
                                    "worktrack_backup_${DateUtils.todayIso()}.json",
                                    json,
                                    "application/json",
                                    "Backup WorkTrack Data"
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("backup_json_btn")
                        ) {
                            Icon(Icons.Default.Backup, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Backup JSON")
                        }

                        OutlinedButton(
                            onClick = { showRestoreJsonDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("restore_json_btn")
                        ) {
                            Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Restore JSON")
                        }
                    }

                    OutlinedButton(
                        onClick = { showResetConfirmDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentRose),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reset_sample_btn")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reset to Example Projects")
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showAddCategoryDialog) {
        AddCategoryDialog(
            onAdd = { name, color ->
                onAddCategory(name, color)
                showAddCategoryDialog = false
            },
            onDismiss = { showAddCategoryDialog = false }
        )
    }

    // Rename Category Dialog
    if (categoryToRename != null) {
        AlertDialog(
            onDismissRequest = { categoryToRename = null },
            title = { Text("Rename Category") },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    label = { Text("New Category Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cat = categoryToRename
                        if (cat != null && renameText.isNotBlank()) {
                            onRenameCategory(cat, renameText.trim())
                            categoryToRename = null
                        }
                    },
                    enabled = renameText.isNotBlank()
                ) {
                    Text("Rename")
                }
            },
            dismissButton = {
                TextButton(onClick = { categoryToRename = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add Custom Field Dialog
    if (showAddFieldDialog) {
        var newFieldLabel by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddFieldDialog = false },
            title = { Text("New Custom Field") },
            text = {
                OutlinedTextField(
                    value = newFieldLabel,
                    onValueChange = { newFieldLabel = it },
                    label = { Text("Field Label") },
                    placeholder = { Text("e.g. YouTube Channel, Priority") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFieldLabel.isNotBlank()) {
                            val id = newFieldLabel.trim().lowercase().replace("[^a-z0-9]".toRegex(), "_")
                            val newField = CustomFieldDef(
                                id = id,
                                label = newFieldLabel.trim(),
                                isEnabled = true,
                                showInTable = true,
                                showInInvoice = false
                            )
                            onUpdateCustomFields(customFields + newField)
                            showAddFieldDialog = false
                        }
                    },
                    enabled = newFieldLabel.isNotBlank()
                ) {
                    Text("Add Field")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddFieldDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Import CSV Dialog
    if (showImportCsvDialog) {
        var csvInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showImportCsvDialog = false },
            title = { Text("Import Projects from CSV") },
            text = {
                Column {
                    Text(
                        text = "Paste CSV rows (e.g., Date, Project Name, Category, Minutes, Rate, Payment):",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = csvInput,
                        onValueChange = { csvInput = it },
                        placeholder = { Text("2026-09-30, Football 5, Football, 5, 60, 300\n2026-09-30, Casino 10, Casino, 10, 40, 400") },
                        minLines = 5,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("csv_paste_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val (success, message) = onImportCsv(csvInput)
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        if (success) showImportCsvDialog = false
                    },
                    enabled = csvInput.isNotBlank(),
                    modifier = Modifier.testTag("confirm_import_csv_btn")
                ) {
                    Text("Import")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportCsvDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Restore JSON Backup Dialog
    if (showRestoreJsonDialog) {
        var jsonInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showRestoreJsonDialog = false },
            title = { Text("Restore Backup (JSON)") },
            text = {
                Column {
                    Text(
                        text = "Paste backup JSON content below:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = jsonInput,
                        onValueChange = { jsonInput = it },
                        placeholder = { Text("{ \"projects\": [ ... ], \"categories\": [ ... ] }") },
                        minLines = 5,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val (success, message) = onRestoreJson(jsonInput)
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        if (success) showRestoreJsonDialog = false
                    },
                    enabled = jsonInput.isNotBlank()
                ) {
                    Text("Restore")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreJsonDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Reset Confirmation
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text("Load Example Projects?") },
            text = { Text("This will replace current projects with example data to showcase the video minutes tracker and calculations.") },
            confirmButton = {
                Button(
                    onClick = {
                        onResetSampleData()
                        showResetConfirmDialog = false
                        Toast.makeText(context, "Example projects loaded!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRose)
                ) {
                    Text("Load Examples")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
