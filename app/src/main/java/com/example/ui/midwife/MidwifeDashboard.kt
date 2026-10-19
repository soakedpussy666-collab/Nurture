package com.example.ui.midwife

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Appointment
import com.example.data.model.Baby
import com.example.data.model.BabyLog
import com.example.ui.components.ConnectClientDialog
import com.example.ui.components.ScheduleAppointmentDialog
import com.example.ui.parent.ActivityLogItem
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MidwifeDashboard(
    currentUserId: String,
    currentUserName: String,
    clients: List<Baby>,
    selectedBaby: Baby?,
    logs: List<BabyLog>,
    appointments: List<Appointment>,
    onSelectBaby: (Baby) -> Unit,
    onConnectClient: (lmcCode: String) -> Unit,
    onScheduleAppointment: (Appointment) -> Unit,
    onUpdateAppointmentStatus: (appointmentId: String, status: String) -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    var showConnectDialog by remember { mutableStateOf(false) }
    var showAppointmentDialog by remember { mutableStateOf(false) }
    var selectedLogFilter by remember { mutableStateOf("all") } // all, feeding, sleep, diaper

    val filteredLogs = remember(logs, selectedLogFilter) {
        if (selectedLogFilter == "all") logs else logs.filter { it.type == selectedLogFilter }
    }

    // Clinical stats calculations for neonatal monitoring
    val feedsToday = logs.count { it.type == "feeding" }
    val wetDiapersToday = logs.count { it.type == "diaper" && (it.diaperType == "wet" || it.diaperType == "both") }
    val dirtyDiapersToday = logs.count { it.type == "diaper" && (it.diaperType == "dirty" || it.diaperType == "both") }
    val totalSleepMinutes = logs.filter { it.type == "sleep" }.sumOf { it.durationMinutes ?: 0L }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MedicalServices,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "LMC Midwife Workspace",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Logged in as $currentUserName",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showConnectDialog = true },
                        modifier = Modifier.testTag("midwife_connect_client_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = "Connect Client via LMC code",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.testTag("midwife_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        if (clients.isEmpty()) {
            // Empty state for midwife
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MedicalServices,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "No Connected Clients Yet",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )

                        Text(
                            text = "Connect with your parent clients to monitor their baby's feeding, sleep, and hydration logs, and book postnatal visits.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )

                        Button(
                            onClick = { showConnectDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("connect_client_empty_button"),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Connect Client via LMC Code")
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Client selection carousel
                item {
                    Text(
                        text = "Active Clients (${clients.size})",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(clients) { client ->
                            val isSelected = selectedBaby?.id == client.id
                            FilterChip(
                                selected = isSelected,
                                onClick = { onSelectBaby(client) },
                                label = {
                                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                        Text(client.name, fontWeight = FontWeight.Bold)
                                        Text("Born ${client.birthDate}", style = MaterialTheme.typography.labelSmall)
                                    }
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.ChildCare,
                                        contentDescription = null,
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                modifier = Modifier.testTag("client_chip_${client.id}")
                            )
                        }
                    }
                }

                if (selectedBaby != null) {
                    // 2. Clinical Vitality Summary Cards (Crucial for neonatal care)
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(1.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${selectedBaby.name}'s Clinical Snapshot",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "Live Logged Data",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    ClinicalMetric(
                                        modifier = Modifier.weight(1f),
                                        label = "Feeds",
                                        value = "$feedsToday",
                                        sub = "total entries",
                                        icon = Icons.Default.Favorite,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    ClinicalMetric(
                                        modifier = Modifier.weight(1f),
                                        label = "Wet Diapers",
                                        value = "$wetDiapersToday",
                                        sub = if (wetDiapersToday >= 5) "Hydrated" else "Monitor closely",
                                        icon = Icons.Default.ChildCare,
                                        tint = if (wetDiapersToday >= 5) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                                    )
                                    ClinicalMetric(
                                        modifier = Modifier.weight(1f),
                                        label = "Dirty Diapers",
                                        value = "$dirtyDiapersToday",
                                        sub = "normal stool",
                                        icon = Icons.Default.ChildCare,
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                    ClinicalMetric(
                                        modifier = Modifier.weight(1f),
                                        label = "Total Sleep",
                                        value = "${totalSleepMinutes / 60}h ${totalSleepMinutes % 60}m",
                                        sub = "logged sleep",
                                        icon = Icons.Default.Nightlight,
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Quick actions for LMC
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = onNavigateToChat,
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("midwife_open_chat_button"),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Chat with Parents")
                                    }

                                    OutlinedButton(
                                        onClick = { showAppointmentDialog = true },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("midwife_book_appointment_button"),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Book Visit")
                                    }
                                }
                            }
                        }
                    }

                    // 3. Appointments Section
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Scheduled Appointments",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TextButton(onClick = { showAppointmentDialog = true }) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("New Visit")
                            }
                        }

                        if (appointments.isEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text(
                                    text = "No appointments scheduled. Tap 'New Visit' to plan a home visit or checkup.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        } else {
                            appointments.forEach { apt ->
                                AppointmentItem(
                                    appointment = apt,
                                    onComplete = { onUpdateAppointmentStatus(apt.id, "completed") }
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                        }
                    }

                    // 4. Clinical Logs Explorer with Filter
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Client Activity Feed",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("all" to "All", "feeding" to "Feeding", "sleep" to "Sleep", "diaper" to "Diapers").forEach { (type, label) ->
                                FilterChip(
                                    selected = selectedLogFilter == type,
                                    onClick = { selectedLogFilter = type },
                                    label = { Text(label) }
                                )
                            }
                        }
                    }

                    if (filteredLogs.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text(
                                    text = "No entries found under this category",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    } else {
                        items(filteredLogs, key = { it.id }) { log ->
                            ActivityLogItem(
                                log = log,
                                onDelete = {} // Read-only for midwife to preserve patient medical record integrity
                            )
                        }
                    }
                }
            }
        }
    }

    // Connect Client Dialog
    if (showConnectDialog) {
        ConnectClientDialog(
            onDismiss = { showConnectDialog = false },
            onConfirm = { code ->
                showConnectDialog = false
                onConnectClient(code)
            }
        )
    }

    // Schedule appointment dialog
    if (showAppointmentDialog && selectedBaby != null) {
        val memberList = (selectedBaby.caregiverUids + selectedBaby.midwifeUids).distinct()
        ScheduleAppointmentDialog(
            babyId = selectedBaby.id,
            currentUserId = currentUserId,
            memberUids = memberList,
            midwifeUid = currentUserId,
            onDismiss = { showAppointmentDialog = false },
            onSave = { apt ->
                showAppointmentDialog = false
                onScheduleAppointment(apt)
            }
        )
    }
}

@Composable
fun ClinicalMetric(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    sub: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: androidx.compose.ui.graphics.Color
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 2.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = sub,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = tint,
                maxLines = 1
            )
        }
    }
}

@Composable
fun AppointmentItem(
    appointment: Appointment,
    onComplete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()) }
    val formattedDate = appointment.dateTime?.toDate()?.let { dateFormat.format(it) } ?: "Scheduled date"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = appointment.title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$formattedDate • ${appointment.location}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (appointment.notes.isNotBlank()) {
                    Text(
                        text = "Notes: ${appointment.notes}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1
                    )
                }
            }

            if (appointment.status == "scheduled") {
                IconButton(onClick = onComplete) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Mark completed",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "Done",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
