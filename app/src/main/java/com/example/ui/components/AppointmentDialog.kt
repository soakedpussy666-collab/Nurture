package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import com.example.data.model.Appointment
import com.google.firebase.Timestamp
import java.util.Date

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScheduleAppointmentDialog(
    babyId: String,
    currentUserId: String,
    memberUids: List<String>,
    midwifeUid: String?,
    onDismiss: () -> Unit,
    onSave: (Appointment) -> Unit
) {
    var title by remember { mutableStateOf("Postnatal Wellness Check") }
    var location by remember { mutableStateOf("Home Visit") }
    var notes by remember { mutableStateOf("") }
    var daysAhead by remember { mutableStateOf(3) }

    val presetTitles = listOf(
        "2-Day Newborn Check",
        "1-Week Weight & Feeding",
        "2-Week Wellness Visit",
        "4-Week Check-in",
        "6-Week Postnatal Exam"
    )

    val locations = listOf(
        "Home Visit" to Icons.Default.Home,
        "Clinic Office" to Icons.Default.LocalHospital,
        "Telehealth Call" to Icons.Default.Videocam
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Schedule LMC Visit", style = MaterialTheme.typography.titleLarge)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Visit Type", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presetTitles.forEach { preset ->
                        FilterChip(
                            selected = title == preset,
                            onClick = { title = preset },
                            label = { Text(preset, style = MaterialTheme.typography.bodySmall) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Appointment Title") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("appointment_title_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Location", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    locations.forEach { (loc, icon) ->
                        FilterChip(
                            selected = location == loc,
                            onClick = { location = loc },
                            label = { Text(loc, style = MaterialTheme.typography.bodySmall) },
                            leadingIcon = { Icon(icon, contentDescription = null) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("Timing (Days from now)", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(6.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(1 to "Tomorrow", 3 to "In 3 Days", 7 to "1 Week", 14 to "2 Weeks").forEach { (d, label) ->
                        FilterChip(
                            selected = daysAhead == d,
                            onClick = { daysAhead = d },
                            label = { Text(label) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Clinical Notes / Preparation") },
                    placeholder = { Text("e.g. Weigh baby, check latch, jaundice check") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("appointment_notes_input"),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val targetTimeMillis = System.currentTimeMillis() + (daysAhead * 24L * 60L * 60L * 1000L)
                    val appointment = Appointment(
                        babyId = babyId,
                        title = title.ifBlank { "Midwife Checkup" },
                        dateTime = Timestamp(Date(targetTimeMillis)),
                        location = location,
                        notes = notes,
                        status = "scheduled",
                        createdByUid = currentUserId,
                        midwifeUid = midwifeUid,
                        memberUids = memberUids
                    )
                    onSave(appointment)
                },
                enabled = title.isNotBlank(),
                modifier = Modifier.testTag("confirm_save_appointment")
            ) {
                Text("Schedule Visit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
