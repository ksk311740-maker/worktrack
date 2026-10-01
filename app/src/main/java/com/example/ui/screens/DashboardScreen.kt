package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ProjectItem
import com.example.model.UserSettings
import com.example.ui.CategorySummary
import com.example.ui.ClientSummary
import com.example.ui.DashboardMetrics
import com.example.ui.components.MetricStatCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AnimatedNumberText
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.ViewfinderMarks
import com.example.ui.theme.bouncyClickable
import com.example.util.DateUtils
import java.util.Locale

@Composable
fun DashboardScreen(
    metrics: DashboardMetrics,
    settings: UserSettings,
    onNavigateToAdd: () -> Unit,
    onNavigateToBulk: () -> Unit,
    onNavigateToInvoice: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onEditProject: (ProjectItem) -> Unit,
    onDuplicateProject: (ProjectItem) -> Unit,
    onOpenQuickAdd: () -> Unit
) {
    val currency = settings.currencySymbol

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_screen")
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Welcome Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "WorkTrack",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Video Editing History & Work Log",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                FilledTonalButton(
                    onClick = onOpenQuickAdd,
                    modifier = Modifier
                        .bouncyClickable { onOpenQuickAdd() }
                        .testTag("dashboard_quick_add_btn")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Quick Add")
                }
            }
        }

        // PRIMARY HERO CARD: TOTAL VIDEO MINUTES (Highlighted with Viewfinder corner marks & animated counter)
        item {
            Box(modifier = Modifier.fillMaxWidth()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("hero_total_minutes_card"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VideoLibrary,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "TOTAL VIDEO MINUTES",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                            ) {
                                Text(
                                    text = "${DateUtils.formatHours(metrics.totalMinutes / 60.0)} runtime",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Giant Minutes Statistic with animated count-up
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AnimatedNumberText(
                                targetValue = metrics.totalMinutes,
                                decimals = if (metrics.totalMinutes % 1.0 == 0.0) 0 else 1,
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "min",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Monthly Minutes Sub-bar with animated count
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
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
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${DateUtils.formatDisplayMonthYear(DateUtils.currentYearMonth())}:",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    AnimatedNumberText(
                                        targetValue = metrics.thisMonthMinutes,
                                        decimals = if (metrics.thisMonthMinutes % 1.0 == 0.0) 0 else 1,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = " min completed",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                // Viewfinder corner marks on hero card
                ViewfinderMarks(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(4.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                )
            }
        }

        // SECONDARY STATS (Smaller cards: Projects, Working Hours, Earnings with Animated Numbers)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // PROJECTS
                MetricStatCard(
                    title = "PROJECTS",
                    numericValue = metrics.totalProjects.toDouble(),
                    subtitle = "${metrics.thisMonthProjects} this mo",
                    icon = Icons.Default.Movie,
                    accentColor = PrimaryIndigo,
                    modifier = Modifier.weight(1f)
                )

                // WORKING HOURS
                MetricStatCard(
                    title = "WORK TIME",
                    value = DateUtils.formatHours(metrics.totalHours),
                    numericValue = if (metrics.totalHours > 0) metrics.totalHours else null,
                    numericDecimals = 1,
                    numericPrefix = "",
                    subtitle = if (metrics.totalHours > 0) "${String.format(Locale.US, "%.1f", metrics.totalHours)}h logged" else "Tracked hours",
                    icon = Icons.Default.AccessTime,
                    accentColor = AccentCyan,
                    modifier = Modifier.weight(1f)
                )

                // EARNINGS (Secondary, clean size)
                MetricStatCard(
                    title = "EARNINGS",
                    numericValue = metrics.totalEarnings,
                    numericPrefix = currency,
                    subtitle = "$currency${String.format(Locale.US, "%,.0f", metrics.thisMonthEarnings)} mo",
                    icon = Icons.Default.AttachMoney,
                    accentColor = AccentEmerald,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Action Buttons Row (Add Project / Bulk Entry) with press feedback
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onNavigateToAdd,
                    modifier = Modifier
                        .weight(1f)
                        .bouncyClickable { onNavigateToAdd() }
                        .testTag("dashboard_add_project_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Project")
                }

                OutlinedButton(
                    onClick = onNavigateToBulk,
                    modifier = Modifier
                        .weight(1f)
                        .bouncyClickable { onNavigateToBulk() }
                        .testTag("dashboard_bulk_entry_btn")
                ) {
                    Icon(imageVector = Icons.Default.PlaylistAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Bulk Entry")
                }
            }
        }

        // Work by Category Breakdown (Prioritizing Video Minutes)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Work by Category",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${metrics.categoryBreakdown.size} categories",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (metrics.categoryBreakdown.isEmpty()) {
                        Text(
                            text = "No category data yet. Categories you create when adding projects will appear here.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        val maxMinutes = metrics.categoryBreakdown.maxOfOrNull { it.totalMinutes } ?: 1.0
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            for (cat in metrics.categoryBreakdown) {
                                val catColor = try {
                                    Color(android.graphics.Color.parseColor(cat.colorHex))
                                } catch (_: Exception) {
                                    MaterialTheme.colorScheme.primary
                                }

                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(catColor)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = cat.category,
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "(${cat.count} projs)",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        // Video Minutes Primary, Earnings Secondary
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "${if (cat.totalMinutes % 1.0 == 0.0) cat.totalMinutes.toInt() else cat.totalMinutes} min",
                                                fontWeight = FontWeight.ExtraBold,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = " • $currency${String.format(Locale.US, "%,.0f", cat.totalEarnings)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    val progress = (cat.totalMinutes / maxMinutes).toFloat().coerceIn(0.05f, 1f)
                                    LinearProgressIndicator(
                                        progress = { progress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = catColor,
                                        trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Work by Client Breakdown (if any client specified)
        if (metrics.clientBreakdown.isNotEmpty()) {
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Business,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Work by Client",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "${metrics.clientBreakdown.size} clients",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            for (client in metrics.clientBreakdown) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = client.client,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "${client.count} projects",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${if (client.totalMinutes % 1.0 == 0.0) client.totalMinutes.toInt() else client.totalMinutes} min",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = AccentCyan
                                        )
                                        Text(
                                            text = " • $currency${String.format(Locale.US, "%,.0f", client.totalEarnings)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Recent Completed Projects
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Projects",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "View All →",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { onNavigateToHistory() }
                        .padding(4.dp)
                )
            }
        }

        if (metrics.recentProjects.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No projects yet", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Add your video projects to start logging minutes and rates!", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(metrics.recentProjects, key = { it.id }) { project ->
                RecentProjectRow(
                    project = project,
                    currency = currency,
                    onEdit = { onEditProject(project) },
                    onDuplicate = { onDuplicateProject(project) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun RecentProjectRow(
    project: ProjectItem,
    currency: String,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .bouncyClickable { onEdit() },
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = project.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    StatusBadge(status = project.status)
                }

                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = DateUtils.formatDisplayDate(project.date),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = " • ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = project.category,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (project.clientName.isNotBlank()) {
                        Text(
                            text = " • ${project.clientName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                // Prominent video duration badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${if (project.minutes % 1.0 == 0.0) project.minutes.toInt() else project.minutes} min",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$currency${String.format(Locale.US, "%,.0f", project.payment)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = AccentEmerald,
                    fontWeight = FontWeight.SemiBold
                )

                Row {
                    IconButton(
                        onClick = onDuplicate,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Duplicate Project",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Project",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    com.example.ui.theme.MyApplicationTheme {
        DashboardScreen(
            metrics = DashboardMetrics(
                totalMinutes = 420.0,
                totalProjects = 35,
                totalHours = 42.0,
                totalEarnings = 25200.0,
                thisMonthMinutes = 180.0,
                thisMonthProjects = 15,
                thisMonthEarnings = 10800.0,
                avgPaymentPerProject = 720.0,
                avgMinutesPerProject = 12.0,
                categoryBreakdown = listOf(
                    CategorySummary("Casino", 12, 74.0, 4440.0, "#10B981"),
                    CategorySummary("Documentary", 10, 41.0, 2870.0, "#6366F1"),
                    CategorySummary("Football", 8, 32.0, 1920.0, "#F59E0B")
                ),
                clientBreakdown = listOf(
                    ClientSummary("Creator Studio", 15, 180.0, 10800.0),
                    ClientSummary("Viral Media", 10, 120.0, 7200.0)
                ),
                recentProjects = listOf(
                    ProjectItem(
                        id = 1,
                        date = "2026-09-30",
                        name = "Football 5",
                        category = "Football",
                        minutes = 5.0,
                        ratePerMinute = 60.0,
                        payment = 300.0,
                        clientName = "Sports Reel",
                        status = ProjectItem.STATUS_COMPLETED
                    ),
                    ProjectItem(
                        id = 2,
                        date = "2026-09-30",
                        name = "Casino 10",
                        category = "Casino",
                        minutes = 10.0,
                        ratePerMinute = 40.0,
                        payment = 400.0,
                        clientName = "Gaming Hub",
                        status = ProjectItem.STATUS_COMPLETED
                    )
                )
            ),
            settings = UserSettings(),
            onNavigateToAdd = {},
            onNavigateToBulk = {},
            onNavigateToInvoice = {},
            onNavigateToHistory = {},
            onEditProject = {},
            onDuplicateProject = {},
            onOpenQuickAdd = {}
        )
    }
}
