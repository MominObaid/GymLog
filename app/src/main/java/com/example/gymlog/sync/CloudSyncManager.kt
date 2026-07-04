package com.example.gymlog.sync

import com.example.gymlog.RoutineRepository
import com.example.gymlog.auth.AuthManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CloudSyncManager @Inject constructor(
    private val repository: RoutineRepository,
    private val authManager: AuthManager
) {
    private val db = FirebaseFirestore.getInstance()

    suspend fun syncToCloud() {
        val user = authManager.currentUser ?: return
        val profile = repository.getProfile() ?: return

        val userId = user.uid
        val profileId = profile.id

        // Sync Routines
        val routines = repository.getAllRoutines(profileId).first()
        routines.forEach { routine ->
            db.collection("users").document(userId)
                .collection("routines").document(routine.id.toString())
                .set(routine)
        }

        // Sync Sessions
        val sessions = repository.getAllSessions(profileId).first()
        sessions.forEach { session ->
            db.collection("users").document(userId)
                .collection("sessions").document(session.id.toString())
                .set(session)
        }
    }
    
    fun isEnabled(): Boolean = authManager.isUserLoggedIn()
}
