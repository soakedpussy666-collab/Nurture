package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
    val id: String = "",
    val userId: String = "",
    val role: String = "parent", // "parent" or "midwife"
    val displayName: String = "",
    val email: String = "",
    val babyId: String? = null,
    val quietHoursEnabled: Boolean = false,
    val subtleNotifications: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Baby(
    val id: String = "",
    val name: String = "",
    val birthDate: String = "", // Format: "YYYY-MM-DD"
    val caregiverUids: List<String> = emptyList(),
    val midwifeUids: List<String> = emptyList(),
    val inviteCode: String = "",
    val lmcInviteCode: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class BabyLog(
    val id: String = "",
    val babyId: String = "",
    val type: String = "feeding", // "feeding", "sleep", "diaper"
    val loggedByUid: String = "",
    val loggedByName: String = "",
    val feedingType: String? = null, // "breast_left", "breast_right", "bottle_formula", "bottle_milk", "solids"
    val amountMl: Long? = null,
    val durationMinutes: Long? = null,
    val sleepQuality: String? = null, // "peaceful", "restless", "contact_nap"
    val diaperType: String? = null, // "wet", "dirty", "both", "dry"
    val consistency: String? = null, // "normal", "loose", "seedy", "meconium"
    val notes: String? = null,
    val memberUids: List<String> = emptyList(),
    val timestamp: Timestamp? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Appointment(
    val id: String = "",
    val babyId: String = "",
    val title: String = "",
    val dateTime: Timestamp? = null,
    val location: String = "Home Visit",
    val notes: String = "",
    val status: String = "scheduled", // "scheduled", "completed", "rescheduled", "cancelled"
    val createdByUid: String = "",
    val midwifeUid: String? = null,
    val memberUids: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ChatMessage(
    val id: String = "",
    val babyId: String = "",
    val senderUid: String = "",
    val senderName: String = "",
    val senderRole: String = "parent", // "parent" or "midwife"
    val text: String = "",
    val memberUids: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null
)
