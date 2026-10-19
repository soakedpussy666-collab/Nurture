package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.Appointment
import com.example.data.model.Baby
import com.example.data.model.BabyLog
import com.example.data.model.ChatMessage
import com.example.data.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class NurtureRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        ),
        FirebaseAuth.getInstance()
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    suspend fun getOrCreateUserProfile(
        uid: String,
        preferredRole: String,
        displayName: String,
        email: String
    ): UserProfile {
        val docRef = db.collection("users").document(uid)
        val snapshot = try {
            docRef.get().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            throw e
        }

        if (snapshot.exists()) {
            val profile = snapshot.toObject(UserProfile::class.java)
            if (profile != null) return profile
        }

        // Create new user profile
        val newProfile = UserProfile(
            id = uid,
            userId = uid,
            role = preferredRole,
            displayName = displayName.ifBlank { "Parent" },
            email = email.ifBlank { "caregiver@nurture.app" },
            babyId = null,
            quietHoursEnabled = false,
            subtleNotifications = true
        )

        val payload = mapOf(
            "userId" to newProfile.userId,
            "role" to newProfile.role,
            "displayName" to newProfile.displayName,
            "email" to newProfile.email,
            "babyId" to newProfile.babyId,
            "quietHoursEnabled" to newProfile.quietHoursEnabled,
            "subtleNotifications" to newProfile.subtleNotifications,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        ).filterValues { it != null }

        try {
            docRef.set(payload).await()
            return newProfile
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    fun observeUserProfile(uid: String): Flow<UserProfile?> = callbackFlow {
        val docRef = db.collection("users").document(uid)
        val registration = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.GET, docRef.path)
                close(error)
                return@addSnapshotListener
            }
            val profile = snapshot?.toObject(UserProfile::class.java)
            trySend(profile)
        }
        awaitClose { registration.remove() }
    }

    suspend fun updateUserProfile(profile: UserProfile) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        val updates = mapOf(
            "displayName" to profile.displayName,
            "role" to profile.role,
            "babyId" to profile.babyId,
            "quietHoursEnabled" to profile.quietHoursEnabled,
            "subtleNotifications" to profile.subtleNotifications,
            "updatedAt" to FieldValue.serverTimestamp()
        ).filterValues { it != null }

        try {
            docRef.update(updates).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun createBaby(name: String, birthDate: String): Baby {
        val uid = requireUserId()
        val babyId = "baby_" + UUID.randomUUID().toString().take(8)
        val inviteCode = "FAM-" + UUID.randomUUID().toString().take(6).uppercase()
        val lmcInviteCode = "LMC-" + UUID.randomUUID().toString().take(6).uppercase()

        val babyDoc = db.collection("babies").document(babyId)
        val payload = mapOf(
            "id" to babyId,
            "name" to name.trim(),
            "birthDate" to birthDate.trim(),
            "caregiverUids" to listOf(uid),
            "midwifeUids" to emptyList<String>(),
            "inviteCode" to inviteCode,
            "lmcInviteCode" to lmcInviteCode,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        try {
            babyDoc.set(payload).await()
            // Link baby to user profile
            val userDoc = db.collection("users").document(uid)
            userDoc.update(
                mapOf(
                    "babyId" to babyId,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            return Baby(
                id = babyId,
                name = name,
                birthDate = birthDate,
                caregiverUids = listOf(uid),
                midwifeUids = emptyList(),
                inviteCode = inviteCode,
                lmcInviteCode = lmcInviteCode
            )
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, babyDoc.path)
            throw e
        }
    }

    fun observeBaby(babyId: String): Flow<Baby?> = callbackFlow {
        val docRef = db.collection("babies").document(babyId)
        val registration = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.GET, docRef.path)
                close(error)
                return@addSnapshotListener
            }
            val baby = snapshot?.toObject(Baby::class.java)
            trySend(baby)
        }
        awaitClose { registration.remove() }
    }

    suspend fun joinBabyByInviteCode(inviteCode: String): Result<Baby> {
        val uid = requireUserId()
        val cleanCode = inviteCode.trim().uppercase()
        return try {
            val querySnapshot = db.collection("babies")
                .whereEqualTo("inviteCode", cleanCode)
                .limit(1)
                .get()
                .await()

            if (querySnapshot.isEmpty) {
                return Result.failure(IllegalArgumentException("No baby record found with code $cleanCode"))
            }

            val doc = querySnapshot.documents.first()
            val baby = doc.toObject(Baby::class.java)
                ?: return Result.failure(IllegalStateException("Failed to parse baby details"))

            if (!baby.caregiverUids.contains(uid)) {
                val updatedCaregivers = baby.caregiverUids + uid
                doc.reference.update(
                    mapOf(
                        "caregiverUids" to updatedCaregivers,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }

            // Link baby to user profile
            db.collection("users").document(uid).update(
                mapOf(
                    "babyId" to baby.id,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            Result.success(baby.copy(caregiverUids = baby.caregiverUids + uid))
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "babies")
            Result.failure(e)
        }
    }

    suspend fun connectBabyByLmcCode(lmcCode: String): Result<Baby> {
        val uid = requireUserId()
        val cleanCode = lmcCode.trim().uppercase()
        return try {
            val querySnapshot = db.collection("babies")
                .whereEqualTo("lmcInviteCode", cleanCode)
                .limit(1)
                .get()
                .await()

            if (querySnapshot.isEmpty) {
                return Result.failure(IllegalArgumentException("No client found for code $cleanCode"))
            }

            val doc = querySnapshot.documents.first()
            val baby = doc.toObject(Baby::class.java)
                ?: return Result.failure(IllegalStateException("Failed to parse client record"))

            if (!baby.midwifeUids.contains(uid)) {
                val updatedMidwives = baby.midwifeUids + uid
                doc.reference.update(
                    mapOf(
                        "midwifeUids" to updatedMidwives,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }

            Result.success(baby.copy(midwifeUids = baby.midwifeUids + uid))
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "babies")
            Result.failure(e)
        }
    }

    fun observeMidwifeBabies(midwifeUid: String): Flow<List<Baby>> = flow {
        val path = "babies"
        emitAll(
            db.collection(path)
                .whereArrayContains("midwifeUids", midwifeUid)
                .snapshots()
                .map { snapshot -> snapshot.toObjects(Baby::class.java) }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    fun observeBabyLogs(babyId: String, currentUid: String): Flow<List<BabyLog>> = flow {
        val path = "babies/$babyId/logs"
        emitAll(
            db.collection("babies").document(babyId).collection("logs")
                .whereArrayContains("memberUids", currentUid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .snapshots()
                .map { snapshot -> snapshot.toObjects(BabyLog::class.java) }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun addBabyLog(log: BabyLog) {
        val uid = requireUserId()
        val logId = if (log.id.isNotBlank()) log.id else "log_" + UUID.randomUUID().toString().take(10)
        val docRef = db.collection("babies").document(log.babyId).collection("logs").document(logId)

        val payload = mapOf(
            "id" to logId,
            "babyId" to log.babyId,
            "type" to log.type,
            "loggedByUid" to uid,
            "loggedByName" to log.loggedByName.ifBlank { "Caregiver" },
            "feedingType" to log.feedingType,
            "amountMl" to log.amountMl,
            "durationMinutes" to log.durationMinutes,
            "sleepQuality" to log.sleepQuality,
            "diaperType" to log.diaperType,
            "consistency" to log.consistency,
            "notes" to log.notes,
            "memberUids" to log.memberUids,
            "timestamp" to (log.timestamp ?: FieldValue.serverTimestamp()),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        ).filterValues { it != null }

        try {
            docRef.set(payload).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteBabyLog(babyId: String, logId: String) {
        val docRef = db.collection("babies").document(babyId).collection("logs").document(logId)
        try {
            docRef.delete().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    fun observeAppointments(babyId: String, currentUid: String): Flow<List<Appointment>> = flow {
        val path = "babies/$babyId/appointments"
        emitAll(
            db.collection("babies").document(babyId).collection("appointments")
                .whereArrayContains("memberUids", currentUid)
                .orderBy("dateTime", Query.Direction.ASCENDING)
                .snapshots()
                .map { snapshot -> snapshot.toObjects(Appointment::class.java) }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun addAppointment(appointment: Appointment) {
        val uid = requireUserId()
        val appointmentId = if (appointment.id.isNotBlank()) appointment.id else "apt_" + UUID.randomUUID().toString().take(10)
        val docRef = db.collection("babies").document(appointment.babyId).collection("appointments").document(appointmentId)

        val payload = mapOf(
            "id" to appointmentId,
            "babyId" to appointment.babyId,
            "title" to appointment.title.trim(),
            "dateTime" to (appointment.dateTime ?: FieldValue.serverTimestamp()),
            "location" to appointment.location.trim(),
            "notes" to appointment.notes.trim(),
            "status" to appointment.status,
            "createdByUid" to uid,
            "midwifeUid" to appointment.midwifeUid,
            "memberUids" to appointment.memberUids,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        ).filterValues { it != null }

        try {
            docRef.set(payload).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updateAppointmentStatus(babyId: String, appointmentId: String, status: String) {
        val docRef = db.collection("babies").document(babyId).collection("appointments").document(appointmentId)
        val updates = mapOf(
            "status" to status,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.update(updates).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    fun observeMessages(babyId: String, currentUid: String): Flow<List<ChatMessage>> = flow {
        val path = "babies/$babyId/messages"
        emitAll(
            db.collection("babies").document(babyId).collection("messages")
                .whereArrayContains("memberUids", currentUid)
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .snapshots()
                .map { snapshot -> snapshot.toObjects(ChatMessage::class.java) }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun sendMessage(message: ChatMessage) {
        val uid = requireUserId()
        val messageId = if (message.id.isNotBlank()) message.id else "msg_" + UUID.randomUUID().toString().take(10)
        val docRef = db.collection("babies").document(message.babyId).collection("messages").document(messageId)

        val payload = mapOf(
            "id" to messageId,
            "babyId" to message.babyId,
            "senderUid" to uid,
            "senderName" to message.senderName.ifBlank { "User" },
            "senderRole" to message.senderRole,
            "text" to message.text.trim(),
            "memberUids" to message.memberUids,
            "createdAt" to FieldValue.serverTimestamp()
        ).filterValues { it != null }

        try {
            docRef.set(payload).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }
}
