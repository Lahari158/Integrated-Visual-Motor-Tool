package com.pdd.app.database

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.pdd.app.SessionResult
import com.pdd.app.UserProfile
import kotlinx.coroutines.tasks.await

/** Cloud storage for the currently authenticated Firebase user only. */
object FirestoreManager {
    private const val TAG = "FirestoreManager"
    private const val PROFILES = "profiles"
    private const val SESSIONS = "sessions"
    private const val LEVELS = "levels"

    private val db get() = FirebaseFirestore.getInstance()
    private val auth get() = FirebaseAuth.getInstance()

    fun keepBackendConnected() {
        try {
            db.enableNetwork()
                .addOnSuccessListener { Log.d(TAG, "Firestore network connected.") }
                .addOnFailureListener { Log.e(TAG, "Firestore network connection failed.", it) }
        } catch (e: Exception) {
            Log.e(TAG, "Error enabling network for Firestore: ${e.message}", e)
        }
    }

    private fun currentUid(): String? {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            Log.w(TAG, "No authenticated user found in FirebaseAuth.")
        }
        return uid
    }

    suspend fun saveUserProfile(profile: UserProfile): Boolean = try {
        val uid = currentUid()
        if (uid.isNullOrEmpty()) {
            Log.e(TAG, "Cannot save profile: User is not authenticated.")
            false
        } else {
            val email = auth.currentUser?.email?.takeIf { it.isNotBlank() } ?: profile.email.trim().lowercase()
            val profileToSave = profile.copy(email = email)
            db.collection(PROFILES).document(uid).set(profileToSave, SetOptions.merge()).await()
            Log.d(TAG, "Profile saved successfully in Firestore for UID: $uid")
            true
        }
    } catch (e: Exception) {
        Log.e(TAG, "Failed to save user profile to Firestore: ${e.message}", e)
        false
    }

    suspend fun getUserProfile(email: String): UserProfile? = try {
        val uid = currentUid()
        if (uid.isNullOrEmpty()) {
            null
        } else {
            val doc = db.collection(PROFILES).document(uid).get().await()
            if (doc.exists()) {
                doc.toObject(UserProfile::class.java)
            } else {
                Log.d(TAG, "No profile document found for UID: $uid")
                null
            }
        }
    } catch (e: Exception) {
        Log.e(TAG, "Unable to load profile from Firestore: ${e.message}", e)
        null
    }

    suspend fun saveSession(email: String, session: SessionResult): Boolean = try {
        val uid = currentUid()
        if (uid.isNullOrEmpty()) {
            Log.e(TAG, "Cannot save session: User is not authenticated.")
            false
        } else {
            db.collection(PROFILES).document(uid).collection(SESSIONS)
                .document(session.id).set(session).await()
            Log.d(TAG, "Session ${session.id} saved to Firestore successfully for UID: $uid")
            true
        }
    } catch (e: Exception) {
        Log.e(TAG, "Failed to save session to Firestore: ${e.message}", e)
        false
    }

    suspend fun getUserSessions(email: String): List<SessionResult> = try {
        val uid = currentUid()
        if (uid.isNullOrEmpty()) {
            emptyList()
        } else {
            val snapshot = db.collection(PROFILES).document(uid).collection(SESSIONS)
                .orderBy("dateMillis", Query.Direction.DESCENDING).get().await()
            val sessions = snapshot.documents.mapNotNull { it.toObject(SessionResult::class.java) }
            Log.d(TAG, "Fetched ${sessions.size} sessions from Firestore for UID: $uid")
            sessions
        }
    } catch (e: Exception) {
        Log.e(TAG, "Unable to load sessions from Firestore: ${e.message}", e)
        emptyList()
    }

    suspend fun deleteSession(email: String, sessionId: String): Boolean = try {
        val uid = currentUid()
        if (!uid.isNullOrEmpty() && sessionId.isNotBlank()) {
            db.collection(PROFILES).document(uid).collection(SESSIONS)
                .document(sessionId).delete().await()
            Log.d(TAG, "Deleted session $sessionId from Firestore")
            true
        } else false
    } catch (e: Exception) {
        Log.e(TAG, "Unable to delete session from Firestore: ${e.message}", e)
        false
    }

    suspend fun saveExerciseLevel(email: String, title: String, level: Int): Boolean = try {
        val uid = currentUid()
        if (!uid.isNullOrEmpty() && title.isNotBlank()) {
            db.collection(PROFILES).document(uid).collection(LEVELS)
                .document(title).set(mapOf("level" to level), SetOptions.merge()).await()
            Log.d(TAG, "Saved exercise level $title = $level to Firestore")
            true
        } else false
    } catch (e: Exception) {
        Log.e(TAG, "Unable to save exercise level to Firestore: ${e.message}", e)
        false
    }

    suspend fun getUserLevels(email: String): Map<String, Int> = try {
        val uid = currentUid()
        if (uid.isNullOrEmpty()) {
            emptyMap()
        } else {
            val snapshot = db.collection(PROFILES).document(uid).collection(LEVELS).get().await()
            snapshot.documents.associate { it.id to (it.getLong("level")?.toInt() ?: 3) }
        }
    } catch (e: Exception) {
        Log.e(TAG, "Unable to load exercise levels from Firestore: ${e.message}", e)
        emptyMap()
    }
}
