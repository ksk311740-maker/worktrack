package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
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
import com.example.ui.theme.MiniWaveformGraphic
import com.example.ui.theme.StaggeredEntrance
import com.example.ui.theme.SuccessCheckmarkAnimation
import com.example.ui.theme.VideoTimelineRuler
import com.example.ui.theme.ViewfinderMarks
import com.example.ui.theme.bouncyClickable
import com.example.ui.theme.rememberPressScaleModifier
import com.example.util.DateUtils
import org.json.JSONObject
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProjectScreen(
    editingProject: ProjectItem?,
    categories: List<CategoryItem>,
    settings: UserSettings,
    customFieldDefs: List<CustomFieldDef>,
    onSave: (ProjectItem) -> Unit,
    onAddCategory: (String, String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val currency = settings.currencySymbol

    // Primary Core Fields
    var date by remember { mutableStateOf(editingProject?.date ?: DateUtils.todayIso()) }
    var name by remember { mutableStateOf(editingProject?.name ?: "") }
    var categoryText by remember { mutableStateOf(editingProject?.category ?: "") }
    var minutesText by remember { mutableStateOf(if (editingProject != null) (if (editingProject.minutes % 1.0 == 0.0) editingProject.minutes.toInt().toString() else editingProject.minutes.toString()) else "5") }
    var rateText by remember { mutableStateOf(if (editingProject != null) editingProject.ratePerMinute.toString() else settings.defaultRatePerMinute.toString()) }

    var isOverridePayment by remember { mutableStateOf(editingProject?.isPaymentOverridden ?: false) }
    var manualPaymentText by remember { mutableStateOf(if (editingProject != null) editingProject.payment.toString() else "") }

    // Secondary Optional Fields
    var clientName by remember { mutableStateOf(editingProject?.clientName ?: "") }
    var youtubeChannel by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf(editingProject?.startTime ?: "") }
    var endTime by remember { mutableStateOf(editingProject?.endTime ?: "") }
    var workingHoursText by remember { mutableStateOf(if (editingProject != null && editingProject.workingHours > 0) editingProject.workingHours.toString() else "") }
    var itemsCountText by remember { mutableStateOf(if (editingProject != null) editingProject.itemsCount.toString() else "1") }
    var notes by remember { mutableStateOf(editingProject?.notes ?: "") }
    var status by remember { mutableStateOf(editingProject?.status ?: ProjectItem.STATUS_COMPLETED) }

    // State for animated focus on the prominent Video Minutes card
    var isMinutesFocused by remember { mutableStateOf(false) }

    // Animation state for save success checkmark
    var isSavingSuccess by remember { mutableStateOf(false) }
    var pendingSavedProject by remember { mutableStateOf<ProjectItem?>(null) }

    // Custom fields map
    val customFieldsValues = remember { mutableStateMapOf<String, String>() }
    LaunchedEffect(editingProject) {
        if (editingProject != null && editingProject.customFieldsJson.isNotBlank()) {
            try {
                val json = JSONObject(editingProject.customFieldsJson)
                val keys = json.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    customFieldsValues[k] = json.getString(k)
                    if (k == "youtube_channel" && youtubeChannel.isBlank()) {
                        youtubeChannel = json.getString(k)
                    }
                }
            } catch (_: Exception) {}
        }
    }

    var showAddCategoryDialog by remember { mutableStateOf(false) }

    // Auto-calculate working hours if start & end times are present
    LaunchedEffect(startTime, endTime) {
        if (startTime.isNotBlank() && endTime.isNotBlank()) {
            val calcHours = DateUtils.calculateWorkingHours(startTime, endTime)
            if (calcHours > 0) {
                workingHoursText = calcHours.toString()
            }
        }
    }

    // Auto-calculate payment: Completed Minutes × Rate Per Minute
    val parsedMinutes = minutesText.toDoubleOrNull() ?: 0.0
    val parsedRate = rateText.toDoubleOrNull() ?: 0.0
    val autoPayment = parsedMinutes * parsedRate

    val finalPayment = if (isOverridePayment) {
        manualPaymentText.toDoubleOrNull() ?: autoPayment
    } else {
        autoPayment
    }

    var validationError by remember { mutableStateOf<String?>(null) }

    fun submitProject() {
        if (name.isBlank()) {
            validationError = "Please enter a project name"
            return
        }
        if (parsedMinutes <= 0) {
            validationError = "Completed video minutes must be greater than 0"
            return
        }
        val finalCat = categoryText.trim().ifBlank { "General" }

        // Serialize custom fields
        val customJson = JSONObject()
        for ((k, v) in customFieldsValues) {
            if (v.isNotBlank()) customJson.put(k, v)
        }
        if (youtubeChannel.isNotBlank()) {
            customJson.put("youtube_channel", youtubeChannel.trim())
        }

        val project = ProjectItem(
            id = editingProject?.id ?: 0L,
            date = date,
            name = name.trim(),
            category = finalCat,
            minutes = parsedMinutes,
            itemsCount = itemsCountText.toIntOrNull() ?: 1,
            ratePerMinute = parsedRate,
            payment = finalPayment,
            isPaymentOverridden = isOverridePayment,
            startTime = startTime.trim(),
            endTime = endTime.trim(),
            workingHours = workingHoursText.toDoubleOrNull() ?: 0.0,
            clientName = clientName.trim(),
            notes = notes.trim(),
            status = status,
            customFieldsJson = customJson.toString(),
            createdAt = editingProject?.createdAt ?: System.currentTimeMillis()
        )

        // Store pending project and trigger satisfying short checkmark animation
        pendingSavedProject = project
        isSavingSuccess = true
    }

    val saveBtnInteractionSource = remember { MutableInteractionSource() }
    val saveBtnPressScale = rememberPressScaleModifier(saveBtnInteractionSource)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (editingProject == null) "Log Video Project" else "Edit Project",
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        MiniWaveformGraphic(
                            modifier = Modifier.width(36.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = { submitProject() },
                        enabled = !isSavingSuccess,
                        interactionSource = saveBtnInteractionSource,
                        modifier = Modifier
                            .then(saveBtnPressScale)
                            .testTag("save_project_submit_btn")
                            .padding(end = 8.dp)
                    ) {
                        if (isSavingSuccess) {
                            SuccessCheckmarkAnimation(
                                modifier = Modifier.size(18.dp),
                                onAnimationEnd = {
                                    pendingSavedProject?.let { onSave(it) }
                                }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Saved!", fontWeight = FontWeight.Bold)
                        } else {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save Project")
                        }
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
                .testTag("add_project_form"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Subtle Video Editing Timeline Graphic at top
            item {
                VideoTimelineRuler(
                    modifier = Modifier.padding(vertical = 4.dp),
                    accentColor = MaterialTheme.colorScheme.primary
                )
            }

            if (validationError != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = validationError ?: "",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            IconButton(onClick = { validationError = null }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onErrorContainer)
                            }
                        }
                    }
                }
            }

            // STAGGERED FORM SECTION 0: PROJECT NAME & DATE
            item {
                StaggeredEntrance(index = 0) {
                    Column {
                        Text(
                            text = "Project Information",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = name,
                            onValueChange = {
                                name = it
                                validationError = null
                            },
                            label = { Text("Project Name *") },
                            placeholder = { Text("e.g. Football 5, Casino 10, Documentary 8") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_project_name")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = date,
                            onValueChange = { date = it },
                            label = { Text("Date (YYYY-MM-DD)") },
                            trailingIcon = {
                                Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null)
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_project_date")
                        )
                    }
                }
            }

            // STAGGERED FORM SECTION 1: CATEGORY (User completely controls categories)
            item {
                StaggeredEntrance(index = 1) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Category",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "+ Add New",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .bouncyClickable { showAddCategoryDialog = true }
                                    .padding(4.dp)
                            )
                        }

                        // If user has existing categories, show them as fast pick chips
                        if (categories.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                for (cat in categories) {
                                    FilterChip(
                                        selected = categoryText.equals(cat.name, ignoreCase = true),
                                        onClick = { categoryText = cat.name },
                                        label = { Text(cat.name) }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        OutlinedTextField(
                            value = categoryText,
                            onValueChange = { categoryText = it },
                            label = { Text("Category (select above or type any custom category)") },
                            placeholder = { Text("e.g. Casino, Football, Documentary, Short...") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_project_category")
                        )
                    }
                }
            }

            // STAGGERED FORM SECTION 2: COMPLETED VIDEO MINUTES & RATE PER MINUTE
            // Visual Emphasis with subtle viewfinder marks and smooth animated focus highlight
            item {
                StaggeredEntrance(index = 2) {
                    val focusBorderAlpha by animateFloatAsState(
                        targetValue = if (isMinutesFocused) 0.85f else 0.35f,
                        animationSpec = tween(durationMillis = 280),
                        label = "card_focus_border"
                    )
                    val focusBgAlpha by animateFloatAsState(
                        targetValue = if (isMinutesFocused) 0.65f else 0.45f,
                        animationSpec = tween(durationMillis = 280),
                        label = "card_focus_bg"
                    )
                    val viewfinderAlpha by animateFloatAsState(
                        targetValue = if (isMinutesFocused) 0.70f else 0.28f,
                        animationSpec = tween(durationMillis = 280),
                        label = "viewfinder_alpha"
                    )

                    Box(modifier = Modifier.fillMaxWidth()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = focusBgAlpha)
                            ),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = SolidColor(MaterialTheme.colorScheme.primary.copy(alpha = focusBorderAlpha))
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.VideoLibrary,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "COMPLETED VIDEO MINUTES",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    // Payment result preview
                                    Text(
                                        text = "$currency${String.format(Locale.US, "%,.2f", finalPayment)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = AccentEmerald
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Large Video Minutes Input + Rate Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedTextField(
                                        value = minutesText,
                                        onValueChange = {
                                            minutesText = it
                                            validationError = null
                                        },
                                        label = { Text("Video Minutes *", fontWeight = FontWeight.Bold) },
                                        placeholder = { Text("e.g. 5, 10, 15") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        modifier = Modifier
                                            .weight(1.2f)
                                            .onFocusChanged { isMinutesFocused = it.isFocused }
                                            .testTag("input_project_minutes")
                                    )

                                    OutlinedTextField(
                                        value = rateText,
                                        onValueChange = { rateText = it },
                                        label = { Text("Rate / min ($currency) *") },
                                        placeholder = { Text("e.g. 60") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("input_project_rate")
                                    )
                                }

                                // Quick duration preset chips with bouncy tactile feedback
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    for (m in listOf(3, 5, 8, 10, 15, 20, 30)) {
                                        SuggestionChip(
                                            onClick = { minutesText = m.toString() },
                                            label = { Text("${m}m") },
                                            modifier = Modifier.bouncyClickable { minutesText = m.toString() }
                                        )
                                    }
                                }

                                // Payment override toggle
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isOverridePayment,
                                        onCheckedChange = {
                                            isOverridePayment = it
                                            if (it && manualPaymentText.isBlank()) {
                                                manualPaymentText = autoPayment.toString()
                                            }
                                        },
                                        modifier = Modifier.testTag("checkbox_override_payment")
                                    )
                                    Text(
                                        text = "Manual payment override (${parsedMinutes}m × $currency$parsedRate = $currency${String.format(Locale.US, "%,.0f", autoPayment)})",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.clickable {
                                            isOverridePayment = !isOverridePayment
                                            if (isOverridePayment && manualPaymentText.isBlank()) {
                                                manualPaymentText = autoPayment.toString()
                                            }
                                        }
                                    )
                                }

                                AnimatedVisibility(visible = isOverridePayment) {
                                    OutlinedTextField(
                                        value = manualPaymentText,
                                        onValueChange = { manualPaymentText = it },
                                        label = { Text("Override Payment Amount ($currency)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 4.dp)
                                            .testTag("input_manual_payment")
                                    )
                                }
                            }
                        }

                        // Viewfinder subtle corner marks with dynamic focus highlight
                        ViewfinderMarks(
                            modifier = Modifier
                                .matchParentSize()
                                .padding(4.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = viewfinderAlpha)
                        )
                    }
                }
            }

            // STAGGERED FORM SECTION 3: CLIENT & DISTRIBUTION (Optional)
            item {
                StaggeredEntrance(index = 3) {
                    Column {
                        Text(
                            text = "Client & Distribution (Optional)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Assign this project to a client or YouTube channel.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = clientName,
                                onValueChange = { clientName = it },
                                label = { Text("Client / Company") },
                                placeholder = { Text("e.g. Creator A, Studio B") },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_client_name")
                            )

                            OutlinedTextField(
                                value = youtubeChannel,
                                onValueChange = { youtubeChannel = it },
                                label = { Text("YouTube Channel") },
                                placeholder = { Text("e.g. @MyChannel") },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_youtube_channel")
                            )
                        }
                    }
                }
            }

            // STAGGERED FORM SECTION 4: WORKING HOURS & TIME TRACKING (Optional)
            item {
                StaggeredEntrance(index = 4) {
                    Column {
                        Text(
                            text = "Working Hours (Optional)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Enter start and end time to automatically calculate editing hours.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = startTime,
                                onValueChange = { startTime = it },
                                label = { Text("Start Time") },
                                placeholder = { Text("09:00 or 9:00 AM") },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_start_time")
                            )

                            OutlinedTextField(
                                value = endTime,
                                onValueChange = { endTime = it },
                                label = { Text("End Time") },
                                placeholder = { Text("11:30 or 11:30 AM") },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_end_time")
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = workingHoursText,
                            onValueChange = { workingHoursText = it },
                            label = { Text("Calculated Working Hours") },
                            placeholder = { Text("e.g. 2.5") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            trailingIcon = {
                                Icon(imageVector = Icons.Default.Schedule, contentDescription = null)
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_working_hours")
                        )
                    }
                }
            }

            // STAGGERED FORM SECTION 5: STATUS SELECTOR
            item {
                StaggeredEntrance(index = 5) {
                    Column {
                        Text(
                            text = "Status",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val statuses = listOf(ProjectItem.STATUS_COMPLETED, ProjectItem.STATUS_PENDING, ProjectItem.STATUS_CANCELLED)
                            for (st in statuses) {
                                FilterChip(
                                    selected = status == st,
                                    onClick = { status = st },
                                    label = { Text(st) },
                                    modifier = Modifier.testTag("status_chip_${st.lowercase()}")
                                )
                            }
                        }
                    }
                }
            }

            // STAGGERED FORM SECTION 6: NOTES & CUSTOM FIELDS
            item {
                StaggeredEntrance(index = 6) {
                    Column {
                        Text(
                            text = "Notes & Additional Info",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Project Notes") },
                            placeholder = { Text("Sound design, 4K rendering notes, revisions...") },
                            minLines = 3,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_project_notes")
                        )
                    }
                }
            }

            // Dynamic Custom Fields
            val activeCustomFields = customFieldDefs.filter { it.isEnabled && it.id != "youtube_channel" }
            for ((idx, field) in activeCustomFields.withIndex()) {
                item {
                    StaggeredEntrance(index = 7 + idx) {
                        OutlinedTextField(
                            value = customFieldsValues[field.id] ?: "",
                            onValueChange = { customFieldsValues[field.id] = it },
                            label = { Text(field.label) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("custom_field_${field.id}")
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (showAddCategoryDialog) {
        AddCategoryDialog(
            onAdd = { name, color ->
                onAddCategory(name, color)
                categoryText = name
                showAddCategoryDialog = false
            },
            onDismiss = { showAddCategoryDialog = false }
        )
    }
}
