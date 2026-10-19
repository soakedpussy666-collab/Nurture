package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Appointment
import com.example.data.model.Baby
import com.example.data.model.BabyLog
import com.example.data.model.ChatMessage
import com.example.data.model.UserProfile
import com.example.data.repository.NurtureRepository
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.performSignOut
import com.example.ui.chat.CareChatScreen
import com.example.ui.midwife.MidwifeDashboard
import com.example.ui.parent.ParentDashboard
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AppRoot()
            }
        }
    }
}

enum class ScreenState {
    DASHBOARD,
    CHAT,
    SETTINGS
}

@Composable
fun AppRoot() {
    var currentUser by remember { mutableStateOf<FirebaseUser?>(FirebaseAuth.getInstance().currentUser) }
    var preferredRoleOnLogin by remember { mutableStateOf("parent") }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        FirebaseAuth.getInstance().addAuthStateListener(listener)
        onDispose {
            FirebaseAuth.getInstance().removeAuthStateListener(listener)
        }
    }

    if (currentUser == null) {
        AuthScreen(
            onAuthSuccess = { role ->
                preferredRoleOnLogin = role
                currentUser = FirebaseAuth.getInstance().currentUser
            }
        )
    } else {
        MainAppContent(
            currentUser = currentUser!!,
            initialRole = preferredRoleOnLogin
        )
    }
}

@Composable
fun MainAppContent(
    currentUser: FirebaseUser,
    initialRole: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember(context) { NurtureRepository(context) }
    val credentialManager = remember { CredentialManager.create(context) }
    val snackbarHostState = remember { SnackbarHostState() }

    var userProfile by remember { mutableStateOf<UserProfile?>(null) }
    var currentScreen by remember { mutableStateOf(ScreenState.DASHBOARD) }

    // Bootstrap user profile in Firestore
    LaunchedEffect(currentUser.uid) {
        try {
            val profile = repository.getOrCreateUserProfile(
                uid = currentUser.uid,
                preferredRole = initialRole,
                displayName = currentUser.displayName ?: "User",
                email = currentUser.email ?: ""
            )
            userProfile = profile
        } catch (e: Exception) {
            snackbarHostState.showSnackbar("Failed to load profile: ${e.localizedMessage}")
        }
    }

    // Observe active profile changes in real-time
    LaunchedEffect(currentUser.uid) {
        repository.observeUserProfile(currentUser.uid).collect { updated ->
            if (updated != null) {
                userProfile = updated
            }
        }
    }

    if (userProfile == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    val isMidwife = userProfile?.role == "midwife"

    // Real-time observations
    val babyId = userProfile?.babyId ?: ""

    // For parent: single baby
    val activeBabyState = remember(babyId) {
        if (babyId.isNotBlank()) repository.observeBaby(babyId) else flowOf(null)
    }.collectAsStateWithLifecycle(initialValue = null)

    // For midwife: client babies list
    val midwifeClientsState = remember(currentUser.uid, isMidwife) {
        if (isMidwife) repository.observeMidwifeBabies(currentUser.uid) else flowOf(emptyList())
    }.collectAsStateWithLifecycle(initialValue = emptyList())

    var midwifeSelectedBaby by remember { mutableStateOf<Baby?>(null) }

    // Default select first midwife client if none selected
    LaunchedEffect(midwifeClientsState.value) {
        if (midwifeSelectedBaby == null && midwifeClientsState.value.isNotEmpty()) {
            midwifeSelectedBaby = midwifeClientsState.value.first()
        }
    }

    val currentContextBaby: Baby? = if (isMidwife) midwifeSelectedBaby else activeBabyState.value
    val contextBabyId = currentContextBaby?.id ?: ""

    // Observe logs for active baby context
    val logsState = remember(contextBabyId, currentUser.uid) {
        if (contextBabyId.isNotBlank()) {
            repository.observeBabyLogs(contextBabyId, currentUser.uid)
        } else {
            flowOf(emptyList())
        }
    }.collectAsStateWithLifecycle(initialValue = emptyList())

    // Observe appointments for active baby context
    val appointmentsState = remember(contextBabyId, currentUser.uid) {
        if (contextBabyId.isNotBlank()) {
            repository.observeAppointments(contextBabyId, currentUser.uid)
        } else {
            flowOf(emptyList())
        }
    }.collectAsStateWithLifecycle(initialValue = emptyList())

    // Observe messages for active baby context
    val messagesState = remember(contextBabyId, currentUser.uid) {
        if (contextBabyId.isNotBlank()) {
            repository.observeMessages(contextBabyId, currentUser.uid)
        } else {
            flowOf(emptyList())
        }
    }.collectAsStateWithLifecycle(initialValue = emptyList())

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentScreen) {
                ScreenState.DASHBOARD -> {
                    if (isMidwife) {
                        MidwifeDashboard(
                            currentUserId = currentUser.uid,
                            currentUserName = userProfile?.displayName ?: "Midwife",
                            clients = midwifeClientsState.value,
                            selectedBaby = currentContextBaby,
                            logs = logsState.value,
                            appointments = appointmentsState.value,
                            onSelectBaby = { baby -> midwifeSelectedBaby = baby },
                            onConnectClient = { code ->
                                scope.launch {
                                    val result = repository.connectBabyByLmcCode(code)
                                    if (result.isSuccess) {
                                        val connected = result.getOrNull()
                                        midwifeSelectedBaby = connected
                                        snackbarHostState.showSnackbar("Client connected successfully!")
                                    } else {
                                        snackbarHostState.showSnackbar(result.exceptionOrNull()?.localizedMessage ?: "Failed to connect client")
                                    }
                                }
                            },
                            onScheduleAppointment = { apt ->
                                scope.launch {
                                    try {
                                        repository.addAppointment(apt)
                                        snackbarHostState.showSnackbar("Appointment scheduled with parents")
                                    } catch (e: Exception) {
                                        snackbarHostState.showSnackbar("Failed to schedule: ${e.localizedMessage}")
                                    }
                                }
                            },
                            onUpdateAppointmentStatus = { aptId, status ->
                                scope.launch {
                                    try {
                                        repository.updateAppointmentStatus(contextBabyId, aptId, status)
                                        snackbarHostState.showSnackbar("Appointment marked $status")
                                    } catch (e: Exception) {
                                        snackbarHostState.showSnackbar("Failed to update status: ${e.localizedMessage}")
                                    }
                                }
                            },
                            onNavigateToChat = { currentScreen = ScreenState.CHAT },
                            onNavigateToSettings = { currentScreen = ScreenState.SETTINGS }
                        )
                    } else {
                        ParentDashboard(
                            currentUserId = currentUser.uid,
                            currentUserName = userProfile?.displayName ?: "Parent",
                            baby = activeBabyState.value,
                            logs = logsState.value,
                            appointments = appointmentsState.value,
                            onCreateBaby = { name, date ->
                                scope.launch {
                                    try {
                                        val newBaby = repository.createBaby(name, date)
                                        userProfile = userProfile?.copy(babyId = newBaby.id)
                                        snackbarHostState.showSnackbar("Baby profile created & synced!")
                                    } catch (e: Exception) {
                                        snackbarHostState.showSnackbar("Failed to create baby: ${e.localizedMessage}")
                                    }
                                }
                            },
                            onJoinBaby = { code ->
                                scope.launch {
                                    val result = repository.joinBabyByInviteCode(code)
                                    if (result.isSuccess) {
                                        val joined = result.getOrNull()
                                        userProfile = userProfile?.copy(babyId = joined?.id)
                                        snackbarHostState.showSnackbar("Connected to ${joined?.name}'s family profile!")
                                    } else {
                                        snackbarHostState.showSnackbar(result.exceptionOrNull()?.localizedMessage ?: "Failed to join")
                                    }
                                }
                            },
                            onAddLog = { log ->
                                scope.launch {
                                    try {
                                        repository.addBabyLog(log)
                                        snackbarHostState.showSnackbar("Activity logged & synced with co-parent")
                                    } catch (e: Exception) {
                                        snackbarHostState.showSnackbar("Failed to log activity: ${e.localizedMessage}")
                                    }
                                }
                            },
                            onDeleteLog = { logId ->
                                scope.launch {
                                    try {
                                        repository.deleteBabyLog(contextBabyId, logId)
                                        snackbarHostState.showSnackbar("Entry removed")
                                    } catch (e: Exception) {
                                        snackbarHostState.showSnackbar("Failed to remove log: ${e.localizedMessage}")
                                    }
                                }
                            },
                            onScheduleAppointment = { apt ->
                                scope.launch {
                                    try {
                                        repository.addAppointment(apt)
                                        snackbarHostState.showSnackbar("Visit requested with Midwife")
                                    } catch (e: Exception) {
                                        snackbarHostState.showSnackbar("Failed to request appointment: ${e.localizedMessage}")
                                    }
                                }
                            },
                            onNavigateToChat = { currentScreen = ScreenState.CHAT },
                            onNavigateToSettings = { currentScreen = ScreenState.SETTINGS }
                        )
                    }
                }

                ScreenState.CHAT -> {
                    BackHandler { currentScreen = ScreenState.DASHBOARD }
                    CareChatScreen(
                        babyName = currentContextBaby?.name ?: "Care Team",
                        currentUserId = currentUser.uid,
                        currentUserName = userProfile?.displayName ?: "User",
                        currentUserRole = userProfile?.role ?: "parent",
                        messages = messagesState.value,
                        onSendMessage = { text ->
                            if (currentContextBaby != null) {
                                val memberList = (currentContextBaby.caregiverUids + currentContextBaby.midwifeUids).distinct()
                                val message = ChatMessage(
                                    babyId = currentContextBaby.id,
                                    senderUid = currentUser.uid,
                                    senderName = userProfile?.displayName ?: "User",
                                    senderRole = userProfile?.role ?: "parent",
                                    text = text,
                                    memberUids = memberList
                                )
                                scope.launch {
                                    try {
                                        repository.sendMessage(message)
                                    } catch (e: Exception) {
                                        snackbarHostState.showSnackbar("Failed to send: ${e.localizedMessage}")
                                    }
                                }
                            }
                        },
                        onBack = { currentScreen = ScreenState.DASHBOARD }
                    )
                }

                ScreenState.SETTINGS -> {
                    BackHandler { currentScreen = ScreenState.DASHBOARD }
                    SettingsScreen(
                        userProfile = userProfile,
                        baby = currentContextBaby,
                        onUpdateSettings = { quietHours, subtleNotif ->
                            userProfile?.let { prof ->
                                val updated = prof.copy(quietHoursEnabled = quietHours, subtleNotifications = subtleNotif)
                                userProfile = updated
                                scope.launch {
                                    try {
                                        repository.updateUserProfile(updated)
                                        snackbarHostState.showSnackbar("Notification preferences saved")
                                    } catch (e: Exception) {
                                        snackbarHostState.showSnackbar("Failed to save settings: ${e.localizedMessage}")
                                    }
                                }
                            }
                        },
                        onSwitchRole = { newRole ->
                            userProfile?.let { prof ->
                                val updated = prof.copy(role = newRole)
                                userProfile = updated
                                scope.launch {
                                    try {
                                        repository.updateUserProfile(updated)
                                        snackbarHostState.showSnackbar("Switched to ${if (newRole == "midwife") "Midwife / LMC" else "Parent"} Mode")
                                    } catch (e: Exception) {
                                        snackbarHostState.showSnackbar("Failed to switch role: ${e.localizedMessage}")
                                    }
                                }
                            }
                        },
                        onSignOut = {
                            performSignOut(
                                context = context,
                                credentialManager = credentialManager,
                                onSignOutComplete = {
                                    currentScreen = ScreenState.DASHBOARD
                                },
                                scope = scope
                            )
                        },
                        onBack = { currentScreen = ScreenState.DASHBOARD }
                    )
                }
            }
        }
    }
}
