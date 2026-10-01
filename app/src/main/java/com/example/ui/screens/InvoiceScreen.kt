package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ProjectItem
import com.example.model.UserSettings
import com.example.ui.theme.AccentEmerald
import com.example.util.DateUtils
import com.example.util.PdfInvoiceGenerator
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceScreen(
    allProjects: List<ProjectItem>,
    clients: List<String>,
    settings: UserSettings,
    initialMonth: String = DateUtils.currentYearMonth(),
    onUpdateSettings: (UserSettings) -> Unit
) {
    val context = LocalContext.current
    val availableMonths = remember { DateUtils.generateYearMonthsList() }

    var selectedMonth by remember { mutableStateOf(initialMonth) }
    var monthExpanded by remember { mutableStateOf(false) }

    var selectedClientFilter by remember { mutableStateOf<String?>(null) }
    var clientFilterExpanded by remember { mutableStateOf(false) }

    // Invoice Customization fields
    var invoiceNumber by remember { mutableStateOf("INV-${selectedMonth.replace("-", "")}-001") }
    var invoiceDate by remember { mutableStateOf(DateUtils.todayIso()) }
    var invoiceTitle by remember { mutableStateOf(settings.invoiceTitle) }
    var clientName by remember { mutableStateOf("") }
    var clientEmail by remember { mutableStateOf("") }
    var clientAddress by remember { mutableStateOf("") }
    var currencySymbol by remember { mutableStateOf(settings.currencySymbol) }
    var customNotes by remember { mutableStateOf(settings.invoiceNotes) }
    var paymentInfoText by remember { mutableStateOf(settings.paymentInfo) }

    var showEditDetails by remember { mutableStateOf(false) }

    // Filter projects for selected billing month AND selected client
    val monthProjects = allProjects.filter { p ->
        p.date.startsWith(selectedMonth) &&
        p.status == ProjectItem.STATUS_COMPLETED &&
        (selectedClientFilter == null || p.clientName.equals(selectedClientFilter, ignoreCase = true))
    }

    val totalMins = monthProjects.sumOf { it.minutes }
    val grandTotal = monthProjects.sumOf { it.payment }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("invoice_screen")
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Billing Month & Client Filter Selection Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Month Picker
                ExposedDropdownMenuBox(
                    expanded = monthExpanded,
                    onExpandedChange = { monthExpanded = !monthExpanded },
                    modifier = Modifier.weight(1.1f)
                ) {
                    OutlinedTextField(
                        value = DateUtils.formatDisplayMonthYear(selectedMonth),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Billing Month") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = monthExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .testTag("invoice_month_select")
                    )

                    ExposedDropdownMenu(
                        expanded = monthExpanded,
                        onDismissRequest = { monthExpanded = false }
                    ) {
                        for (m in availableMonths) {
                            DropdownMenuItem(
                                text = { Text(DateUtils.formatDisplayMonthYear(m)) },
                                onClick = {
                                    selectedMonth = m
                                    invoiceNumber = "INV-${m.replace("-", "")}-001"
                                    monthExpanded = false
                                }
                            )
                        }
                    }
                }

                // Client Selector
                ExposedDropdownMenuBox(
                    expanded = clientFilterExpanded,
                    onExpandedChange = { clientFilterExpanded = !clientFilterExpanded },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = selectedClientFilter ?: "All Clients",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Client") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = clientFilterExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .testTag("invoice_client_filter")
                    )

                    ExposedDropdownMenu(
                        expanded = clientFilterExpanded,
                        onDismissRequest = { clientFilterExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Clients") },
                            onClick = {
                                selectedClientFilter = null
                                clientFilterExpanded = false
                            }
                        )
                        for (c in clients) {
                            DropdownMenuItem(
                                text = { Text(c) },
                                onClick = {
                                    selectedClientFilter = c
                                    clientName = c
                                    clientFilterExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Customize Toggle Button
        item {
            FilledTonalButton(
                onClick = { showEditDetails = !showEditDetails },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("invoice_customize_btn")
            ) {
                Icon(imageVector = Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (showEditDetails) "Hide Invoice Customizer" else "Customize Invoice Header & Details")
            }
        }

        // Customizable Invoice Details Accordion
        item {
            AnimatedVisibility(visible = showEditDetails) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Customize Invoice Fields",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = invoiceNumber,
                                onValueChange = { invoiceNumber = it },
                                label = { Text("Invoice #") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = invoiceDate,
                                onValueChange = { invoiceDate = it },
                                label = { Text("Invoice Date") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = invoiceTitle,
                                onValueChange = { invoiceTitle = it },
                                label = { Text("Invoice Title") },
                                singleLine = true,
                                modifier = Modifier.weight(1.4f)
                            )
                            OutlinedTextField(
                                value = currencySymbol,
                                onValueChange = { currencySymbol = it },
                                label = { Text("Currency") },
                                singleLine = true,
                                modifier = Modifier.weight(0.6f)
                            )
                        }

                        OutlinedTextField(
                            value = clientName,
                            onValueChange = { clientName = it },
                            label = { Text("Client / Company Name") },
                            placeholder = { Text("e.g. Creator A or Studio B") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = clientAddress,
                            onValueChange = { clientAddress = it },
                            label = { Text("Client Address / Email") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = paymentInfoText,
                            onValueChange = { paymentInfoText = it },
                            label = { Text("Payment Info (bKash, Nagad, Bank details)") },
                            minLines = 2,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = customNotes,
                            onValueChange = { customNotes = it },
                            label = { Text("Invoice Notes & Terms") },
                            minLines = 2,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Primary Actions: Download PDF / Print PDF
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        if (monthProjects.isEmpty()) {
                            Toast.makeText(context, "No completed projects for this client/month to invoice", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val invoiceData = PdfInvoiceGenerator.InvoiceData(
                            invoiceNumber = invoiceNumber,
                            invoiceDate = invoiceDate,
                            billingPeriod = DateUtils.formatDisplayMonthYear(selectedMonth),
                            invoiceTitle = invoiceTitle,
                            clientName = clientName.ifBlank { selectedClientFilter ?: "Client" },
                            clientEmail = clientEmail,
                            clientAddress = clientAddress,
                            projects = monthProjects,
                            currencySymbol = currencySymbol,
                            customNotes = customNotes
                        )
                        val customSettings = settings.copy(
                            paymentInfo = paymentInfoText,
                            invoiceNotes = customNotes
                        )
                        val pdfFile = PdfInvoiceGenerator.generateInvoicePdf(context, customSettings, invoiceData)
                        PdfInvoiceGenerator.shareOrPrintPdf(context, pdfFile, "Download / Print Invoice")
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("download_print_pdf_btn")
                ) {
                    Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Download PDF")
                }

                OutlinedButton(
                    onClick = {
                        if (monthProjects.isEmpty()) {
                            Toast.makeText(context, "No completed projects for this client/month to print", Toast.LENGTH_SHORT).show()
                            return@OutlinedButton
                        }
                        val invoiceData = PdfInvoiceGenerator.InvoiceData(
                            invoiceNumber = invoiceNumber,
                            invoiceDate = invoiceDate,
                            billingPeriod = DateUtils.formatDisplayMonthYear(selectedMonth),
                            invoiceTitle = invoiceTitle,
                            clientName = clientName.ifBlank { selectedClientFilter ?: "Client" },
                            clientEmail = clientEmail,
                            clientAddress = clientAddress,
                            projects = monthProjects,
                            currencySymbol = currencySymbol,
                            customNotes = customNotes
                        )
                        val customSettings = settings.copy(
                            paymentInfo = paymentInfoText,
                            invoiceNotes = customNotes
                        )
                        val pdfFile = PdfInvoiceGenerator.generateInvoicePdf(context, customSettings, invoiceData)
                        PdfInvoiceGenerator.openPdfViewer(context, pdfFile)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("print_invoice_btn")
                ) {
                    Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Print / View")
                }
            }
        }

        // Live Professional Invoice Preview Card
        item {
            Text(
                text = "Live Invoice Preview (${monthProjects.size} projects • ${totalMins} min)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("invoice_preview_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Invoice Top Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                text = invoiceTitle.uppercase(Locale.US),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "WorkTrack Invoice",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = invoiceNumber,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Date: ${DateUtils.formatDisplayDate(invoiceDate)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Period: ${DateUtils.formatDisplayMonthYear(selectedMonth)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // From / Billed To Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // From
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp)),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "FROM:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = settings.editorName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                if (settings.editorEmail.isNotBlank()) {
                                    Text(text = settings.editorEmail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (settings.editorPhone.isNotBlank()) {
                                    Text(text = settings.editorPhone, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (settings.editorAddress.isNotBlank()) {
                                    Text(text = settings.editorAddress, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        // Billed To
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp)),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "BILLED TO:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = clientName.ifBlank { selectedClientFilter ?: "Client / Company" },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                if (clientAddress.isNotBlank()) {
                                    Text(text = clientAddress, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Table Header
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("# Project", modifier = Modifier.weight(2f), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text("Category", modifier = Modifier.weight(1.2f), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text("Mins", modifier = Modifier.weight(0.9f), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text("Rate", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text("Amount", modifier = Modifier.weight(1.3f), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    if (monthProjects.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No completed projects for ${DateUtils.formatDisplayMonthYear(selectedMonth)}${if (selectedClientFilter != null) " under $selectedClientFilter" else ""}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        for ((idx, p) in monthProjects.withIndex()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (idx % 2 == 1) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) else Color.Transparent,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(2f)) {
                                    Text(
                                        text = "${idx + 1}. ${p.name}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = DateUtils.formatDisplayDate(p.date),
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(text = p.category, modifier = Modifier.weight(1.2f), fontSize = 11.sp)
                                Text(text = "${if (p.minutes % 1.0 == 0.0) p.minutes.toInt() else p.minutes}m", modifier = Modifier.weight(0.9f), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 11.sp)
                                Text(text = "$currencySymbol${p.ratePerMinute}", modifier = Modifier.weight(1f), fontSize = 11.sp)
                                Text(
                                    text = "$currencySymbol${String.format(Locale.US, "%,.2f", p.payment)}",
                                    modifier = Modifier.weight(1.3f),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Summary Total Box
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp)),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Projects:", style = MaterialTheme.typography.bodySmall)
                                Text("${monthProjects.size}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Video Minutes:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                Text("${totalMins} min", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "GRAND TOTAL:",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "$currencySymbol${String.format(Locale.US, "%,.2f", grandTotal)}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = AccentEmerald
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Payment Information box
                    if (paymentInfoText.isNotBlank()) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp)),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "PAYMENT DETAILS:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = paymentInfoText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    if (customNotes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = customNotes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
