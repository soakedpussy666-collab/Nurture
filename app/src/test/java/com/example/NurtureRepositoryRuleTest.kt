package com.example

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.BabyLog
import com.example.data.repository.NurtureRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NurtureRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun testCreateUserProfileAndBabyFlow() = runBlocking {
        val email = "parent_test_${System.currentTimeMillis()}@example.com"
        val uid = signInTestUser(email)
        val repository = NurtureRepository(firestore, auth)

        // 1. Create or get user profile
        val profile = repository.getOrCreateUserProfile(
            uid = uid,
            preferredRole = "parent",
            displayName = "Emma",
            email = email
        )
        assertNotNull(profile)
        assertEquals("parent", profile.role)
        assertEquals("Emma", profile.displayName)

        // 2. Create baby
        val baby = repository.createBaby("Baby Liam", "2026-10-10")
        assertNotNull(baby.id)
        assertEquals("Baby Liam", baby.name)
        assertTrue(baby.caregiverUids.contains(uid))

        // 3. Add feeding log
        val log = BabyLog(
            babyId = baby.id,
            type = "feeding",
            loggedByUid = uid,
            loggedByName = "Emma",
            feedingType = "breast_left",
            durationMinutes = 15,
            memberUids = listOf(uid),
            timestamp = Timestamp.now()
        )
        repository.addBabyLog(log)

        // 4. Observe logs
        val logs = repository.observeBabyLogs(baby.id, uid).first()
        assertTrue(logs.isNotEmpty())
        assertEquals("feeding", logs.first().type)
        assertEquals("breast_left", logs.first().feedingType)
    }
}
