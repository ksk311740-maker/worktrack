package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.model.CategoryItem
import com.example.model.ProjectItem
import com.example.model.UserSettings
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.SuccessCheckmarkAnimation
import com.example.ui.theme.bouncyClickable
import com.example.util.DateUtils
import java.util.Locale

@Composable
fun QuickAddDialog(
    categories: List<CategoryItem>,
    settings: UserSettings,
    onSave: (ProjectItem) -> Unit,
    onDismiss: () -> Unit
) {
    val currency = settings.currencySymbol
    var date by remember { mutableStateOf(DateUtils.todayIso()) }
    var name by remember { mutableStateOf("") }
    var categoryText by remember { mutableStateOf(categories.firstOrNull()?.name ?: "") }
    var minutesText by remember { mutableStateOf("5") }
    var rateText by remember { mutableStateOf(settings.defaultRatePerMinute.toString()) }

    var isSavingSuccess by remember { mutableStateOf(false) }
    var pendingProject by remember { mutableStateOf<ProjectItem?>(null) }

    val parsedMins = minutesText.toDoubleOrNull() ?: 0.0
    val parsedRate = rateText.toDoubleOrNull() ?: settings.defaultRatePerMinute
    val calculatedPayment = parsedMins * parsedRate

    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Quick Add Video Project",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quick_add_dialog_content")
            ) {
                // Video Minutes & Calculated Payment Header
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VideoLibrary, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${parsedMins} min",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Text(
                            text = "$currency${String.format(Locale.US, "%,.0f", calculatedPayment)} (${parsedMins}m × $currency$parsedRate)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = AccentEmerald
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Project Name
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorMsg = null
                    },
                    label = { Text("Project Name *") },
                    placeholder = { Text("e.g. Football 5, Casino 10") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quick_input_name")
                )

                if (errorMsg != null) {
                    Text(
                        text = errorMsg ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Category Chips + Text Input
                if (categories.isNotEmpty()) {
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
                    Spacer(modifier = Modifier.height(4.dp))
                }

                OutlinedTextField(
                    value = categoryText,
                    onValueChange = { categoryText = it },
                    label = { Text("Category") },
                    placeholder = { Text("e.g. Casino, Football, Documentary") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Minutes & Rate Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = minutesText,
                        onValueChange = { minutesText = it },
                        label = { Text("Video Mins *", fontWeight = FontWeight.Bold) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("quick_input_minutes")
                    )

                    OutlinedTextField(
                        value = rateText,
                        onValueChange = { rateText = it },
                        label = { Text("Rate ($currency)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_input_rate")
                    )
                }

                // Quick Minutes Increment Chips
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (minVal in listOf(3, 5, 8, 10, 15, 20)) {
                        SuggestionChip(
                            onClick = { minutesText = minVal.toString() },
                            label = { Text("${minVal}m") },
                            modifier = Modifier.bouncyClickable { minutesText = minVal.toString() }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        errorMsg = "Please enter project name"
                        return@Button
                    }
                    if (parsedMins <= 0) {
                        errorMsg = "Video minutes must be > 0"
                        return@Button
                    }

                    val project = ProjectItem(
                        date = date,
                        name = name.trim(),
                        category = categoryText.trim().ifBlank { "General" },
                        minutes = parsedMins,
                        itemsCount = 1,
                        ratePerMinute = parsedRate,
                        payment = calculatedPayment,
                        status = ProjectItem.STATUS_COMPLETED
                    )
                    pendingProject = project
                    isSavingSuccess = true
                },
                enabled = !isSavingSuccess,
                modifier = Modifier.testTag("quick_add_confirm_btn")
            ) {
                if (isSavingSuccess) {
                    SuccessCheckmarkAnimation(
                        modifier = Modifier.size(16.dp),
                        onAnimationEnd = {
                            pendingProject?.let { onSave(it) }
                            onDismiss()
                        }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Saved!")
                } else {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("quick_add_cancel_btn")
            ) {
                Text("Cancel")
            }
        }
    )
}
