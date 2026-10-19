package com.example.ui.parent

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Appointment
import com.example.data.model.Baby
import com.example.data.model.BabyLog
import com.example.ui.components.CreateBabyDialog
import com.example.ui.components.JoinBabyDialog
import com.example.ui.components.LogDiaperDialog
import com.example.ui.components.LogFeedingDialog
import com.example.ui.components.LogSleepDialog
import com.example.ui.components.ScheduleAppointmentDialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentDashboard(
    currentUserId: String,
    currentUserName: String,
    baby: Baby?,
    logs: List<BabyLog>,
    appointments: List<Appointment>,
    onCreateBaby: (name: String, birthDate: String) -> Unit,
    onJoinBaby: (code: String) -> Unit,
    onAddLog: (BabyLog) -> Unit,
    onDeleteLog: (logId: String) -> Unit,
    onScheduleAppointment: (Appointment) -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    var showFeedDialog by remember { mutableStateOf(false) }
    var showSleepDialog by remember { mutableStateOf(false) }
    var showDiaperDialog by remember { mutableStateOf(false) }
    var showCreateBabyDialog by remember { mutableStateOf(false) }
    var showJoinBabyDialog by remember { mutableStateOf(false) }
    var showAppointmentDialog by remember { mutableStateOf(false) }

    val lastFeed = logs.firstOrNull { it.type == "feeding" }
    val lastSleep = logs.firstOrNull { it.type == "sleep" }
    val lastDiaper = logs.firstOrNull { it.type == "diaper" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = baby?.name ?: "Nurture",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CloudDone,
                                        contentDescription = "Synced",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Synced",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                        if (baby != null && baby.birthDate.isNotBlank()) {
                            Text(
                                text = "Born ${baby.birthDate} • Co-parent synced",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToChat,
                        modifier = Modifier.testTag("parent_chat_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = "Care Chat with Midwife",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.testTag("parent_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        if (baby == null) {
            // First time onboarding setup for parents
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
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChildCare,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Welcome to Nurture",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = "Start by creating a profile for your baby or connect with your partner's existing profile.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )

                        Button(
                            onClick = { showCreateBabyDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("create_baby_profile_button"),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Create Baby Profile")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = { showJoinBabyDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("join_baby_profile_button"),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("I have a Family Code to join")
                        }
                    }
                }
            }
        } else {
            // Main clean parent dashboard
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Clean minimal 3-card overview
                item {
                    Text(
                        text = "Recent Status",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Favorite,
                            title = "Feeding",
                            mainText = formatTimeAgo(lastFeed?.timestamp),
                            subText = lastFeed?.let { feedSummary(it) } ?: "No feed logged",
                            onClick = { showFeedDialog = true }
                        )
                        MetricCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Nightlight,
                            title = "Sleep",
                            mainText = formatTimeAgo(lastSleep?.timestamp),
                            subText = lastSleep?.let { "${it.durationMinutes ?: 45}m nap" } ?: "No nap logged",
                            onClick = { showSleepDialog = true }
                        )
                        MetricCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.ChildCare,
                            title = "Diaper",
                            mainText = formatTimeAgo(lastDiaper?.timestamp),
                            subText = lastDiaper?.diaperType?.replaceFirstChar { it.uppercase() } ?: "No change logged",
                            onClick = { showDiaperDialog = true }
                        )
                    }
                }

                // 2. Quick Action Log Buttons
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showFeedDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("quick_log_feeding_button"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Feed")
                        }

                        FilledTonalButton(
                            onClick = { showSleepDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("quick_log_sleep_button"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sleep")
                        }

                        FilledTonalButton(
                            onClick = { showDiaperDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("quick_log_diaper_button"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Diaper")
                        }
                    }
                }

                // 3. LMC Midwife Integration Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
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
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MedicalServices,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Lead Maternity Carer (LMC)",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                                        )
                                        Text(
                                            text = if (baby.midwifeUids.isNotEmpty()) "Midwife connected & reviewing logs" else "Share code with midwife",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                if (baby.midwifeUids.isNotEmpty()) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Connected",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Next upcoming appointment if any
                            val nextApt = appointments.firstOrNull { it.status == "scheduled" }
                            if (nextApt != null) {
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarMonth,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = nextApt.title,
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                                            )
                                            Text(
                                                text = "${nextApt.location} • In a few days",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = onNavigateToChat,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Chat LMC")
                                }

                                Button(
                                    onClick = { showAppointmentDialog = true },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Schedule")
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // LMC Connect Code
                            Text(
                                text = "Midwife Connect Code: ${baby.lmcInviteCode}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }
                    }
                }

                // 4. Collaborative Caregiver Sync Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.People,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Caregivers Synced (${baby.caregiverUids.size})",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                    Text(
                                        text = "Family Code: ${baby.inviteCode}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Text(
                                text = "Real-time",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // 5. Activity Log List
                item {
                    Text(
                        text = "Activity Timeline",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (logs.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No activities logged yet today",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Tap Feed, Sleep, or Diaper above to start",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                } else {
                    items(logs, key = { it.id }) { log ->
                        ActivityLogItem(
                            log = log,
                            onDelete = { onDeleteLog(log.id) }
                        )
                    }
                }
            }
        }
    }

    // Dialogs
    if (showFeedDialog && baby != null) {
        val memberList = (baby.caregiverUids + baby.midwifeUids).distinct()
        LogFeedingDialog(
            onDismiss = { showFeedDialog = false },
            onSave = { type, amount, duration, notes ->
                showFeedDialog = false
                val newLog = BabyLog(
                    babyId = baby.id,
                    type = "feeding",
                    loggedByUid = currentUserId,
                    loggedByName = currentUserName,
                    feedingType = type,
                    amountMl = amount,
                    durationMinutes = duration,
                    notes = notes,
                    memberUids = memberList,
                    timestamp = com.google.firebase.Timestamp.now()
                )
                onAddLog(newLog)
            }
        )
    }

    if (showSleepDialog && baby != null) {
        val memberList = (baby.caregiverUids + baby.midwifeUids).distinct()
        LogSleepDialog(
            onDismiss = { showSleepDialog = false },
            onSave = { duration, quality, notes ->
                showSleepDialog = false
                val newLog = BabyLog(
                    babyId = baby.id,
                    type = "sleep",
                    loggedByUid = currentUserId,
                    loggedByName = currentUserName,
                    durationMinutes = duration,
                    sleepQuality = quality,
                    notes = notes,
                    memberUids = memberList,
                    timestamp = com.google.firebase.Timestamp.now()
                )
                onAddLog(newLog)
            }
        )
    }

    if (showDiaperDialog && baby != null) {
        val memberList = (baby.caregiverUids + baby.midwifeUids).distinct()
        LogDiaperDialog(
            onDismiss = { showDiaperDialog = false },
            onSave = { type, consistency, notes ->
                showDiaperDialog = false
                val newLog = BabyLog(
                    babyId = baby.id,
                    type = "diaper",
                    loggedByUid = currentUserId,
                    loggedByName = currentUserName,
                    diaperType = type,
                    consistency = consistency,
                    notes = notes,
                    memberUids = memberList,
                    timestamp = com.google.firebase.Timestamp.now()
                )
                onAddLog(newLog)
            }
        )
    }

    if (showCreateBabyDialog) {
        CreateBabyDialog(
            onDismiss = { showCreateBabyDialog = false },
            onConfirm = { name, date ->
                showCreateBabyDialog = false
                onCreateBaby(name, date)
            }
        )
    }

    if (showJoinBabyDialog) {
        JoinBabyDialog(
            onDismiss = { showJoinBabyDialog = false },
            onConfirm = { code ->
                showJoinBabyDialog = false
                onJoinBaby(code)
            }
        )
    }

    if (showAppointmentDialog && baby != null) {
        val memberList = (baby.caregiverUids + baby.midwifeUids).distinct()
        ScheduleAppointmentDialog(
            babyId = baby.id,
            currentUserId = currentUserId,
            memberUids = memberList,
            midwifeUid = baby.midwifeUids.firstOrNull(),
            onDismiss = { showAppointmentDialog = false },
            onSave = { apt ->
                showAppointmentDialog = false
                onScheduleAppointment(apt)
            }
        )
    }
}

@Composable
fun MetricCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    mainText: String,
    subText: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = mainText,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = subText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
fun ActivityLogItem(
    log: BabyLog,
    onDelete: () -> Unit
) {
    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val formattedTime = log.timestamp?.toDate()?.let { timeFormat.format(it) } ?: "Recently"

    val (icon, tint) = when (log.type) {
        "feeding" -> Icons.Default.Favorite to MaterialTheme.colorScheme.primary
        "sleep" -> Icons.Default.Nightlight to MaterialTheme.colorScheme.secondary
        else -> Icons.Default.ChildCare to MaterialTheme.colorScheme.tertiary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
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
                    .background(tint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = when (log.type) {
                            "feeding" -> feedSummary(log)
                            "sleep" -> "${log.durationMinutes ?: 60}m Sleep (${log.sleepQuality ?: "Peaceful"})"
                            "diaper" -> "Diaper (${log.diaperType ?: "Wet"})"
                            else -> "Event"
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text(
                        text = "by ${log.loggedByName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (!log.notes.isNullOrBlank()) {
                        Text(
                            text = " • ${log.notes}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete log",
                    tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

private fun feedSummary(log: BabyLog): String {
    return when (log.feedingType) {
        "breast_left" -> "Left Breast (${log.durationMinutes ?: 15}m)"
        "breast_right" -> "Right Breast (${log.durationMinutes ?: 15}m)"
        "bottle_milk" -> "Milk Bottle (${log.amountMl ?: 60}ml)"
        "bottle_formula" -> "Formula Bottle (${log.amountMl ?: 60}ml)"
        "solids" -> "Solids Feeding"
        else -> "Feeding"
    }
}

private fun formatTimeAgo(timestamp: com.google.firebase.Timestamp?): String {
    if (timestamp == null) return "Never"
    val diffSeconds = (Date().time - timestamp.toDate().time) / 1000
    return when {
        diffSeconds < 60 -> "Just now"
        diffSeconds < 3600 -> "${diffSeconds / 60}m ago"
        diffSeconds < 86400 -> "${diffSeconds / 3600}h ago"
        else -> "${diffSeconds / 86400}d ago"
    }
}
