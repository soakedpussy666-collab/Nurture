package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LogFeedingDialog(
    onDismiss: () -> Unit,
    onSave: (feedingType: String, amountMl: Long?, durationMinutes: Long?, notes: String) -> Unit
) {
    var selectedType by remember { mutableStateOf("breast_left") }
    var durationMinutes by remember { mutableIntStateOf(15) }
    var amountMl by remember { mutableIntStateOf(60) }
    var notes by remember { mutableStateOf("") }

    val feedingOptions = listOf(
        "breast_left" to "Left Breast",
        "breast_right" to "Right Breast",
        "bottle_milk" to "Expressed Milk",
        "bottle_formula" to "Formula Bottle",
        "solids" to "Solids"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Feeding", style = MaterialTheme.typography.titleLarge)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Method",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    feedingOptions.forEach { (key, label) ->
                        FilterChip(
                            selected = selectedType == key,
                            onClick = { selectedType = key },
                            label = { Text(label) },
                            modifier = Modifier.testTag("feed_chip_$key")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (selectedType.startsWith("breast")) {
                    Text(
                        "Duration: $durationMinutes min",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(onClick = { if (durationMinutes > 1) durationMinutes -= 5 }) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease duration")
                        }
                        listOf(5, 10, 15, 20, 25).forEach { min ->
                            OutlinedButton(
                                onClick = { durationMinutes = min },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.padding(2.dp)
                            ) {
                                Text("${min}m")
                            }
                        }
                        IconButton(onClick = { durationMinutes += 5 }) {
                            Icon(Icons.Default.Add, contentDescription = "Increase duration")
                        }
                    }
                } else if (selectedType.startsWith("bottle")) {
                    Text(
                        "Amount: $amountMl ml",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(onClick = { if (amountMl >= 10) amountMl -= 10 }) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease amount")
                        }
                        listOf(30, 60, 90, 120, 150).forEach { ml ->
                            OutlinedButton(
                                onClick = { amountMl = ml },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.padding(2.dp)
                            ) {
                                Text("${ml}ml")
                            }
                        }
                        IconButton(onClick = { amountMl += 10 }) {
                            Icon(Icons.Default.Add, contentDescription = "Increase amount")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    placeholder = { Text("e.g. Good latch, burped well") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("feeding_notes_input"),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        selectedType,
                        if (selectedType.startsWith("bottle")) amountMl.toLong() else null,
                        if (selectedType.startsWith("breast")) durationMinutes.toLong() else null,
                        notes
                    )
                },
                modifier = Modifier.testTag("save_feeding_button")
            ) {
                Text("Save Feed")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LogSleepDialog(
    onDismiss: () -> Unit,
    onSave: (durationMinutes: Long, quality: String, notes: String) -> Unit
) {
    var durationMinutes by remember { mutableIntStateOf(60) }
    var selectedQuality by remember { mutableStateOf("peaceful") }
    var notes by remember { mutableStateOf("") }

    val qualityOptions = listOf(
        "peaceful" to "Peaceful",
        "restless" to "Restless",
        "contact_nap" to "Contact Nap",
        "bassinet" to "Bassinet"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Nightlight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Sleep", style = MaterialTheme.typography.titleLarge)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Nap Duration: $durationMinutes min", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(30, 45, 60, 90, 120).forEach { min ->
                        OutlinedButton(
                            onClick = { durationMinutes = min },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("${min}m")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("Sleep Style / Quality", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    qualityOptions.forEach { (key, label) ->
                        FilterChip(
                            selected = selectedQuality == key,
                            onClick = { selectedQuality = key },
                            label = { Text(label) },
                            modifier = Modifier.testTag("sleep_chip_$key")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    placeholder = { Text("e.g. Swaddled with white noise") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sleep_notes_input"),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(durationMinutes.toLong(), selectedQuality, notes)
                },
                modifier = Modifier.testTag("save_sleep_button")
            ) {
                Text("Save Sleep")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LogDiaperDialog(
    onDismiss: () -> Unit,
    onSave: (diaperType: String, consistency: String, notes: String) -> Unit
) {
    var diaperType by remember { mutableStateOf("wet") }
    var consistency by remember { mutableStateOf("normal") }
    var notes by remember { mutableStateOf("") }

    val typeOptions = listOf(
        "wet" to "💧 Wet",
        "dirty" to "💩 Dirty",
        "both" to "💧+💩 Both",
        "dry" to "Dry Check"
    )

    val consistencyOptions = listOf(
        "normal" to "Normal",
        "seedy" to "Seedy (Breastfed)",
        "loose" to "Loose",
        "meconium" to "Meconium"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ChildCare,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Diaper Change", style = MaterialTheme.typography.titleLarge)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Diaper Content", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    typeOptions.forEach { (key, label) ->
                        FilterChip(
                            selected = diaperType == key,
                            onClick = { diaperType = key },
                            label = { Text(label) },
                            modifier = Modifier.testTag("diaper_chip_$key")
                        )
                    }
                }

                if (diaperType == "dirty" || diaperType == "both") {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Consistency (LMC Hydration/Digestion Info)", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(6.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        consistencyOptions.forEach { (key, label) ->
                            FilterChip(
                                selected = consistency == key,
                                onClick = { consistency = key },
                                label = { Text(label) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    placeholder = { Text("e.g. Yellow mustard color, no rash") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("diaper_notes_input"),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(diaperType, consistency, notes)
                },
                modifier = Modifier.testTag("save_diaper_button")
            ) {
                Text("Save Diaper")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun CreateBabyDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, birthDate: String) -> Unit
) {
    var babyName by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("2026-10-15") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Welcome your Baby", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column {
                Text(
                    "Set up your baby profile to track daily feeding, sleep, and share live updates with family and your Midwife.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = babyName,
                    onValueChange = { babyName = it },
                    label = { Text("Baby's Name or Nickname") },
                    placeholder = { Text("e.g. Leo") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("baby_name_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = birthDate,
                    onValueChange = { birthDate = it },
                    label = { Text("Birth Date (YYYY-MM-DD)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("birth_date_input"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (babyName.isNotBlank()) onConfirm(babyName, birthDate)
                },
                enabled = babyName.isNotBlank(),
                modifier = Modifier.testTag("confirm_create_baby")
            ) {
                Text("Create Profile")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun JoinBabyDialog(
    onDismiss: () -> Unit,
    onConfirm: (code: String) -> Unit
) {
    var inviteCode by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Join as Co-Parent / Caregiver") },
        text = {
            Column {
                Text(
                    "Enter the Family Invite Code provided by the other parent (e.g. FAM-AB12CD) to instantly sync your baby's logs in real-time.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = inviteCode,
                    onValueChange = { inviteCode = it.uppercase() },
                    label = { Text("Family Invite Code") },
                    placeholder = { Text("FAM-...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("invite_code_input"),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (inviteCode.isNotBlank()) onConfirm(inviteCode) },
                enabled = inviteCode.isNotBlank(),
                modifier = Modifier.testTag("confirm_join_baby")
            ) {
                Text("Connect")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun ConnectClientDialog(
    onDismiss: () -> Unit,
    onConfirm: (code: String) -> Unit
) {
    var lmcCode by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Connect New Client (Midwife)") },
        text = {
            Column {
                Text(
                    "Enter the client's LMC Code (e.g. LMC-XY987Z) shared by the parents to access clinical tracking, book visits, and chat securely.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = lmcCode,
                    onValueChange = { lmcCode = it.uppercase() },
                    label = { Text("Client LMC Code") },
                    placeholder = { Text("LMC-...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("lmc_code_input"),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (lmcCode.isNotBlank()) onConfirm(lmcCode) },
                enabled = lmcCode.isNotBlank(),
                modifier = Modifier.testTag("confirm_connect_lmc")
            ) {
                Text("Link Client")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
