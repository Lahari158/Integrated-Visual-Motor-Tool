package com.pdd.app

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.pdd.app.R
import com.pdd.app.database.FirestoreManager
import com.pdd.app.ui.theme.PddAppTheme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent { PddAppTheme(dynamicColor = false) { PddApp() } }
    }
}

// ── Ultra High-Contrast Theme Colors ────────────────────────────────────
private val Ink = Color(0xFF0A1F13)         // Deep charcoal green for titles
private val CardWhite = Color(0xFFFFFFFF)   // Solid white card containers
private val Blue = Color(0xFF1565C0)        // Deep sapphire blue
private val Green = Color(0xFF1B6E32)       // Vibrant medical green
private val Muted = Color(0xFF223E2E)       // Dark forest slate for body text
private val FieldBg = Color(0xFFF0F7F1)     // Light green tint
private val CardBorder = Color(0xFFC8E6C9)  // Crisp green card border
private val Serif = FontFamily.Serif

// ── Models for User Profiles & Saved Exercise Sessions ───────────────────
data class UserProfile(
    var fullName: String = "",
    var username: String = "",
    var email: String = "",
    var phone: String = "",
    var ageGender: String = "24 Years • Male",
    var therapyGoal: String = "Visual focus stabilization & motor tracking",
    var therapistName: String = "Dr. A. Rajesh (Neuro-Ophthalmology)",
    var emergencyContact: String = "+91 98765 00000 (Parent)"
)

data class SessionResult(
    var id: String = UUID.randomUUID().toString(),
    var exerciseTitle: String = "Visual Focus Training",
    var timestamp: String = SimpleDateFormat("EEE, MMM dd • hh:mm a", Locale.getDefault()).format(Date()),
    var dayOfWeek: String = SimpleDateFormat("EEE", Locale.US).format(Date()),
    var dateMillis: Long = System.currentTimeMillis(),
    var hits: Int = 0,
    var totalTargets: Int = 0,
    var accuracyPct: Int = 0,
    var avgReactionMs: Int = 0,
    var durationSeconds: Int = 30,
    var level: Int = 3,
    var levelLabel: String = "Level 3 • Medium"
)

// ── Performance Level Model & Real-Time Performance Configuration ────────────
data class LevelInfo(
    val level: Int,
    val name: String,
    val description: String,
    val color: Color,
    val speedMultiplier: Float,
    val targetSizeDp: Int,
    val roundCount: Int,
    val timeLimitMs: Long
)

fun getLevelDetails(level: Int): LevelInfo {
    return when (level.coerceIn(1, 5)) {
        1 -> LevelInfo(
            level = 1,
            name = "Easy",
            description = "Novice pace • Gentle movement, large touch targets & relaxed timing.",
            color = Color(0xFF2E7D32), // Medical Green
            speedMultiplier = 0.65f,
            targetSizeDp = 88,
            roundCount = 6,
            timeLimitMs = 2500L
        )
        2 -> LevelInfo(
            level = 2,
            name = "Light Easy",
            description = "Light pace • Steady visual cues & moderate target sizing.",
            color = Color(0xFF00838F), // Cyan / Teal
            speedMultiplier = 0.85f,
            targetSizeDp = 76,
            roundCount = 8,
            timeLimitMs = 2000L
        )
        3 -> LevelInfo(
            level = 3,
            name = "Medium",
            description = "Standard clinical pace • Balanced motor-visual response targets.",
            color = Color(0xFF1565C0), // Sapphire Blue
            speedMultiplier = 1.0f,
            targetSizeDp = 64,
            roundCount = 10,
            timeLimitMs = 1500L
        )
        4 -> LevelInfo(
            level = 4,
            name = "Hard",
            description = "Advanced pace • High-speed target jumps & precision hit boxes.",
            color = Color(0xFFE65100), // Deep Orange
            speedMultiplier = 1.35f,
            targetSizeDp = 50,
            roundCount = 12,
            timeLimitMs = 1000L
        )
        5 -> LevelInfo(
            level = 5,
            name = "Very Hard",
            description = "Master pace • High speed, rapid reflex & micro target accuracy.",
            color = Color(0xFFC62828), // Crimson Red
            speedMultiplier = 1.75f,
            targetSizeDp = 38,
            roundCount = 15,
            timeLimitMs = 600L
        )
        else -> LevelInfo(
            level = 3,
            name = "Medium",
            description = "Standard clinical pace.",
            color = Color(0xFF1565C0),
            speedMultiplier = 1.0f,
            targetSizeDp = 64,
            roundCount = 10,
            timeLimitMs = 1500L
        )
    }
}

@Composable
private fun ContainerLevelBadge(
    level: Int,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val info = getLevelDetails(level)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(info.color.copy(alpha = 0.12f))
            .border(1.dp, info.color.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 9.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "⚡ L${info.level} • ${info.name}",
                style = TextStyle(
                    fontFamily = Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = info.color
                )
            )
        }
    }
}

@Composable
private fun RealtimeLevelSelector(
    selectedLevel: Int,
    onLevelSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentInfo = getLevelDetails(selectedLevel)
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, CardBorder, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("⚡ Real-Time Performance Mode", style = titleStyle(14.sp, color = Ink))
                ContainerLevelBadge(selectedLevel)
            }
            Spacer(Modifier.height(6.dp))
            Text(currentInfo.description, style = bodyStyle(12.sp, color = Muted))
            Spacer(Modifier.height(12.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                (1..5).forEach { lvl ->
                    val info = getLevelDetails(lvl)
                    val isSelected = (lvl == selectedLevel)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) info.color else FieldBg)
                            .border(1.dp, if (isSelected) info.color else CardBorder, RoundedCornerShape(12.dp))
                            .clickable { onLevelSelected(lvl) },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "L$lvl",
                                style = TextStyle(
                                    fontFamily = Serif,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (isSelected) Color.White else Ink
                                )
                            )
                            Text(
                                text = when (lvl) {
                                    1 -> "Easy"
                                    2 -> "Light"
                                    3 -> "Med"
                                    4 -> "Hard"
                                    else -> "V.Hard"
                                },
                                style = TextStyle(
                                    fontFamily = Serif,
                                    fontSize = 9.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White.copy(0.92f) else Muted
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Real-Time Persistent Storage Manager ────────────────────────────────
object AppStorageManager {
    private const val PREF_NAME = "pdd_app_realtime_db"

    fun saveUser(context: Context, email: String, pass: String, profile: UserProfile) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val profilesJson = prefs.getString("profiles_map", "{}") ?: "{}"
        val profilesObj = JSONObject(profilesJson)
        val profJson = JSONObject().apply {
            put("fullName", profile.fullName)
            put("username", profile.username)
            put("email", profile.email)
            put("phone", profile.phone)
            put("ageGender", profile.ageGender)
            put("therapyGoal", profile.therapyGoal)
            put("therapistName", profile.therapistName)
            put("emergencyContact", profile.emergencyContact)
        }
        profilesObj.put(email, profJson)

        prefs.edit()
            .remove("users_map") // Firebase Auth owns credentials; never keep passwords locally.
            .putString("profiles_map", profilesObj.toString())
            .putString("last_email", email)
            .apply()
    }

    fun loadUsers(context: Context): MutableMap<String, String> {
        // Authentication is handled exclusively by Firebase Auth.
        return mutableMapOf()
    }

    fun loadProfiles(context: Context): MutableMap<String, UserProfile> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val map = mutableMapOf(
            "shiva@example.com" to UserProfile("Shiva K.", "shiva", "shiva@example.com", "+91 98765 43210"),
            "patient@simats.edu" to UserProfile("Patient Simats", "simats", "patient@simats.edu", "+91 98765 11111")
        )
        val profilesJson = prefs.getString("profiles_map", null) ?: return map
        try {
            val obj = JSONObject(profilesJson)
            val keys = obj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                val pObj = obj.getJSONObject(k)
                map[k] = UserProfile(
                    fullName = pObj.optString("fullName", "Patient User"),
                    username = pObj.optString("username", "patient"),
                    email = pObj.optString("email", k),
                    phone = pObj.optString("phone", "+91 98765 00000"),
                    ageGender = pObj.optString("ageGender", "24 Years • Male"),
                    therapyGoal = pObj.optString("therapyGoal", "Visual focus stabilization & motor tracking"),
                    therapistName = pObj.optString("therapistName", "Dr. A. Rajesh (Neuro-Ophthalmology)"),
                    emergencyContact = pObj.optString("emergencyContact", "+91 98765 00000 (Parent)")
                )
            }
        } catch (_: Exception) {}
        return map
    }

    fun saveSession(context: Context, email: String, session: SessionResult) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val arrJson = prefs.getString("sessions_list_$email", "[]") ?: "[]"
        val arr = JSONArray(arrJson)
        val obj = JSONObject().apply {
            put("id", session.id)
            put("exerciseTitle", session.exerciseTitle)
            put("timestamp", session.timestamp)
            put("dayOfWeek", session.dayOfWeek)
            put("dateMillis", session.dateMillis)
            put("hits", session.hits)
            put("totalTargets", session.totalTargets)
            put("accuracyPct", session.accuracyPct)
            put("avgReactionMs", session.avgReactionMs)
            put("durationSeconds", session.durationSeconds)
            put("level", session.level)
            put("levelLabel", session.levelLabel)
        }
        arr.put(obj)
        prefs.edit().putString("sessions_list_$email", arr.toString()).apply()
    }

    fun replaceSessions(context: Context, email: String, sessions: List<SessionResult>) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val arr = JSONArray()
        sessions.reversed().forEach { session ->
            arr.put(JSONObject().apply {
                put("id", session.id)
                put("exerciseTitle", session.exerciseTitle)
                put("timestamp", session.timestamp)
                put("dayOfWeek", session.dayOfWeek)
                put("dateMillis", session.dateMillis)
                put("hits", session.hits)
                put("totalTargets", session.totalTargets)
                put("accuracyPct", session.accuracyPct)
                put("avgReactionMs", session.avgReactionMs)
                put("durationSeconds", session.durationSeconds)
                put("level", session.level)
                put("levelLabel", session.levelLabel)
            })
        }
        prefs.edit().putString("sessions_list_$email", arr.toString()).apply()
    }

    fun loadSessions(context: Context, email: String): List<SessionResult> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val list = mutableListOf<SessionResult>()
        val arrJson = prefs.getString("sessions_list_$email", null) ?: return emptyList()
        val currentDayDefault = SimpleDateFormat("EEE", Locale.US).format(Date())
        try {
            val arr = JSONArray(arrJson)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    SessionResult(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        exerciseTitle = obj.optString("exerciseTitle", "Visual Focus Training"),
                        timestamp = obj.optString("timestamp", "Just now"),
                        dayOfWeek = obj.optString("dayOfWeek", currentDayDefault),
                        dateMillis = obj.optLong("dateMillis", System.currentTimeMillis()),
                        hits = obj.optInt("hits", 0),
                        totalTargets = obj.optInt("totalTargets", 0),
                        accuracyPct = obj.optInt("accuracyPct", 0),
                        avgReactionMs = obj.optInt("avgReactionMs", 0),
                        durationSeconds = obj.optInt("durationSeconds", 30),
                        level = obj.optInt("level", 3),
                        levelLabel = obj.optString("levelLabel", "Level 3 • Medium")
                    )
                )
            }
        } catch (_: Exception) {}
        return list.reversed()
    }

    fun deleteSession(context: Context, email: String, sessionId: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val arrJson = prefs.getString("sessions_list_$email", "[]") ?: "[]"
        try {
            val arr = JSONArray(arrJson)
            val newArr = JSONArray()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                if (obj.optString("id") != sessionId) {
                    newArr.put(obj)
                }
            }
            prefs.edit().putString("sessions_list_$email", newArr.toString()).apply()
        } catch (_: Exception) {}
    }

    fun saveExerciseLevel(context: Context, exerciseTitle: String, level: Int) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val levelsJson = prefs.getString("exercise_levels_map", "{}") ?: "{}"
        val obj = JSONObject(levelsJson)
        obj.put(exerciseTitle, level)
        prefs.edit().putString("exercise_levels_map", obj.toString()).apply()
    }

    fun loadExerciseLevels(context: Context): MutableMap<String, Int> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val map = mutableMapOf<String, Int>()
        val levelsJson = prefs.getString("exercise_levels_map", null) ?: return map
        try {
            val obj = JSONObject(levelsJson)
            val keys = obj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                map[k] = obj.optInt(k, 3)
            }
        } catch (_: Exception) {}
        return map
    }

    fun setLoggedIn(context: Context, email: String, loggedIn: Boolean) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean("is_logged_in", loggedIn)
            .putString("last_email", email)
            .apply()
    }

    fun getLoggedInState(context: Context): Pair<Boolean, String> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val loggedIn = prefs.getBoolean("is_logged_in", false)
        val email = prefs.getString("last_email", "shiva@example.com") ?: "shiva@example.com"
        return Pair(loggedIn, email)
    }
}

private data class Exercise(val title: String, val subtitle: String, val icon: String, val color: Color, var currentLevel: Int = 3)
private val exercises = listOf(
    Exercise("Visual Focus Training", "Concentration exercises to stabilize ocular fixation.", "◉", Blue),
    Exercise("Eye-Hand Coordination Therapy", "Synchronized visual-motor feedback loop enhancement.", "☝", Color(0xFF2E7D32)),
    Exercise("Shape Recognition Therapy", "Visual-spatial processing and pattern differentiation.", "◆", Blue),
    Exercise("Reaction Time Assessment", "Evaluating neural transmission and motor response latency.", "◔", Green),
    Exercise("Peripheral Vision", "Peripheral field awareness & focal anchor training.", "👁️", Color(0xFFFF9800)),
    Exercise("Visual Scanning", "Fast spatial searching & target symbol identification.", "🔍", Blue),
    Exercise("Memory Sequence", "Sequential recall & visual-spatial memory training.", "🧩", Color(0xFF9C27B0))
)

private val allExercises = exercises + listOf(
    Exercise("Memory & Pattern Rehabilitation", "Short-term cognitive spatial memory retraining.", "M", Blue),
    Exercise("Mood & Stress Analysis", "Real-time emotional wellness and physiological tracking.", "+", Blue)
)

@Composable
fun PddApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        FirestoreManager.keepBackendConnected()
    }

    val (_, storedEmail) = remember { AppStorageManager.getLoggedInState(context) }
    val firebaseAuth = remember { FirebaseAuth.getInstance() }
    var loggedIn by remember { mutableStateOf(false) }
    var selectedExercise by remember { mutableStateOf(allExercises[2]) }

    var currentUserEmail by remember {
        mutableStateOf(firebaseAuth.currentUser?.email?.trim()?.lowercase() ?: storedEmail)
    }

    // Real-Time Persistent Exercise Levels Registry (1 to 5)
    val exerciseLevels = remember {
        mutableStateMapOf<String, Int>().apply {
            putAll(AppStorageManager.loadExerciseLevels(context))
        }
    }

    fun getLevelForExercise(title: String): Int = exerciseLevels[title] ?: 3
    fun setLevelForExercise(title: String, level: Int) {
        exerciseLevels[title] = level
        AppStorageManager.saveExerciseLevel(context, title, level)
        scope.launch {
            try {
                FirestoreManager.saveExerciseLevel(currentUserEmail, title, level)
            } catch (e: Exception) {
                Toast.makeText(context, "Cloud Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Real-Time Persistent User Profiles Registry
    val userProfiles = remember {
        mutableStateMapOf<String, UserProfile>().apply {
            putAll(AppStorageManager.loadProfiles(context))
        }
    }

    // Real-Time Persistent Registered Users Map
    val registeredUsers = remember {
        mutableStateMapOf<String, String>().apply {
            putAll(AppStorageManager.loadUsers(context))
        }
    }

    // Persistent Real-Time Completed Sessions
    val completedSessions = remember {
        mutableStateListOf<SessionResult>()
    }

    LaunchedEffect(Unit) {
        FirestoreManager.keepBackendConnected()
        // Always start fresh on Sign In & Sign Up screen when app is opened
        try {
            firebaseAuth.signOut()
        } catch (_: Exception) {}
        loggedIn = false
        AppStorageManager.setLoggedIn(context, currentUserEmail, false)
    }

    LaunchedEffect(currentUserEmail, loggedIn) {
        if (loggedIn && currentUserEmail.isNotBlank()) {
            val cloudProfile = FirestoreManager.getUserProfile(currentUserEmail)
            if (cloudProfile != null) {
                userProfiles[currentUserEmail] = cloudProfile
                AppStorageManager.saveUser(context, currentUserEmail, "", cloudProfile)
            } else {
                // Ensure profile document is created in Firestore upon login if not present
                val activeProfile = userProfiles[currentUserEmail] ?: UserProfile(
                    fullName = "Patient User",
                    username = currentUserEmail.substringBefore("@"),
                    email = currentUserEmail,
                    phone = "+91 98765 00000"
                )
                userProfiles[currentUserEmail] = activeProfile
                AppStorageManager.saveUser(context, currentUserEmail, "", activeProfile)
                FirestoreManager.saveUserProfile(activeProfile)
            }

            // Load local backup first
            val localSessions = AppStorageManager.loadSessions(context, currentUserEmail)
            completedSessions.clear()
            completedSessions.addAll(localSessions)

            // Merge local backup with Firestore so sessions saved while offline are not lost.
            val firestoreSessions = FirestoreManager.getUserSessions(currentUserEmail)
            val mergedSessions = (firestoreSessions + localSessions)
                .distinctBy { it.id }
                .sortedByDescending { it.dateMillis }

            completedSessions.clear()
            completedSessions.addAll(mergedSessions)
            AppStorageManager.replaceSessions(context, currentUserEmail, mergedSessions)

            // Push all sessions to Firestore so cloud database remains up to date
            for (session in mergedSessions) {
                FirestoreManager.saveSession(currentUserEmail, session)
            }

            // Merge exercise levels and save to Firestore
            val firestoreLevels = FirestoreManager.getUserLevels(currentUserEmail)
            if (firestoreLevels.isNotEmpty()) {
                exerciseLevels.putAll(firestoreLevels)
            }
            for ((title, lvl) in exerciseLevels) {
                FirestoreManager.saveExerciseLevel(currentUserEmail, title, lvl)
            }
        }
    }

    // Resolved profile of currently logged-in patient
    val currentUserProfile = userProfiles[currentUserEmail] ?: UserProfile(
        fullName = "Patient User",
        username = "patient",
        email = currentUserEmail,
        phone = "+91 98765 00000"
    )

    // Real-Time System Back Navigation Backstack (Pops screens step-by-step all the way up to Home screen)
    val navBackStack = remember { mutableStateListOf("Home") }
    val screen = navBackStack.lastOrNull() ?: "Home"

    fun navigateTo(target: String) {
        if (target == "Home") {
            navBackStack.clear()
            navBackStack.add("Home")
        } else if (navBackStack.contains(target)) {
            val idx = navBackStack.indexOf(target)
            while (navBackStack.size > idx + 1) {
                navBackStack.removeAt(navBackStack.size - 1)
            }
        } else {
            navBackStack.add(target)
        }
    }

    fun popBack() {
        if (navBackStack.size > 1) {
            navBackStack.removeAt(navBackStack.size - 1)
        }
    }

    // System Back Button Handler across entire app
    BackHandler(enabled = loggedIn && navBackStack.size > 1) {
        popBack()
    }

    Box(Modifier.fillMaxSize()) {
        ForestBackground()
        if (!loggedIn) {
            AuthScreen(
                userProfiles = userProfiles,
                registeredUsers = registeredUsers,
                onAuthenticated = { email ->
                    FirestoreManager.keepBackendConnected()
                    currentUserEmail = email
                    loggedIn = true
                    AppStorageManager.setLoggedIn(context, email, true)
                }
            )
        } else {
            Scaffold(
                containerColor = Color.Transparent,
                bottomBar = { AnimatedBottomBar(screen) { navigateTo(it) } }
            ) { padding ->
                Box(Modifier.fillMaxSize().padding(padding)) {
                    AnimatedContent(
                        targetState = screen,
                        transitionSpec = {
                            (slideInHorizontally { width -> width / 5 } + fadeIn(tween(150)))
                                .togetherWith(slideOutHorizontally { width -> -width / 5 } + fadeOut(tween(150)))
                        },
                        label = "screen_change_animation"
                    ) { currentScreen ->
                        when (currentScreen) {
                            "Home" -> HomeScreen(
                                userProfile = currentUserProfile,
                                completedSessions = completedSessions,
                                getLevel = ::getLevelForExercise,
                                setLevel = ::setLevelForExercise,
                                onExercise = { selectedExercise = it; navigateTo("Details") },
                                onMood = { navigateTo("Mood") },
                                onHistory = { navigateTo("History") }
                            )
                            "Therapy" -> CategoriesScreen(
                                getLevel = ::getLevelForExercise,
                                onSelect = { selectedExercise = it; navigateTo("Details") },
                                onMood = { navigateTo("Mood") }
                            )
                            "Reports" -> ReportsScreen(
                                completedSessions = completedSessions,
                                onNavigate = { navigateTo(it) },
                                onDeleteSession = { sessionToDelete ->
                                    completedSessions.remove(sessionToDelete)
                                    AppStorageManager.deleteSession(context, currentUserEmail, sessionToDelete.id)
                                    scope.launch {
                                        try {
                                            FirestoreManager.deleteSession(currentUserEmail, sessionToDelete.id)
                                            Toast.makeText(context, "Session Deleted", Toast.LENGTH_SHORT).show()
                                        } catch (_: Exception) {}
                                    }
                                }
                            )
                            "Profile" -> ProfileScreen(
                                userProfile = currentUserProfile,
                                completedSessionsCount = completedSessions.size,
                                onProfileUpdated = { updatedProfile ->
                                    userProfiles[currentUserEmail] = updatedProfile
                                    val userPass = registeredUsers[currentUserEmail] ?: "123456"
                                    AppStorageManager.saveUser(context, currentUserEmail, userPass, updatedProfile)
                                    scope.launch {
                                        FirestoreManager.saveUserProfile(updatedProfile)
                                    }
                                },
                            onSignOut = {
                                    FirebaseAuth.getInstance().signOut()
                                    loggedIn = false
                                    AppStorageManager.setLoggedIn(context, currentUserEmail, false)
                                    navBackStack.clear()
                                    navBackStack.add("Home")
                                }
                            )
                            "Details" -> DetailScreen(
                                exercise = selectedExercise,
                                currentLevel = getLevelForExercise(selectedExercise.title),
                                onLevelSelected = { lvl -> setLevelForExercise(selectedExercise.title, lvl) },
                                onStartSession = { navigateTo("ActiveSession") },
                                onBack = { popBack() }
                            )
                            "ActiveSession" -> ActiveExerciseSessionScreen(
                                exercise = selectedExercise,
                                initialLevel = getLevelForExercise(selectedExercise.title),
                                onLevelChanged = { lvl -> setLevelForExercise(selectedExercise.title, lvl) },
                                onSessionFinished = { result ->
                                    completedSessions.add(0, result)
                                    AppStorageManager.saveSession(context, currentUserEmail, result)
                                    scope.launch {
                                        try {
                                            FirestoreManager.saveSession(currentUserEmail, result)
                                            Toast.makeText(context, "Session Results Uploaded 🚀", Toast.LENGTH_SHORT).show()
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Upload Failed: ${e.message}", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                    navigateTo("Reports")
                                },
                                onCancel = { popBack() }
                            )
                            "Mood" -> MoodScreen(
                                onSessionFinished = { result ->
                                    completedSessions.add(0, result)
                                    AppStorageManager.saveSession(context, currentUserEmail, result)
                                    scope.launch {
                                        try {
                                            FirestoreManager.saveSession(currentUserEmail, result)
                                            Toast.makeText(context, "Session Results Uploaded 🚀", Toast.LENGTH_SHORT).show()
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Upload Failed: ${e.message}", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                    navigateTo("Reports")
                                },
                                onDone = { popBack() }
                            )
                            "Trend" -> TrendScreen("Accuracy Trend", completedSessions) { popBack() }
                            "Reaction" -> TrendScreen("Reaction Trend", completedSessions) { popBack() }
                            "History" -> SessionHistoryScreen(
                                sessions = completedSessions,
                                onBack = { popBack() },
                                onDeleteSession = { sessionToDelete ->
                                    completedSessions.remove(sessionToDelete)
                                    AppStorageManager.deleteSession(context, currentUserEmail, sessionToDelete.id)
                                    scope.launch {
                                        try {
                                            FirestoreManager.deleteSession(currentUserEmail, sessionToDelete.id)
                                            Toast.makeText(context, "Session Deleted", Toast.LENGTH_SHORT).show()
                                        } catch (_: Exception) {}
                                    }
                                }
                            )
                            "Weekly" -> WeeklyScreen(completedSessions) { popBack() }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ForestBackground() {
    Image(
        painter = painterResource(id = R.drawable.bg_forest_rain),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
    )
    Box(
        Modifier
            .fillMaxSize()
            .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        Color(0xF204140A),
                        Color(0xDF092113),
                        Color(0xF4030F07)
                    )
                )
            )
    )
}

// ── Header with Clear Backspace / Back Button ────────────────────────────
@Composable
private fun Header(title: String, onBack: (() -> Unit)? = null) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(CardWhite, RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Green,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
        Spacer(Modifier.width(if (onBack == null) 8.dp else 4.dp))
        Image(
            painter = painterResource(id = R.drawable.logo_cyber_eye),
            contentDescription = "Logo",
            modifier = Modifier.size(32.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(title, style = titleStyle(22.sp), color = Ink, modifier = Modifier.weight(1f))
        if (onBack != null) {
            TextButton(onClick = onBack) {
                Text("Back", color = Green, fontFamily = Serif, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

private fun titleStyle(size: androidx.compose.ui.unit.TextUnit, weight: FontWeight = FontWeight.Bold, color: Color = Ink) =
    TextStyle(fontFamily = Serif, fontWeight = weight, fontSize = size, color = color)
private fun bodyStyle(size: androidx.compose.ui.unit.TextUnit = 15.sp, color: Color = Muted) =
    TextStyle(fontFamily = Serif, fontSize = size, color = color, lineHeight = (size.value * 1.4f).sp)

// ── Animated Bottom Navigation Bar ──────────────────────────────────────
private data class NavItemData(
    val label: String,
    val iconFilled: ImageVector,
    val iconOutlined: ImageVector
)

@Composable
private fun AnimatedBottomBar(current: String, onNavigate: (String) -> Unit) {
    val navItems = listOf(
        NavItemData("Home", Icons.Filled.Home, Icons.Outlined.Home),
        NavItemData("Therapy", Icons.Filled.FitnessCenter, Icons.Outlined.FitnessCenter),
        NavItemData("Reports", Icons.Filled.BarChart, Icons.Outlined.Assessment),
        NavItemData("Profile", Icons.Filled.Person, Icons.Outlined.Person)
    )

    Surface(
        color = Color(0xF7FFFFFF),
        shadowElevation = 12.dp
    ) {
        val activeTab = when (current) {
            "Details", "ActiveSession" -> "Therapy"
            "History", "Trend", "Reaction", "Weekly" -> "Reports"
            else -> current
        }

        Row(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 6.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEach { item ->
                val isSelected = activeTab == item.label

                val activeBgColor by animateColorAsState(
                    targetValue = if (isSelected) Green.copy(alpha = 0.18f) else Color.Transparent,
                    animationSpec = tween(150),
                    label = "tabBg"
                )

                val contentColor by animateColorAsState(
                    targetValue = if (isSelected) Green else Color(0xFF37474F),
                    animationSpec = tween(150),
                    label = "tabContent"
                )

                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.15f else 1.0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "tabScale"
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(activeBgColor)
                        .clickable { onNavigate(item.label) }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (isSelected) item.iconFilled else item.iconOutlined,
                            contentDescription = item.label,
                            tint = contentColor,
                            modifier = Modifier
                                .size(25.dp)
                                .scale(scale)
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = item.label,
                            style = bodyStyle(12.sp, color = contentColor),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

// ── Home Screen with Dynamic Welcome Name & Real-time Progress Stats ─────
@Composable
private fun HomeScreen(
    userProfile: UserProfile,
    completedSessions: List<SessionResult>,
    getLevel: (String) -> Int,
    setLevel: (String, Int) -> Unit,
    onExercise: (Exercise) -> Unit,
    onMood: () -> Unit,
    onHistory: () -> Unit
) {
    val welcomeName = userProfile.fullName.split(" ").firstOrNull() ?: "Patient"

    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Spacer(Modifier.height(8.dp))
            Text(
                "Integrated Visual Motor Trainer",
                style = titleStyle(20.sp, color = Color.White),
                modifier = Modifier.fillMaxWidth()
            )
        }

        item { ProgressCard(welcomeName, completedSessions) }

        item { Text("Current Therapy Sessions", style = titleStyle(21.sp, color = Color.White)) }
        items(allExercises) { exercise ->
            val lastResult = completedSessions.firstOrNull { it.exerciseTitle == exercise.title }
            val currentLvl = getLevel(exercise.title)
            SessionCard(
                exercise = exercise,
                level = currentLvl,
                lastResult = lastResult,
                onLevelChange = { newLvl -> setLevel(exercise.title, newLvl) },
                onClick = {
                    if (exercise.title.contains("Mood")) onMood() else onExercise(exercise)
                }
            )
        }
        item { Text("Clinical Tools & Analytics", style = titleStyle(21.sp, color = Color.White), modifier = Modifier.padding(top = 8.dp)) }
        item { ClinicalToolCard("Complete Session History", "📜", onHistory) }
        item {
            OutlinedButton(
                onClick = onMood,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = CardWhite,
                    contentColor = Green
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Text("✦  Mood & Stress Check-in", fontFamily = Serif, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Green)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable private fun ProgressCard(name: String, sessions: List<SessionResult>) {
    val avgAccuracy = if (sessions.isNotEmpty()) sessions.map { it.accuracyPct }.average().toInt() else 0
    val completedExercisesCount = sessions.map { it.exerciseTitle }.distinct().size
    val progressPct = ((completedExercisesCount * 100) / allExercises.size).coerceAtMost(100)

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier.border(1.dp, CardBorder, RoundedCornerShape(24.dp))
    ) {
        Column(Modifier.padding(24.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Welcome back,", style = bodyStyle(16.sp, color = Muted))
                    Text(name, style = titleStyle(28.sp, color = Blue))
                    Spacer(Modifier.height(4.dp))
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE8F5E9))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("⚡ Performance Modes 1–5 Active", style = bodyStyle(11.sp, color = Green), fontWeight = FontWeight.Bold)
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Average Accuracy", style = titleStyle(14.sp, weight = FontWeight.SemiBold, color = Muted))
                    Text("$avgAccuracy%", style = titleStyle(20.sp, color = Ink))
                }
            }
            Spacer(Modifier.height(22.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Therapy Progress", style = titleStyle(17.sp))
                Text("$progressPct%", style = titleStyle(21.sp, color = Green))
            }
            Spacer(Modifier.height(8.dp))
            Box(Modifier.fillMaxWidth().height(12.dp).background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp))) {
                Box(
                    Modifier
                        .fillMaxWidth(fraction = (progressPct / 100f).coerceIn(0.05f, 1.0f))
                        .height(12.dp)
                        .background(Green, RoundedCornerShape(8.dp))
                )
            }
        }
    }
}

@Composable private fun SessionCard(
    exercise: Exercise,
    level: Int,
    lastResult: SessionResult?,
    onLevelChange: (Int) -> Unit,
    onClick: () -> Unit
) {
    var showLevelPicker by remember { mutableStateOf(false) }

    Card(
        Modifier
            .fillMaxWidth()
            .border(1.dp, CardBorder, RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(64.dp).background(FieldBg, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(exercise.icon, color = Green, fontSize = 32.sp)
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (lastResult != null) {
                            Text("Acc: ${lastResult.accuracyPct}%", color = Green, style = bodyStyle(12.sp), fontWeight = FontWeight.Bold)
                        } else {
                            Text("Ready", color = Blue, style = bodyStyle(12.sp), fontWeight = FontWeight.Bold)
                        }
                        ContainerLevelBadge(level = level, onClick = { showLevelPicker = !showLevelPicker })
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(exercise.title, style = titleStyle(16.sp, color = Ink))
                    Spacer(Modifier.height(2.dp))
                    Text(exercise.subtitle, style = bodyStyle(12.sp, color = Muted), maxLines = 2)
                }
                Spacer(Modifier.width(6.dp))
                Button(
                    onClick = onClick,
                    shape = RoundedCornerShape(13.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (lastResult != null) Blue else Green)
                ) {
                    Text(if (lastResult != null) "Replay" else "Start", fontFamily = Serif, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            if (showLevelPicker) {
                Spacer(Modifier.height(10.dp))
                RealtimeLevelSelector(selectedLevel = level, onLevelSelected = { newLvl ->
                    onLevelChange(newLvl)
                    showLevelPicker = false
                })
            }
        }
    }
}

@Composable private fun ClinicalToolCard(title: String, icon: String, onClick: () -> Unit) {
    Card(
        Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(1.dp, CardBorder, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(icon, color = Green, fontSize = 26.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(40.dp))
            Text(title, style = titleStyle(17.sp, color = Ink), modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Green)
        }
    }
}

@Composable private fun CategoriesScreen(
    getLevel: (String) -> Int,
    onSelect: (Exercise) -> Unit,
    onMood: () -> Unit
) {
    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Header("Exercise Categories")
            Spacer(Modifier.height(10.dp))
            Text(
                "Select a specialized training modality to begin your rehabilitation session.",
                style = bodyStyle(15.sp, color = Color(0xFFE8F5E9))
            )
        }
        items(allExercises.chunked(2)) { pair ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                CategoryCard(pair[0], getLevel(pair[0].title), Modifier.weight(1f)) {
                    if (pair[0].title.contains("Mood")) onMood() else onSelect(pair[0])
                }
                if (pair.size > 1) {
                    CategoryCard(pair[1], getLevel(pair[1].title), Modifier.weight(1f)) {
                        if (pair[1].title.contains("Mood")) onMood() else onSelect(pair[1])
                    }
                } else {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Green),
                shape = RoundedCornerShape(22.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(Modifier.padding(24.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Daily Goal", style = titleStyle(21.sp, color = Color.White))
                        Text("5 / 5 Exercises", style = bodyStyle(15.sp, color = Color.White))
                    }
                    Spacer(Modifier.height(18.dp))
                    Text("100%", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text("Consistent practice is key to neuroplasticity. Keep going!", style = bodyStyle(15.sp, color = Color.White))
                }
            }
        }
    }
}

@Composable private fun CategoryCard(
    exercise: Exercise,
    level: Int,
    modifier: Modifier,
    onSelect: (Exercise) -> Unit
) {
    Card(
        modifier = modifier
            .clickable { onSelect(exercise) }
            .border(1.dp, CardBorder, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            Modifier.padding(14.dp).height(230.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Box(
                Modifier.size(54.dp).background(FieldBg, RoundedCornerShape(27.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(exercise.icon, color = Green, fontSize = 26.sp)
            }
            ContainerLevelBadge(level = level)
            Text(
                exercise.title,
                style = titleStyle(15.sp, color = Ink),
                textAlign = TextAlign.Center,
                maxLines = 2
            )
            Text(
                exercise.subtitle,
                style = bodyStyle(11.sp, color = Muted),
                textAlign = TextAlign.Center,
                maxLines = 2
            )
            Text(
                "Start Session ►",
                color = Green,
                fontFamily = Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}

private data class ExerciseSlide(
    val stepTitle: String,
    val description: String,
    val details: String,
    val icon: String
)

@Composable private fun DetailScreen(
    exercise: Exercise,
    currentLevel: Int,
    onLevelSelected: (Int) -> Unit,
    onStartSession: () -> Unit,
    onBack: () -> Unit
) {
    val slides = remember {
        listOf(
            ExerciseSlide(
                "Step 1 • Visual Fixation & Alignment",
                "Center your visual gaze on the target dot.",
                "Maintain steady posture. Keep distance at 40cm from the screen.",
                "◉"
            ),
            ExerciseSlide(
                "Step 2 • Saccadic Target Tracking",
                "Follow target points as they shift dynamically.",
                "Move eyes only without tilting your head. Speed adapts to response.",
                "☝"
            ),
            ExerciseSlide(
                "Step 3 • Motor Feedback Loop",
                "Tap targets accurately upon appearance.",
                "Synchronize hand movements with visual stimulation cues.",
                "◆"
            ),
            ExerciseSlide(
                "Step 4 • Doctor Metric Evaluation",
                "Session complete! System evaluates response latency.",
                "Results are updated live to Doctor Progress Monitor.",
                "🩺"
            )
        )
    }

    var currentSlideIndex by remember { mutableIntStateOf(0) }

    // Intercept system Back button
    BackHandler(enabled = currentSlideIndex > 0) {
        currentSlideIndex--
    }

    Column(
        Modifier.fillMaxSize().padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Header("Exercise Details", onBack)
        Spacer(Modifier.height(14.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(76.dp).background(CardWhite, RoundedCornerShape(20.dp)).border(1.dp, CardBorder, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(exercise.icon, color = Green, fontSize = 38.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(exercise.title, style = titleStyle(20.sp, color = Color.White))
                Text(exercise.subtitle, style = bodyStyle(12.sp, color = Color(0xFFE8F5E9)), maxLines = 2)
            }
        }
        Spacer(Modifier.height(14.dp))

        // Level Selector Container
        RealtimeLevelSelector(selectedLevel = currentLevel, onLevelSelected = onLevelSelected)
        Spacer(Modifier.height(14.dp))

        // Slide Carousel
        Card(
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            shape = RoundedCornerShape(22.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            modifier = Modifier.fillMaxWidth().border(1.dp, CardBorder, RoundedCornerShape(22.dp))
        ) {
            Column(Modifier.padding(18.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Interactive Exercise Guide", style = titleStyle(14.sp, color = Green))
                    Text("Slide ${currentSlideIndex + 1} of ${slides.size}", style = bodyStyle(12.sp, color = Muted), fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(10.dp))

                AnimatedContent(
                    targetState = currentSlideIndex,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally { width -> width } + fadeIn()).togetherWith(slideOutHorizontally { width -> -width } + fadeOut())
                        } else {
                            (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(slideOutHorizontally { width -> width } + fadeOut())
                        }
                    },
                    label = "slide_transition"
                ) { index ->
                    val slide = slides[index]
                    Column(Modifier.fillMaxWidth()) {
                        Text(slide.stepTitle, style = titleStyle(16.sp, color = Ink))
                        Spacer(Modifier.height(4.dp))
                        Text(slide.description, style = bodyStyle(13.sp, color = Muted))
                        Spacer(Modifier.height(6.dp))
                        Box(
                            Modifier.fillMaxWidth().background(FieldBg, RoundedCornerShape(12.dp)).padding(10.dp)
                        ) {
                            Text("💡 " + slide.details, style = bodyStyle(12.sp, color = Ink))
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { if (currentSlideIndex > 0) currentSlideIndex-- },
                        enabled = currentSlideIndex > 0,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(38.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Slide", tint = if (currentSlideIndex > 0) Green else Color.Gray)
                        Spacer(Modifier.width(2.dp))
                        Text("◄ Prev", fontFamily = Serif, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (currentSlideIndex > 0) Green else Color.Gray)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        slides.indices.forEach { i ->
                            Box(
                                Modifier
                                    .size(if (i == currentSlideIndex) 10.dp else 8.dp)
                                    .background(
                                        if (i == currentSlideIndex) Green else Color(0xFFC8E6C9),
                                        RoundedCornerShape(5.dp)
                                    )
                            )
                        }
                    }

                    Button(
                        onClick = { if (currentSlideIndex < slides.size - 1) currentSlideIndex++ },
                        enabled = currentSlideIndex < slides.size - 1,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(38.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Green)
                    ) {
                        Text("Next ►", fontFamily = Serif, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(Modifier.width(2.dp))
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next Slide", tint = Color.White)
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        Button(
            onClick = onStartSession,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Green)
        ) {
            Text("Start Level $currentLevel Session ►", fontFamily = Serif, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        Spacer(Modifier.height(14.dp))
    }
}

@Composable
private fun RealtimeGameTopHeader(
    title: String,
    currentLevel: Int,
    score: Int,
    onLevelChanged: (Int) -> Unit,
    onBack: () -> Unit
) {
    var showLevelSelector by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Ink,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(Modifier.width(6.dp))
                Column {
                    Text(title, style = titleStyle(19.sp, color = Ink))
                    ContainerLevelBadge(
                        level = currentLevel,
                        onClick = { showLevelSelector = !showLevelSelector }
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🏆 ", fontSize = 14.sp)
                    Text("SCORE", fontFamily = Serif, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF9800))
                }
                Text("$score", style = titleStyle(22.sp, color = Ink))
            }
        }

        if (showLevelSelector) {
            Spacer(Modifier.height(10.dp))
            RealtimeLevelSelector(
                selectedLevel = currentLevel,
                onLevelSelected = { newLevel ->
                    onLevelChanged(newLevel)
                    showLevelSelector = false
                }
            )
        }
    }
}

// ── Real Working Interactive Exercise Session Router ─────────────────────
@Composable private fun ActiveExerciseSessionScreen(
    exercise: Exercise,
    initialLevel: Int,
    onLevelChanged: (Int) -> Unit,
    onSessionFinished: (SessionResult) -> Unit,
    onCancel: () -> Unit
) {
    var activeLevel by remember { mutableIntStateOf(initialLevel) }

    fun updateLevel(lvl: Int) {
        activeLevel = lvl
        onLevelChanged(lvl)
    }

    when {
        exercise.title.contains("Peripheral Vision") -> {
            PeripheralVisionGame(exercise, activeLevel, ::updateLevel, onSessionFinished, onCancel)
        }
        exercise.title.contains("Visual Scanning") -> {
            VisualScanningGame(exercise, activeLevel, ::updateLevel, onSessionFinished, onCancel)
        }
        exercise.title.contains("Memory Sequence") -> {
            MemorySequenceGame(exercise, activeLevel, ::updateLevel, onSessionFinished, onCancel)
        }
        exercise.title.contains("Reaction") || exercise.title.contains("Speed") -> {
            ReactionSpeedGame(exercise, activeLevel, ::updateLevel, onSessionFinished, onCancel)
        }
        exercise.title.contains("Memory") || exercise.title.contains("Pattern") || exercise.title.contains("Rehabilitation") -> {
            MemoryPatternRehabilitationGame(exercise, activeLevel, ::updateLevel, onSessionFinished, onCancel)
        }
        exercise.title.contains("Shape") || exercise.title.contains("Recognition") || exercise.title.contains("Matching") -> {
            ShapeRecognitionGame(exercise, activeLevel, ::updateLevel, onSessionFinished, onCancel)
        }
        exercise.title.contains("Eye-Hand") || exercise.title.contains("Coordination") || exercise.title.contains("Hand") -> {
            HandEyeCoordinationGame(exercise, activeLevel, ::updateLevel, onSessionFinished, onCancel)
        }
        else -> {
            ColorRecognitionGame(exercise, activeLevel, ::updateLevel, onSessionFinished, onCancel)
        }
    }
}

// ── Reaction Speed Session Game (Real-Time Mode Levels 1 to 5) ──
@Composable
private fun ReactionSpeedGame(
    exercise: Exercise,
    activeLevel: Int,
    onLevelChanged: (Int) -> Unit,
    onSessionFinished: (SessionResult) -> Unit,
    onCancel: () -> Unit
) {
    val levelInfo = remember(activeLevel) { getLevelDetails(activeLevel) }
    val totalRounds = levelInfo.roundCount
    var currentRound by remember { mutableIntStateOf(1) }
    var score by remember { mutableIntStateOf(0) }
    var hits by remember { mutableIntStateOf(0) }
    var misses by remember { mutableIntStateOf(0) }
    val reactionTimes = remember { mutableStateListOf<Int>() }
    var sessionCompleted by remember { mutableStateOf(false) }

    // 8 Vibrant Colors
    val colorOptions = remember {
        listOf(
            Color(0xFF0033FF) to "BLUE",
            Color(0xFFFF0000) to "RED",
            Color(0xFF00C853) to "GREEN",
            Color(0xFFFDD835) to "YELLOW",
            Color(0xFFFF6D00) to "ORANGE",
            Color(0xFFAA00FF) to "PURPLE",
            Color(0xFF00E5FF) to "CYAN",
            Color(0xFFFF4081) to "PINK"
        )
    }

    var currentColorPair by remember { mutableStateOf(colorOptions[0]) }
    var spawnTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    fun generateNextColor() {
        currentColorPair = colorOptions.random()
        spawnTime = System.currentTimeMillis()
    }

    LaunchedEffect(currentRound, activeLevel) {
        generateNextColor()
    }

    fun handleCircleTap() {
        val now = System.currentTimeMillis()
        val reaction = (now - spawnTime).toInt().coerceIn(100, 950)
        reactionTimes.add(reaction)

        hits++
        score += (10 * levelInfo.speedMultiplier).toInt().coerceAtLeast(10)

        if (currentRound < totalRounds) {
            currentRound++
        } else {
            sessionCompleted = true
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Box(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                RealtimeGameTopHeader(
                    title = "Reaction Speed",
                    currentLevel = activeLevel,
                    score = score,
                    onLevelChanged = onLevelChanged,
                    onBack = onCancel
                )

                Spacer(Modifier.height(20.dp))

                // Round Header Text
                Text("Round $currentRound of $totalRounds", style = titleStyle(26.sp, color = Ink), textAlign = TextAlign.Center)
                Spacer(Modifier.height(4.dp))
                Text("Watch closely & tap fast...", style = bodyStyle(15.sp, color = Color.Gray), textAlign = TextAlign.Center)

                Spacer(Modifier.weight(1f))

                // Dynamic Size Reaction Circle based on Level (L1 larger, L5 micro precision)
                val circleSize = (levelInfo.targetSizeDp * 2.6f).dp
                Box(
                    modifier = Modifier
                        .size(circleSize)
                        .clip(RoundedCornerShape(circleSize / 2))
                        .background(currentColorPair.first)
                        .border(4.dp, Color.White, RoundedCornerShape(circleSize / 2))
                        .clickable { handleCircleTap() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentColorPair.second,
                        fontFamily = Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = (circleSize.value * 0.16f).sp,
                        color = Color.White
                    )
                }

                Spacer(Modifier.weight(1f))

                Text("Tap the color circle as fast as possible!", style = bodyStyle(14.sp, color = Muted), modifier = Modifier.padding(bottom = 36.dp))
            }

            // Session Completion Overlay
            if (sessionCompleted) {
                val finalAcc = if (hits + misses > 0) (hits * 100) / (hits + misses) else 100
                val avgReact = if (reactionTimes.isNotEmpty()) reactionTimes.average().toInt() else 210

                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(0.75f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                            .border(1.dp, CardBorder, RoundedCornerShape(26.dp)),
                        shape = RoundedCornerShape(26.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        elevation = CardDefaults.cardElevation(10.dp)
                    ) {
                        Column(
                            Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🏆", fontSize = 48.sp)
                            Spacer(Modifier.height(6.dp))
                            Text("Session Completed!", style = titleStyle(24.sp, color = Ink), textAlign = TextAlign.Center)
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Reaction Speed", style = bodyStyle(15.sp, color = Green), fontWeight = FontWeight.Bold)
                                ContainerLevelBadge(level = activeLevel)
                            }

                            Spacer(Modifier.height(20.dp))

                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Final Score", style = bodyStyle(12.sp, color = Muted))
                                    Text("$score pts", style = titleStyle(22.sp, color = Ink))
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Accuracy", style = bodyStyle(12.sp, color = Muted))
                                    Text("$finalAcc%", style = titleStyle(22.sp, color = Green))
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Avg Speed", style = bodyStyle(12.sp, color = Blue))
                                    Text("${avgReact}ms", style = titleStyle(22.sp, color = Blue))
                                }
                            }

                            Spacer(Modifier.height(24.dp))

                            Button(
                                onClick = {
                                    val result = SessionResult(
                                        exerciseTitle = "Reaction Time Assessment",
                                        hits = hits,
                                        totalTargets = totalRounds,
                                        accuracyPct = finalAcc,
                                        avgReactionMs = avgReact,
                                        durationSeconds = 30
                                    )
                                    onSessionFinished(result)
                                },
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Green)
                            ) {
                                Text("Save Results & Continue", fontFamily = Serif, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Memory & Pattern Rehabilitation Session Game (Interactive 3x3 Pattern Sequence & Live Score) ──
@Composable
private fun MemoryPatternRehabilitationGame(
    exercise: Exercise,
    activeLevel: Int,
    onLevelChanged: (Int) -> Unit,
    onSessionFinished: (SessionResult) -> Unit,
    onCancel: () -> Unit
) {
    val levelInfo = remember(activeLevel) { getLevelDetails(activeLevel) }
    val totalRounds = levelInfo.roundCount
    var currentRound by remember { mutableIntStateOf(1) }
    var score by remember { mutableIntStateOf(0) }
    var hits by remember { mutableIntStateOf(0) }
    var misses by remember { mutableIntStateOf(0) }
    val reactionTimes = remember { mutableStateListOf<Int>() }
    var sessionCompleted by remember { mutableStateOf(false) }

    val activeSequence = remember { mutableStateListOf<Int>() }
    val userSequence = remember { mutableStateListOf<Int>() }
    var highlightedTile by remember { mutableIntStateOf(-1) }
    var isShowingSequence by remember { mutableStateOf(true) }
    var promptText by remember { mutableStateOf("Watch closely...") }
    var spawnTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    fun generateNewSequence() {
        activeSequence.clear()
        userSequence.clear()
        val count = (2 + activeLevel).coerceAtMost(7)
        for (i in 0 until count) {
            activeSequence.add((0..8).random())
        }
        isShowingSequence = true
        promptText = "Watch closely..."
    }

    LaunchedEffect(currentRound, activeLevel) {
        generateNewSequence()
        val flashDelay = (550L / levelInfo.speedMultiplier).toLong().coerceAtLeast(180L)
        for (tileIndex in activeSequence) {
            delay(flashDelay)
            highlightedTile = tileIndex
            delay(flashDelay)
            highlightedTile = -1
        }
        isShowingSequence = false
        promptText = "Repeat the pattern"
        spawnTime = System.currentTimeMillis()
    }

    fun handleTileClick(index: Int) {
        if (isShowingSequence) return

        userSequence.add(index)
        val step = userSequence.size - 1

        if (userSequence[step] == activeSequence[step]) {
            if (userSequence.size == activeSequence.size) {
                val now = System.currentTimeMillis()
                val reaction = (now - spawnTime).toInt().coerceIn(140, 850)
                reactionTimes.add(reaction)
                hits++
                score += (10 * levelInfo.speedMultiplier).toInt().coerceAtLeast(10)

                if (currentRound < totalRounds) {
                    currentRound++
                } else {
                    sessionCompleted = true
                }
            }
        } else {
            misses++
            userSequence.clear()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Box(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                RealtimeGameTopHeader(
                    title = "Memory & Pattern",
                    currentLevel = activeLevel,
                    score = score,
                    onLevelChanged = onLevelChanged,
                    onBack = onCancel
                )

                Spacer(Modifier.height(20.dp))

                Text("Round $currentRound of $totalRounds", style = titleStyle(26.sp, color = Ink), textAlign = TextAlign.Center)
                Spacer(Modifier.height(4.dp))
                Text(promptText, style = bodyStyle(15.sp, color = Color.Gray), textAlign = TextAlign.Center)
                Spacer(Modifier.height(20.dp))

                Text("Repeat the pattern", style = titleStyle(20.sp, color = Ink), textAlign = TextAlign.Center)
                Spacer(Modifier.height(16.dp))

                // 3x3 Grid of 9 Light Blue Cards
                Column(
                    modifier = Modifier.width(300.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    for (row in 0..2) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            for (col in 0..2) {
                                val index = row * 3 + col
                                val isHighlighted = (highlightedTile == index)

                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(86.dp)
                                        .clickable { handleTileClick(index) }
                                        .border(1.dp, Color(0xFFBBDEFB), RoundedCornerShape(18.dp)),
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isHighlighted) Color(0xFF1E88E5) else Color(0xFFE3F2FD)
                                    ),
                                    elevation = CardDefaults.cardElevation(2.dp)
                                ) {}
                            }
                        }
                    }
                }

                Spacer(Modifier.weight(1f))
            }

            // Session Completion Overlay
            if (sessionCompleted) {
                val finalAcc = if (hits + misses > 0) (hits * 100) / (hits + misses) else 100
                val avgReact = if (reactionTimes.isNotEmpty()) reactionTimes.average().toInt() else 215

                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(0.75f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                            .border(1.dp, CardBorder, RoundedCornerShape(26.dp)),
                        shape = RoundedCornerShape(26.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        elevation = CardDefaults.cardElevation(10.dp)
                    ) {
                        Column(
                            Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🏆", fontSize = 48.sp)
                            Spacer(Modifier.height(6.dp))
                            Text("Session Completed!", style = titleStyle(24.sp, color = Ink), textAlign = TextAlign.Center)
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Memory & Pattern", style = bodyStyle(15.sp, color = Green), fontWeight = FontWeight.Bold)
                                ContainerLevelBadge(level = activeLevel)
                            }

                            Spacer(Modifier.height(20.dp))

                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Final Score", style = bodyStyle(12.sp, color = Muted))
                                    Text("$score pts", style = titleStyle(22.sp, color = Ink))
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Accuracy", style = bodyStyle(12.sp, color = Muted))
                                    Text("$finalAcc%", style = titleStyle(22.sp, color = Green))
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Avg Speed", style = bodyStyle(12.sp, color = Blue))
                                    Text("${avgReact}ms", style = titleStyle(22.sp, color = Blue))
                                }
                            }

                            Spacer(Modifier.height(24.dp))

                            Button(
                                onClick = {
                                    val result = SessionResult(
                                        exerciseTitle = "Memory & Pattern Rehabilitation",
                                        hits = hits,
                                        totalTargets = totalRounds,
                                        accuracyPct = finalAcc,
                                        avgReactionMs = avgReact,
                                        durationSeconds = 30,
                                        level = activeLevel,
                                        levelLabel = "Level $activeLevel • ${levelInfo.name}"
                                    )
                                    onSessionFinished(result)
                                },
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Green)
                            ) {
                                Text("Save Results & Continue", fontFamily = Serif, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}


// ── Shape Recognition / Shape Matching Session Game ──
private data class ShapeItem(
    val name: String,
    val icon: String
)

@Composable
private fun ShapeRecognitionGame(
    exercise: Exercise,
    activeLevel: Int,
    onLevelChanged: (Int) -> Unit,
    onSessionFinished: (SessionResult) -> Unit,
    onCancel: () -> Unit
) {
    val levelInfo = remember(activeLevel) { getLevelDetails(activeLevel) }
    val totalRounds = levelInfo.roundCount
    var currentRound by remember { mutableIntStateOf(1) }
    var score by remember { mutableIntStateOf(0) }
    var hits by remember { mutableIntStateOf(0) }
    var misses by remember { mutableIntStateOf(0) }
    val reactionTimes = remember { mutableStateListOf<Int>() }
    var sessionCompleted by remember { mutableStateOf(false) }

    val allShapes = remember {
        listOf(
            ShapeItem("Star", "★"),
            ShapeItem("Hexagon", "⬢"),
            ShapeItem("Circle", "●"),
            ShapeItem("Triangle", "▲"),
            ShapeItem("Square", "■"),
            ShapeItem("Diamond", "◆")
        )
    }

    var targetShape by remember { mutableStateOf(allShapes[1]) }
    val choiceOptions = remember { mutableStateListOf<ShapeItem>() }
    var spawnTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var feedbackMessage by remember { mutableStateOf("") }

    fun generateNewRound() {
        val target = allShapes.random()
        targetShape = target
        val choiceCount = (3 + (activeLevel / 2)).coerceAtMost(4)
        val others = allShapes.filter { it.name != target.name }.shuffled().take(choiceCount - 1)
        choiceOptions.clear()
        choiceOptions.addAll((others + target).shuffled())
        spawnTime = System.currentTimeMillis()
    }

    LaunchedEffect(Unit, activeLevel) {
        generateNewRound()
    }

    fun handleChoiceSelected(choice: ShapeItem) {
        val now = System.currentTimeMillis()
        val reaction = (now - spawnTime).toInt().coerceIn(140, 900)
        reactionTimes.add(reaction)

        if (choice.name == targetShape.name) {
            hits++
            score += (10 * levelInfo.speedMultiplier).toInt().coerceAtLeast(10)
            feedbackMessage = "✓ Correct Match! (+10)"
        } else {
            misses++
            feedbackMessage = "✗ Incorrect!"
        }

        if (currentRound < totalRounds) {
            currentRound++
            generateNewRound()
        } else {
            sessionCompleted = true
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Box(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                RealtimeGameTopHeader(
                    title = "Shape Matching",
                    currentLevel = activeLevel,
                    score = score,
                    onLevelChanged = onLevelChanged,
                    onBack = onCancel
                )

                Spacer(Modifier.height(18.dp))

                Text("Round $currentRound of $totalRounds", style = titleStyle(26.sp, color = Ink), textAlign = TextAlign.Center)
                Spacer(Modifier.height(4.dp))
                Text("Match target shape...", style = bodyStyle(15.sp, color = Color.Gray), textAlign = TextAlign.Center)

                Spacer(Modifier.height(20.dp))

                val centerCardSize = (210 * (1.05f - (activeLevel * 0.04f))).dp
                Card(
                    modifier = Modifier
                        .size(centerCardSize)
                        .border(3.dp, Color(0xFF2196F3), RoundedCornerShape(28.dp)),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF4F9FF)),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = targetShape.icon,
                            fontSize = (centerCardSize.value * 0.42f).sp,
                            color = Color(0xFF2196F3)
                        )
                    }
                }

                if (feedbackMessage.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        feedbackMessage,
                        style = titleStyle(16.sp, color = if (feedbackMessage.startsWith("✓")) Green else Color.Red),
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(24.dp))

                Text("Tap the matching shape below", style = bodyStyle(15.sp, color = Color.Gray), textAlign = TextAlign.Center)
                Spacer(Modifier.height(14.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    choiceOptions.forEach { shape ->
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(78.dp)
                                .clickable { handleChoiceSelected(shape) }
                                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(18.dp)),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = shape.icon,
                                    fontSize = 34.sp,
                                    color = Color(0xFF2196F3)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
            }

            // Session Completion Overlay
            if (sessionCompleted) {
                val finalAcc = if (totalRounds > 0) (hits * 100) / totalRounds else 100
                val avgReact = if (reactionTimes.isNotEmpty()) reactionTimes.average().toInt() else 220

                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(0.75f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                            .border(1.dp, CardBorder, RoundedCornerShape(26.dp)),
                        shape = RoundedCornerShape(26.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        elevation = CardDefaults.cardElevation(10.dp)
                    ) {
                        Column(
                            Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🏆", fontSize = 48.sp)
                            Spacer(Modifier.height(6.dp))
                            Text("Session Completed!", style = titleStyle(24.sp, color = Ink), textAlign = TextAlign.Center)
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Shape Matching", style = bodyStyle(15.sp, color = Green), fontWeight = FontWeight.Bold)
                                ContainerLevelBadge(level = activeLevel)
                            }

                            Spacer(Modifier.height(20.dp))

                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Final Score", style = bodyStyle(12.sp, color = Muted))
                                    Text("$score pts", style = titleStyle(22.sp, color = Ink))
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Accuracy", style = bodyStyle(12.sp, color = Muted))
                                    Text("$finalAcc%", style = titleStyle(22.sp, color = Green))
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Avg Speed", style = bodyStyle(12.sp, color = Blue))
                                    Text("${avgReact}ms", style = titleStyle(22.sp, color = Blue))
                                }
                            }

                            Spacer(Modifier.height(24.dp))

                            Button(
                                onClick = {
                                    val result = SessionResult(
                                        exerciseTitle = "Shape Recognition Therapy",
                                        hits = hits,
                                        totalTargets = totalRounds,
                                        accuracyPct = finalAcc,
                                        avgReactionMs = avgReact,
                                        durationSeconds = 30,
                                        level = activeLevel,
                                        levelLabel = "Level $activeLevel • ${levelInfo.name}"
                                    )
                                    onSessionFinished(result)
                                },
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Green)
                            ) {
                                Text("Save Results & Continue", fontFamily = Serif, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}


// ── Hand-Eye Coordination Therapy Session Game ──
private data class CircleTarget(
    val id: Int,
    var xFraction: Float,
    var yFraction: Float,
    val color: Color,
    val sizeDp: Int = 76
)

@Composable
private fun HandEyeCoordinationGame(
    exercise: Exercise,
    activeLevel: Int,
    onLevelChanged: (Int) -> Unit,
    onSessionFinished: (SessionResult) -> Unit,
    onCancel: () -> Unit
) {
    val levelInfo = remember(activeLevel) { getLevelDetails(activeLevel) }
    val totalRounds = levelInfo.roundCount
    var currentRound by remember { mutableIntStateOf(1) }
    var score by remember { mutableIntStateOf(0) }
    var hits by remember { mutableIntStateOf(0) }
    var misses by remember { mutableIntStateOf(0) }
    val reactionTimes = remember { mutableStateListOf<Int>() }
    var sessionCompleted by remember { mutableStateOf(false) }

    val activeTargets = remember { mutableStateListOf<CircleTarget>() }
    var spawnTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var jumpTrigger by remember { mutableIntStateOf(0) }

    val availableColors = remember {
        listOf(
            Color(0xFFFFFF00),
            Color(0xFFFF0000),
            Color(0xFF0000FF),
            Color(0xFF00E676),
            Color(0xFFAF52DE)
        )
    }

    fun generateNewRoundTargets() {
        activeTargets.clear()
        val colors = availableColors.shuffled()
        val targetSize = levelInfo.targetSizeDp
        val posList = listOf(
            Pair((15..45).random() / 100f, (20..40).random() / 100f),
            Pair((50..80).random() / 100f, (30..50).random() / 100f),
            Pair((25..75).random() / 100f, (55..75).random() / 100f)
        )
        for (i in 0..2) {
            val (x, y) = posList[i]
            activeTargets.add(CircleTarget(i, x, y, colors[i], sizeDp = targetSize))
        }
        spawnTime = System.currentTimeMillis()
    }

    // Auto-jump timer driven by level speedMultiplier
    LaunchedEffect(currentRound, activeLevel, sessionCompleted) {
        if (!sessionCompleted) {
            generateNewRoundTargets()
            val jumpDelay = (1700L / levelInfo.speedMultiplier).toLong().coerceAtLeast(500L)
            while (!sessionCompleted) {
                delay(jumpDelay)
                activeTargets.forEachIndexed { idx, target ->
                    val newX = (15..80).random() / 100f
                    val newY = (20..75).random() / 100f
                    activeTargets[idx] = target.copy(xFraction = newX, yFraction = newY)
                }
                jumpTrigger++
            }
        }
    }

    fun handleCircleTap(target: CircleTarget) {
        val now = System.currentTimeMillis()
        val reaction = (now - spawnTime).toInt().coerceIn(120, 850)
        reactionTimes.add(reaction)

        hits++
        score += (10 * levelInfo.speedMultiplier).toInt().coerceAtLeast(10)

        val newX = (15..80).random() / 100f
        val newY = (20..75).random() / 100f
        val idx = activeTargets.indexOfFirst { it.id == target.id }
        if (idx != -1) {
            activeTargets[idx] = target.copy(xFraction = newX, yFraction = newY)
        }

        if (hits % 3 == 0) {
            if (currentRound < totalRounds) {
                currentRound++
                generateNewRoundTargets()
            } else {
                sessionCompleted = true
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Box(Modifier.fillMaxSize()) {
            Column(
                Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                RealtimeGameTopHeader(
                    title = "Hand-Eye Coordination",
                    currentLevel = activeLevel,
                    score = score,
                    onLevelChanged = onLevelChanged,
                    onBack = onCancel
                )

                Spacer(Modifier.height(14.dp))

                Text("Round $currentRound of $totalRounds", style = titleStyle(26.sp, color = Ink), textAlign = TextAlign.Center)
                Spacer(Modifier.height(4.dp))
                Text("Tap jumping targets before they move!", style = bodyStyle(15.sp, color = Muted), textAlign = TextAlign.Center)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clickable { misses++ }
                ) {
                    activeTargets.forEach { target ->
                        val animX by animateFloatAsState(
                            targetValue = (target.xFraction * 2) - 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            label = "jumpX"
                        )
                        val animY by animateFloatAsState(
                            targetValue = (target.yFraction * 2) - 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            label = "jumpY"
                        )

                        Box(
                            modifier = Modifier
                                .align(BiasAlignment(animX, animY))
                                .size(target.sizeDp.dp)
                                .clip(RoundedCornerShape(target.sizeDp.dp / 2))
                                .background(target.color)
                                .clickable { handleCircleTap(target) }
                        )
                    }
                }
            }

            if (sessionCompleted) {
                val finalAcc = if (hits + misses > 0) (hits * 100) / (hits + misses) else 100
                val avgReact = if (reactionTimes.isNotEmpty()) reactionTimes.average().toInt() else 215

                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(0.75f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                            .border(1.dp, CardBorder, RoundedCornerShape(26.dp)),
                        shape = RoundedCornerShape(26.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        elevation = CardDefaults.cardElevation(10.dp)
                    ) {
                        Column(
                            Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🏆", fontSize = 48.sp)
                            Spacer(Modifier.height(6.dp))
                            Text("Session Completed!", style = titleStyle(24.sp, color = Ink), textAlign = TextAlign.Center)
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Hand-Eye Coordination", style = bodyStyle(15.sp, color = Green), fontWeight = FontWeight.Bold)
                                ContainerLevelBadge(level = activeLevel)
                            }

                            Spacer(Modifier.height(20.dp))

                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Final Score", style = bodyStyle(12.sp, color = Muted))
                                    Text("$score pts", style = titleStyle(22.sp, color = Ink))
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Accuracy", style = bodyStyle(12.sp, color = Muted))
                                    Text("$finalAcc%", style = titleStyle(22.sp, color = Green))
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Avg Speed", style = bodyStyle(12.sp, color = Blue))
                                    Text("${avgReact}ms", style = titleStyle(22.sp, color = Blue))
                                }
                            }

                            Spacer(Modifier.height(24.dp))

                            Button(
                                onClick = {
                                    val result = SessionResult(
                                        exerciseTitle = "Eye-Hand Coordination Therapy",
                                        hits = hits,
                                        totalTargets = totalRounds * 3,
                                        accuracyPct = finalAcc,
                                        avgReactionMs = avgReact,
                                        durationSeconds = 30,
                                        level = activeLevel,
                                        levelLabel = "Level $activeLevel • ${levelInfo.name}"
                                    )
                                    onSessionFinished(result)
                                },
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Green)
                            ) {
                                Text("Save Results & Continue", fontFamily = Serif, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}


// ── Color Recognition Session Game ──
@Composable private fun ColorRecognitionGame(
    exercise: Exercise,
    activeLevel: Int,
    onLevelChanged: (Int) -> Unit,
    onSessionFinished: (SessionResult) -> Unit,
    onCancel: () -> Unit
) {
    val levelInfo = remember(activeLevel) { getLevelDetails(activeLevel) }
    val totalRounds = levelInfo.roundCount
    var currentRound by remember { mutableIntStateOf(1) }
    var score by remember { mutableIntStateOf(0) }
    var hits by remember { mutableIntStateOf(0) }
    var misses by remember { mutableIntStateOf(0) }
    val reactionTimes = remember { mutableStateListOf<Int>() }
    var sessionCompleted by remember { mutableStateOf(false) }

    val colorOptions = remember {
        listOf(
            Color(0xFFFFFF00) to "YELLOW",
            Color(0xFFFF3D00) to "RED",
            Color(0xFF4CAF50) to "GREEN",
            Color(0xFF2196F3) to "BLUE",
            Color(0xFF9C27B0) to "PURPLE",
            Color(0xFFFF9800) to "ORANGE"
        )
    }

    var cardColorPair by remember { mutableStateOf(colorOptions.random()) }
    var wordText by remember { mutableStateOf(colorOptions.random().second) }
    var spawnTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var feedbackMessage by remember { mutableStateOf("") }

    fun generateNewRound() {
        val bgPair = colorOptions.random()
        val isMatch = (0..1).random() == 1
        val text = if (isMatch) {
            bgPair.second
        } else {
            colorOptions.filter { it.second != bgPair.second }.random().second
        }
        cardColorPair = bgPair
        wordText = text
        spawnTime = System.currentTimeMillis()
    }

    LaunchedEffect(currentRound, activeLevel) {
        generateNewRound()
    }

    fun handleAnswer(userClickedTrue: Boolean) {
        val actualMatch = (cardColorPair.second == wordText)
        val now = System.currentTimeMillis()
        val reaction = (now - spawnTime).toInt().coerceIn(150, 950)
        reactionTimes.add(reaction)

        if (userClickedTrue == actualMatch) {
            hits++
            score += (10 * levelInfo.speedMultiplier).toInt().coerceAtLeast(10)
            feedbackMessage = "✓ Correct! (+10)"
        } else {
            misses++
            feedbackMessage = "✗ Incorrect!"
        }

        if (currentRound < totalRounds) {
            currentRound++
            generateNewRound()
        } else {
            sessionCompleted = true
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Box(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                RealtimeGameTopHeader(
                    title = "Color Recognition",
                    currentLevel = activeLevel,
                    score = score,
                    onLevelChanged = onLevelChanged,
                    onBack = onCancel
                )

                Spacer(Modifier.height(20.dp))

                Text("Round $currentRound of $totalRounds", style = titleStyle(26.sp, color = Ink), textAlign = TextAlign.Center)
                Spacer(Modifier.height(4.dp))
                Text("Does text match background color?", style = bodyStyle(15.sp, color = Color.Gray), textAlign = TextAlign.Center)

                Spacer(Modifier.height(30.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .padding(horizontal = 10.dp)
                        .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(28.dp)),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColorPair.first),
                    elevation = CardDefaults.cardElevation(6.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = wordText,
                            fontFamily = Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 42.sp,
                            color = Color.Black
                        )
                    }
                }

                if (feedbackMessage.isNotBlank()) {
                    Spacer(Modifier.height(14.dp))
                    Text(
                        feedbackMessage,
                        style = titleStyle(16.sp, color = if (feedbackMessage.startsWith("✓")) Green else Color.Red),
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.weight(1f))

                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 36.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = { handleAnswer(true) },
                        modifier = Modifier
                            .weight(1f)
                            .height(62.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("TRUE", fontFamily = Serif, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Button(
                        onClick = { handleAnswer(false) },
                        modifier = Modifier
                            .weight(1f)
                            .height(62.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                    ) {
                        Text("FALSE", fontFamily = Serif, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            if (sessionCompleted) {
                val finalAcc = if (totalRounds > 0) (hits * 100) / totalRounds else 100
                val avgReact = if (reactionTimes.isNotEmpty()) reactionTimes.average().toInt() else 240

                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(0.75f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                            .border(1.dp, CardBorder, RoundedCornerShape(26.dp)),
                        shape = RoundedCornerShape(26.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        elevation = CardDefaults.cardElevation(10.dp)
                    ) {
                        Column(
                            Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🏆", fontSize = 48.sp)
                            Spacer(Modifier.height(6.dp))
                            Text("Session Completed!", style = titleStyle(24.sp, color = Ink), textAlign = TextAlign.Center)
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Color Recognition", style = bodyStyle(15.sp, color = Green), fontWeight = FontWeight.Bold)
                                ContainerLevelBadge(level = activeLevel)
                            }

                            Spacer(Modifier.height(20.dp))

                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Final Score", style = bodyStyle(12.sp, color = Muted))
                                    Text("$score pts", style = titleStyle(22.sp, color = Ink))
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Accuracy", style = bodyStyle(12.sp, color = Muted))
                                    Text("$finalAcc%", style = titleStyle(22.sp, color = Green))
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Avg Speed", style = bodyStyle(12.sp, color = Blue))
                                    Text("${avgReact}ms", style = titleStyle(22.sp, color = Blue))
                                }
                            }

                            Spacer(Modifier.height(24.dp))

                            Button(
                                onClick = {
                                    val result = SessionResult(
                                        exerciseTitle = exercise.title,
                                        hits = hits,
                                        totalTargets = totalRounds,
                                        accuracyPct = finalAcc,
                                        avgReactionMs = avgReact,
                                        durationSeconds = 30,
                                        level = activeLevel,
                                        levelLabel = "Level $activeLevel • ${levelInfo.name}"
                                    )
                                    onSessionFinished(result)
                                },
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Green)
                            ) {
                                Text("Save Results & Continue", fontFamily = Serif, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Peripheral Vision Session Game ──
@Composable
private fun PeripheralVisionGame(
    exercise: Exercise,
    activeLevel: Int,
    onLevelChanged: (Int) -> Unit,
    onSessionFinished: (SessionResult) -> Unit,
    onCancel: () -> Unit
) {
    val levelInfo = remember(activeLevel) { getLevelDetails(activeLevel) }
    val totalRounds = levelInfo.roundCount
    var currentRound by remember { mutableIntStateOf(1) }
    var score by remember { mutableIntStateOf(0) }
    var hits by remember { mutableIntStateOf(0) }
    var misses by remember { mutableIntStateOf(0) }
    val reactionTimes = remember { mutableStateListOf<Int>() }
    var sessionCompleted by remember { mutableStateOf(false) }

    var targetOffsetX by remember { mutableFloatStateOf(0f) }
    var targetOffsetY by remember { mutableFloatStateOf(0f) }
    var spawnTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    fun generateNewTarget() {
        val angle = (0..360).random() * (Math.PI / 180.0)
        val distance = (60 + (activeLevel * 16)).toFloat()
        targetOffsetX = (kotlin.math.cos(angle) * distance).toFloat()
        targetOffsetY = (kotlin.math.sin(angle) * distance).toFloat()
        spawnTime = System.currentTimeMillis()
    }

    LaunchedEffect(currentRound, activeLevel) {
        generateNewTarget()
    }

    fun handleTargetTap() {
        val now = System.currentTimeMillis()
        val reaction = (now - spawnTime).toInt().coerceIn(150, 950)
        reactionTimes.add(reaction)
        hits++
        score += (10 * levelInfo.speedMultiplier).toInt().coerceAtLeast(10)

        if (currentRound < totalRounds) {
            currentRound++
        } else {
            sessionCompleted = true
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Box(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                RealtimeGameTopHeader(
                    title = "Peripheral Vision",
                    currentLevel = activeLevel,
                    score = score,
                    onLevelChanged = onLevelChanged,
                    onBack = onCancel
                )

                Spacer(Modifier.height(18.dp))

                Text("Round $currentRound of $totalRounds", style = titleStyle(26.sp, color = Ink), textAlign = TextAlign.Center)
                Spacer(Modifier.height(4.dp))
                Text("Focus on red center dot. Tap yellow peripheral dot!", style = bodyStyle(14.sp, color = Color.Gray), textAlign = TextAlign.Center)

                Spacer(Modifier.weight(1f))

                // Play Field Area with Center Red Dot & Dynamic Peripheral Yellow Target
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val targetSize = levelInfo.targetSizeDp.dp
                    Box(
                        modifier = Modifier
                            .offset(x = targetOffsetX.dp, y = targetOffsetY.dp)
                            .size(targetSize)
                            .clip(RoundedCornerShape(targetSize / 2))
                            .background(Color(0xFFFFFF00))
                            .clickable { handleTargetTap() }
                    )

                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .background(Color.Red)
                    )
                }

                Spacer(Modifier.weight(1f))
            }

            if (sessionCompleted) {
                val finalAcc = if (totalRounds > 0) (hits * 100) / totalRounds else 100
                val avgReact = if (reactionTimes.isNotEmpty()) reactionTimes.average().toInt() else 220

                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(0.75f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                            .border(1.dp, CardBorder, RoundedCornerShape(26.dp)),
                        shape = RoundedCornerShape(26.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        elevation = CardDefaults.cardElevation(10.dp)
                    ) {
                        Column(
                            Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🏆", fontSize = 48.sp)
                            Spacer(Modifier.height(6.dp))
                            Text("Session Completed!", style = titleStyle(24.sp, color = Ink), textAlign = TextAlign.Center)
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Peripheral Vision", style = bodyStyle(15.sp, color = Green), fontWeight = FontWeight.Bold)
                                ContainerLevelBadge(level = activeLevel)
                            }

                            Spacer(Modifier.height(20.dp))

                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Final Score", style = bodyStyle(12.sp, color = Muted))
                                    Text("$score pts", style = titleStyle(22.sp, color = Ink))
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Accuracy", style = bodyStyle(12.sp, color = Muted))
                                    Text("$finalAcc%", style = titleStyle(22.sp, color = Green))
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Avg Speed", style = bodyStyle(12.sp, color = Blue))
                                    Text("${avgReact}ms", style = titleStyle(22.sp, color = Blue))
                                }
                            }

                            Spacer(Modifier.height(24.dp))

                            Button(
                                onClick = {
                                    val result = SessionResult(
                                        exerciseTitle = "Peripheral Vision",
                                        hits = hits,
                                        totalTargets = totalRounds,
                                        accuracyPct = finalAcc,
                                        avgReactionMs = avgReact,
                                        durationSeconds = 30,
                                        level = activeLevel,
                                        levelLabel = "Level $activeLevel • ${levelInfo.name}"
                                    )
                                    onSessionFinished(result)
                                },
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Green)
                            ) {
                                Text("Save Results & Continue", fontFamily = Serif, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}


// ── Visual Scanning Session Game ──
@Composable
private fun VisualScanningGame(
    exercise: Exercise,
    activeLevel: Int,
    onLevelChanged: (Int) -> Unit,
    onSessionFinished: (SessionResult) -> Unit,
    onCancel: () -> Unit
) {
    val levelInfo = remember(activeLevel) { getLevelDetails(activeLevel) }
    val totalRounds = levelInfo.roundCount
    var currentRound by remember { mutableIntStateOf(1) }
    var score by remember { mutableIntStateOf(0) }
    var hits by remember { mutableIntStateOf(0) }
    var misses by remember { mutableIntStateOf(0) }
    val reactionTimes = remember { mutableStateListOf<Int>() }
    var sessionCompleted by remember { mutableStateOf(false) }

    val availableSymbols = remember { listOf("🔍", "❤️", "🔧", "🏠", "⭐", "😊") }
    var targetSymbol by remember { mutableStateOf(availableSymbols[0]) }
    val gridSymbols = remember { mutableStateListOf<String>() }
    var spawnTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    val gridCols = remember(activeLevel) { (3 + activeLevel).coerceAtMost(6) }
    val gridRows = remember(activeLevel) { (3 + (activeLevel / 2)).coerceAtMost(6) }
    val totalGridCells = gridCols * gridRows

    fun generateNewRound() {
        val target = availableSymbols.random()
        targetSymbol = target
        val list = mutableListOf<String>()
        list.add(target)
        val distractors = availableSymbols.filter { it != target }
        for (i in 0 until (totalGridCells - 1)) {
            list.add(distractors.random())
        }
        list.shuffle()
        gridSymbols.clear()
        gridSymbols.addAll(list)
        spawnTime = System.currentTimeMillis()
    }

    LaunchedEffect(currentRound, activeLevel) {
        generateNewRound()
    }

    fun handleSymbolTap(symbol: String) {
        val now = System.currentTimeMillis()
        val reaction = (now - spawnTime).toInt().coerceIn(160, 950)
        reactionTimes.add(reaction)

        if (symbol == targetSymbol) {
            hits++
            score += (10 * levelInfo.speedMultiplier).toInt().coerceAtLeast(10)
            if (currentRound < totalRounds) {
                currentRound++
            } else {
                sessionCompleted = true
            }
        } else {
            misses++
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Box(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                RealtimeGameTopHeader(
                    title = "Visual Scanning",
                    currentLevel = activeLevel,
                    score = score,
                    onLevelChanged = onLevelChanged,
                    onBack = onCancel
                )

                Spacer(Modifier.height(14.dp))

                Text("Round $currentRound of $totalRounds", style = titleStyle(26.sp, color = Ink), textAlign = TextAlign.Center)
                Spacer(Modifier.height(2.dp))
                Text("Find this symbol:", style = bodyStyle(14.sp, color = Ink), textAlign = TextAlign.Center)
                Spacer(Modifier.height(4.dp))
                Text(targetSymbol, fontSize = 34.sp)

                Spacer(Modifier.height(16.dp))

                // Dynamic Grid of Symbols based on Level
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    for (row in 0 until gridRows) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            for (col in 0 until gridCols) {
                                val index = row * gridCols + col
                                if (index < gridSymbols.size) {
                                    val sym = gridSymbols[index]
                                    Box(
                                        modifier = Modifier
                                            .size((280 / gridCols).coerceIn(36, 56).dp)
                                            .clickable { handleSymbolTap(sym) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(sym, fontSize = (220 / gridCols).coerceIn(20, 32).sp)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.weight(1f))
            }

            if (sessionCompleted) {
                val finalAcc = if (hits + misses > 0) (hits * 100) / (hits + misses) else 100
                val avgReact = if (reactionTimes.isNotEmpty()) reactionTimes.average().toInt() else 230

                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(0.75f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                            .border(1.dp, CardBorder, RoundedCornerShape(26.dp)),
                        shape = RoundedCornerShape(26.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        elevation = CardDefaults.cardElevation(10.dp)
                    ) {
                        Column(
                            Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🏆", fontSize = 48.sp)
                            Spacer(Modifier.height(6.dp))
                            Text("Session Completed!", style = titleStyle(24.sp, color = Ink), textAlign = TextAlign.Center)
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Visual Scanning", style = bodyStyle(15.sp, color = Green), fontWeight = FontWeight.Bold)
                                ContainerLevelBadge(level = activeLevel)
                            }

                            Spacer(Modifier.height(20.dp))

                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Final Score", style = bodyStyle(12.sp, color = Muted))
                                    Text("$score pts", style = titleStyle(22.sp, color = Ink))
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Accuracy", style = bodyStyle(12.sp, color = Muted))
                                    Text("$finalAcc%", style = titleStyle(22.sp, color = Green))
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Avg Speed", style = bodyStyle(12.sp, color = Blue))
                                    Text("${avgReact}ms", style = titleStyle(22.sp, color = Blue))
                                }
                            }

                            Spacer(Modifier.height(24.dp))

                            Button(
                                onClick = {
                                    val result = SessionResult(
                                        exerciseTitle = "Visual Scanning",
                                        hits = hits,
                                        totalTargets = totalRounds,
                                        accuracyPct = finalAcc,
                                        avgReactionMs = avgReact,
                                        durationSeconds = 30,
                                        level = activeLevel,
                                        levelLabel = "Level $activeLevel • ${levelInfo.name}"
                                    )
                                    onSessionFinished(result)
                                },
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Green)
                            ) {
                                Text("Save Results & Continue", fontFamily = Serif, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}


// ── Memory Sequence Session Game ──
@Composable
private fun MemorySequenceGame(
    exercise: Exercise,
    activeLevel: Int,
    onLevelChanged: (Int) -> Unit,
    onSessionFinished: (SessionResult) -> Unit,
    onCancel: () -> Unit
) {
    val levelInfo = remember(activeLevel) { getLevelDetails(activeLevel) }
    val totalRounds = levelInfo.roundCount
    var currentRound by remember { mutableIntStateOf(1) }
    var score by remember { mutableIntStateOf(0) }
    var hits by remember { mutableIntStateOf(0) }
    var misses by remember { mutableIntStateOf(0) }
    val reactionTimes = remember { mutableStateListOf<Int>() }
    var sessionCompleted by remember { mutableStateOf(false) }

    val padColors = remember {
        listOf(
            Color(0xFFFF8A80),
            Color(0xFFA5D6A7),
            Color(0xFF90CAF9),
            Color(0xFFFFF59D)
        )
    }

    val activeSequence = remember { mutableStateListOf<Int>() }
    val userSequence = remember { mutableStateListOf<Int>() }
    var highlightedPad by remember { mutableIntStateOf(-1) }
    var isShowingSequence by remember { mutableStateOf(true) }
    var promptText by remember { mutableStateOf("Watch closely...") }
    var spawnTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    fun generateNewSequence() {
        activeSequence.clear()
        userSequence.clear()
        val count = (2 + activeLevel).coerceAtMost(7)
        for (i in 0 until count) {
            activeSequence.add((0..3).random())
        }
        isShowingSequence = true
        promptText = "Watch closely..."
    }

    LaunchedEffect(currentRound, activeLevel) {
        generateNewSequence()
        val flashDelay = (500L / levelInfo.speedMultiplier).toLong().coerceAtLeast(180L)
        for (padIndex in activeSequence) {
            delay(flashDelay)
            highlightedPad = padIndex
            delay(flashDelay)
            highlightedPad = -1
        }
        isShowingSequence = false
        promptText = "Your Turn! Repeat the sequence."
        spawnTime = System.currentTimeMillis()
    }

    fun handlePadClick(index: Int) {
        if (isShowingSequence) return
        val now = System.currentTimeMillis()
        val reaction = (now - spawnTime).toInt().coerceIn(150, 950)
        reactionTimes.add(reaction)
        userSequence.add(index)

        val currentStep = userSequence.size - 1
        if (userSequence[currentStep] == activeSequence[currentStep]) {
            if (userSequence.size == activeSequence.size) {
                hits++
                score += (10 * levelInfo.speedMultiplier).toInt().coerceAtLeast(10)
                if (currentRound < totalRounds) {
                    currentRound++
                } else {
                    sessionCompleted = true
                }
            }
        } else {
            misses++
            userSequence.clear()
            promptText = "Incorrect! Try again."
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Box(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                RealtimeGameTopHeader(
                    title = "Memory Sequence",
                    currentLevel = activeLevel,
                    score = score,
                    onLevelChanged = onLevelChanged,
                    onBack = onCancel
                )

                Spacer(Modifier.height(20.dp))

                Text("Round $currentRound of $totalRounds", style = titleStyle(26.sp, color = Ink), textAlign = TextAlign.Center)
                Spacer(Modifier.height(4.dp))
                Text(promptText, style = bodyStyle(15.sp, color = Color.Gray), textAlign = TextAlign.Center)

                Spacer(Modifier.height(30.dp))

                // 2x2 Grid of Color Pads
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    for (row in 0..1) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            for (col in 0..1) {
                                val padIndex = row * 2 + col
                                val isHighlighted = (highlightedPad == padIndex)
                                val color = padColors[padIndex]

                                Card(
                                    modifier = Modifier
                                        .size(130.dp)
                                        .clickable { handlePadClick(padIndex) }
                                        .border(
                                            width = if (isHighlighted) 4.dp else 1.dp,
                                            color = if (isHighlighted) Color.White else Color.Transparent,
                                            shape = RoundedCornerShape(22.dp)
                                        ),
                                    shape = RoundedCornerShape(22.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isHighlighted) color.copy(alpha = 0.6f) else color
                                    ),
                                    elevation = CardDefaults.cardElevation(if (isHighlighted) 8.dp else 2.dp)
                                ) {}
                            }
                        }
                    }
                }

                Spacer(Modifier.weight(1f))
            }

            if (sessionCompleted) {
                val finalAcc = if (hits + misses > 0) (hits * 100) / (hits + misses) else 100
                val avgReact = if (reactionTimes.isNotEmpty()) reactionTimes.average().toInt() else 210

                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(0.75f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                            .border(1.dp, CardBorder, RoundedCornerShape(26.dp)),
                        shape = RoundedCornerShape(26.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        elevation = CardDefaults.cardElevation(10.dp)
                    ) {
                        Column(
                            Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🏆", fontSize = 48.sp)
                            Spacer(Modifier.height(6.dp))
                            Text("Session Completed!", style = titleStyle(24.sp, color = Ink), textAlign = TextAlign.Center)
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Memory Sequence", style = bodyStyle(15.sp, color = Green), fontWeight = FontWeight.Bold)
                                ContainerLevelBadge(level = activeLevel)
                            }

                            Spacer(Modifier.height(20.dp))

                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Final Score", style = bodyStyle(12.sp, color = Muted))
                                    Text("$score pts", style = titleStyle(22.sp, color = Ink))
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Accuracy", style = bodyStyle(12.sp, color = Muted))
                                    Text("$finalAcc%", style = titleStyle(22.sp, color = Green))
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Avg Speed", style = bodyStyle(12.sp, color = Blue))
                                    Text("${avgReact}ms", style = titleStyle(22.sp, color = Blue))
                                }
                            }

                            Spacer(Modifier.height(24.dp))

                            Button(
                                onClick = {
                                    val result = SessionResult(
                                        exerciseTitle = "Memory Sequence",
                                        hits = hits,
                                        totalTargets = totalRounds,
                                        accuracyPct = finalAcc,
                                        avgReactionMs = avgReact,
                                        durationSeconds = 30,
                                        level = activeLevel,
                                        levelLabel = "Level $activeLevel • ${levelInfo.name}"
                                    )
                                    onSessionFinished(result)
                                },
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Green)
                            ) {
                                Text("Save Results & Continue", fontFamily = Serif, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Reports Screen with Saved Sessions List ──
@Composable private fun ReportsScreen(
    completedSessions: List<SessionResult>,
    onNavigate: (String) -> Unit,
    onDeleteSession: ((SessionResult) -> Unit)? = null
) {
    val totalSessions = completedSessions.size
    val avgAcc = if (completedSessions.isNotEmpty()) completedSessions.map { it.accuracyPct }.average().toInt() else 0
    val totalMins = totalSessions * 10

    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Header("Analytics & History") }
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Green),
                shape = RoundedCornerShape(22.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(Modifier.padding(24.dp)) {
                    Text("Today's Performance", style = titleStyle(18.sp, color = Color.White))
                    Spacer(Modifier.height(18.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        listOf("$totalSessions\nSessions", "$avgAcc%\nAccuracy", "$totalMins\nMinutes").forEach {
                            Text(it, style = titleStyle(22.sp, color = Color.White), textAlign = TextAlign.Center)
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Text("Analytics", style = titleStyle(21.sp, color = Color.White))
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                AnalyticsItem("▣", "Weekly") { onNavigate("Weekly") }
                AnalyticsItem("⌁", "Accuracy") { onNavigate("Trend") }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                AnalyticsItem("◔", "Reaction") { onNavigate("Reaction") }
                AnalyticsItem("↶", "History") { onNavigate("History") }
            }
        }
        item {
            Text("Recent Saved Sessions", style = titleStyle(21.sp, color = Color.White))
        }

        if (completedSessions.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Text(
                        "No sessions recorded yet. Start training to save your progress!",
                        style = bodyStyle(15.sp, color = Muted),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(28.dp)
                    )
                }
            }
        } else {
            items(completedSessions) { session ->
                Card(
                    modifier = Modifier.fillMaxWidth().border(1.dp, CardBorder, RoundedCornerShape(18.dp)),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    elevation = CardDefaults.cardElevation(3.dp)
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(48.dp).background(FieldBg, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                            Text("✅", fontSize = 22.sp)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(session.exerciseTitle, style = titleStyle(15.sp, color = Ink))
                                ContainerLevelBadge(level = session.level)
                            }
                            Spacer(Modifier.height(2.dp))
                            Text(session.timestamp, style = bodyStyle(12.sp, color = Muted))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("${session.accuracyPct}% Acc", style = titleStyle(15.sp, color = Green))
                            Text("${session.avgReactionMs} ms", style = bodyStyle(12.sp, color = Blue))
                        }
                        if (onDeleteSession != null) {
                            Spacer(Modifier.width(8.dp))
                            IconButton(onClick = { onDeleteSession(session) }, modifier = Modifier.size(32.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Delete Session",
                                    tint = Color.LightGray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable private fun AnalyticsItem(icon: String, label: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.size(140.dp, 100.dp).clickable { onClick() }.border(1.dp, CardBorder, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(icon, color = Green, fontSize = 32.sp)
            Spacer(Modifier.height(4.dp))
            Text(label, style = titleStyle(15.sp, color = Ink))
        }
    }
}

@Composable private fun TrendScreen(title: String, sessions: List<SessionResult>, onBack: () -> Unit) {
    val avgVal = if (sessions.isNotEmpty()) {
        if (title.contains("Accuracy")) "${sessions.map { it.accuracyPct }.average().toInt()}%" else "${sessions.map { it.avgReactionMs }.average().toInt()} ms"
    } else {
        if (title.contains("Accuracy")) "0%" else "0ms"
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        Header(title, onBack)
        Card(
            Modifier.padding(top = 24.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                Modifier.fillMaxWidth().height(360.dp).padding(28.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(if (title.contains("Accuracy")) "Accuracy Over Time" else "Reaction Time (ms)", style = titleStyle(20.sp, color = Ink))
                Text("Average: $avgVal", style = titleStyle(26.sp, color = Green), modifier = Modifier.align(Alignment.CenterHorizontally))
                Text("Total Saved Sessions Recorded: ${sessions.size}", style = bodyStyle(15.sp, color = Muted), modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable private fun SessionHistoryScreen(
    sessions: List<SessionResult>,
    onBack: () -> Unit,
    onDeleteSession: ((SessionResult) -> Unit)? = null
) {
    Column(Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        Header("Exercise History", onBack)
        Spacer(Modifier.height(16.dp))

        if (sessions.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No exercise history found.", style = titleStyle(19.sp, color = Color.White))
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(sessions) { session ->
                    Card(
                        modifier = Modifier.fillMaxWidth().border(1.dp, CardBorder, RoundedCornerShape(18.dp)),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        elevation = CardDefaults.cardElevation(3.dp)
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(48.dp).background(FieldBg, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                                Text("🎯", fontSize = 22.sp)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(session.exerciseTitle, style = titleStyle(15.sp, color = Ink))
                                    ContainerLevelBadge(level = session.level)
                                }
                                Spacer(Modifier.height(2.dp))
                                Text(session.timestamp, style = bodyStyle(12.sp, color = Muted))
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("${session.accuracyPct}% Acc", style = titleStyle(15.sp, color = Green))
                                Text("${session.avgReactionMs} ms", style = bodyStyle(12.sp, color = Blue))
                            }
                            if (onDeleteSession != null) {
                                Spacer(Modifier.width(8.dp))
                                IconButton(onClick = { onDeleteSession(session) }, modifier = Modifier.size(32.dp)) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Delete Session",
                                        tint = Color.LightGray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Real-Time Weekly Progress Screen ────────────────────────────────────
@Composable private fun WeeklyScreen(sessions: List<SessionResult>, onBack: () -> Unit) {
    val currentDayName = remember { SimpleDateFormat("EEE", Locale.US).format(Date()) }
    val weekDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    val dayCounts = remember(sessions) {
        val counts = mutableMapOf("Mon" to 0, "Tue" to 0, "Wed" to 0, "Thu" to 0, "Fri" to 0, "Sat" to 0, "Sun" to 0)
        val sdfDay = SimpleDateFormat("EEE", Locale.US)
        sessions.forEach { s ->
            val dayStr = if (s.dayOfWeek.isNotBlank()) {
                s.dayOfWeek
            } else {
                try { sdfDay.format(Date(s.dateMillis)) } catch (_: Exception) { currentDayName }
            }
            val matched = weekDays.firstOrNull { it.equals(dayStr, ignoreCase = true) } ?: currentDayName
            counts[matched] = (counts[matched] ?: 0) + 1
        }
        weekDays.map { counts[it] ?: 0 }
    }

    val totalWeeklySessions = sessions.size
    val avgAcc = if (sessions.isNotEmpty()) sessions.map { it.accuracyPct }.average().toInt() else 0
    val activeDaysCount = dayCounts.count { it > 0 }
    val maxCount = (dayCounts.maxOrNull() ?: 1).coerceAtLeast(1)

    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Header("Weekly Progress", onBack) }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Green),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(Modifier.padding(24.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Weekly Summary", style = titleStyle(20.sp, color = Color.White))
                            Text("Real-Time Activity Tracking", style = bodyStyle(13.sp, color = Color(0xFFE8F5E9)))
                        }
                        Text("$activeDaysCount / 7 Days", style = titleStyle(18.sp, color = Color.White))
                    }
                    Spacer(Modifier.height(20.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Sessions", style = bodyStyle(12.sp, color = Color(0xFFC8E6C9)))
                            Text("$totalWeeklySessions", style = titleStyle(26.sp, color = Color.White))
                        }
                        Column {
                            Text("Average Accuracy", style = bodyStyle(12.sp, color = Color(0xFFC8E6C9)))
                            Text("$avgAcc%", style = titleStyle(26.sp, color = Color.White))
                        }
                        Column {
                            Text("Active Streak", style = bodyStyle(12.sp, color = Color(0xFFC8E6C9)))
                            Text("${activeDaysCount} Days 🔥", style = titleStyle(22.sp, color = Color.White))
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardBorder, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(Modifier.padding(22.dp)) {
                    Text("Daily Activity Breakdown", style = titleStyle(18.sp, color = Ink))
                    Spacer(Modifier.height(4.dp))
                    Text("Completed therapy sessions this week", style = bodyStyle(13.sp, color = Muted))

                    Spacer(Modifier.height(24.dp))

                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        weekDays.forEachIndexed { index, day ->
                            val count = dayCounts[index]
                            val heightFrac = if (count > 0) (count.toFloat() / maxCount).coerceIn(0.25f, 1f) else 0.08f
                            val isCurrentDay = day.equals(currentDayName, ignoreCase = true)

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    if (count > 0) "$count" else "-",
                                    style = bodyStyle(12.sp, color = if (count > 0) Green else Muted),
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(4.dp))
                                Box(
                                    Modifier
                                        .width(26.dp)
                                        .height((100 * heightFrac).dp)
                                        .background(
                                            if (count > 0) Green else Color(0xFFE0E0E0),
                                            RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                                        )
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    day,
                                    style = bodyStyle(13.sp, color = if (isCurrentDay) Green else Ink),
                                    fontWeight = if (isCurrentDay) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Text("Weekly Milestones", style = titleStyle(20.sp, color = Color.White), modifier = Modifier.padding(top = 4.dp))
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardBorder, RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                elevation = CardDefaults.cardElevation(3.dp)
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("🎯", fontSize = 28.sp, modifier = Modifier.width(42.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Consistency Goal", style = titleStyle(16.sp, color = Ink))
                        Text("Train 5 days per week to maintain neuro-plasticity", style = bodyStyle(13.sp, color = Muted))
                    }
                    Text(if (activeDaysCount >= 5 || sessions.isNotEmpty()) "Target Met! ✅" else "In Progress", style = bodyStyle(12.sp, color = Green), fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── Profile Screen Matching User Screenshot ────────────────────────────────
@Composable private fun ProfileScreen(
    userProfile: UserProfile,
    completedSessionsCount: Int,
    onProfileUpdated: (UserProfile) -> Unit,
    onSignOut: () -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var showSavedMessage by remember { mutableStateOf(false) }

    var fullName by remember { mutableStateOf(userProfile.fullName) }
    var email by remember { mutableStateOf(userProfile.email) }
    var phone by remember { mutableStateOf(if (userProfile.phone.contains("00000")) "9160542295" else userProfile.phone) }

    val initialAgeGender = remember(userProfile.ageGender) {
        val parts = userProfile.ageGender.split("•")
        val a = if (parts.isNotEmpty() && parts[0].isNotBlank()) parts[0].trim() else "23 years"
        val g = if (parts.size > 1 && parts[1].isNotBlank()) parts[1].trim() else "Male"
        Pair(a, g)
    }

    var age by remember { mutableStateOf(initialAgeGender.first) }
    var gender by remember { mutableStateOf(initialAgeGender.second) }
    var condition by remember { mutableStateOf("Stroke Recovery") }
    var goals by remember { mutableStateOf("Enhance Hand-Eye Coordination") }

    // Real-time auto sync when logged-in patient details change
    LaunchedEffect(userProfile) {
        fullName = userProfile.fullName
        email = userProfile.email
        phone = if (userProfile.phone.contains("00000")) "9160542295" else userProfile.phone
        val parts = userProfile.ageGender.split("•")
        age = if (parts.isNotEmpty() && parts[0].isNotBlank()) parts[0].trim() else "23 years"
        gender = if (parts.size > 1 && parts[1].isNotBlank()) parts[1].trim() else "Male"
    }

    val initialLetter = remember(fullName) {
        fullName.trim().firstOrNull()?.uppercase() ?: "S"
    }
    val firstName = remember(fullName) {
        fullName.trim().split(" ").firstOrNull() ?: "shiva"
    }

    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Header("Profile") }

        item {
            // White Container Card for high visibility matching screenshot
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardBorder, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Large Blue Circular Avatar with First Letter (e.g., 'S')
                    Box(
                        Modifier
                            .size(100.dp)
                            .background(Color(0xFF1E88E5), RoundedCornerShape(50.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initialLetter,
                            style = TextStyle(
                                fontFamily = Serif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 44.sp,
                                color = Color.White
                            )
                        )
                    }

                    Spacer(Modifier.height(14.dp))
                    Text(firstName, style = titleStyle(24.sp, color = Ink), textAlign = TextAlign.Center)
                    Spacer(Modifier.height(2.dp))
                    Text(email, style = bodyStyle(13.sp, color = Color.Gray), textAlign = TextAlign.Center)
                    Spacer(Modifier.height(4.dp))
                    Text("Welcome, $firstName", style = titleStyle(16.sp, color = Color(0xFF1E88E5)), textAlign = TextAlign.Center)

                    Spacer(Modifier.height(20.dp))

                    // Fields with subtle dividers matching screenshot
                    ProfileLineItem("User ID", "8", false, {})
                    ProfileLineItem("Full Name", fullName, isEditing) { fullName = it }
                    ProfileLineItem("Email", email, isEditing) { email = it }
                    ProfileLineItem("Phone", phone, isEditing) { phone = it }
                    ProfileLineItem("Age", age, isEditing) { age = it }
                    ProfileLineItem("Gender", gender, isEditing) { gender = it }

                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Medical Information",
                        style = bodyStyle(13.sp, color = Color.Gray),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )

                    ProfileLineItem("Condition", condition, isEditing) { condition = it }
                    ProfileLineItem("Goals", goals, isEditing) { goals = it }

                    Spacer(Modifier.height(24.dp))

                    // Rounded Outline Edit Profile Button matching screenshot
                    OutlinedButton(
                        onClick = {
                            if (isEditing) {
                                showSavedMessage = true
                                onProfileUpdated(
                                    userProfile.copy(
                                        fullName = fullName,
                                        email = email,
                                        phone = phone,
                                        ageGender = "$age • $gender",
                                        therapyGoal = goals
                                    )
                                )
                            }
                            isEditing = !isEditing
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(26.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBDEFB)),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
                    ) {
                        Text(
                            if (isEditing) "Save Profile" else "Edit Profile",
                            fontFamily = Serif,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E88E5)
                        )
                    }

                    if (showSavedMessage) {
                        Spacer(Modifier.height(10.dp))
                        Text("Profile updated successfully! ✅", style = bodyStyle(13.sp, color = Green), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            OutlinedButton(
                onClick = onSignOut,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = CardWhite, contentColor = Color(0xFFC62828)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF9A9A))
            ) {
                Text("Sign Out Account", fontFamily = Serif, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable private fun ProfileLineItem(
    label: String,
    value: String,
    isEditing: Boolean,
    onValueChange: (String) -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = bodyStyle(14.sp, color = Color(0xFF555555)))
            if (isEditing) {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    modifier = Modifier.width(180.dp),
                    shape = RoundedCornerShape(10.dp),
                    textStyle = bodyStyle(14.sp, color = Ink)
                )
            } else {
                Text(value, style = titleStyle(14.sp, weight = FontWeight.SemiBold, color = Ink))
            }
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFFEEEEEE))
        )
    }
}

@Composable private fun ProfileStatCard(value: String, label: String, modifier: Modifier) {
    Card(
        modifier = modifier.border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(
            Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = titleStyle(20.sp, color = Green))
            Spacer(Modifier.height(2.dp))
            Text(label, style = bodyStyle(13.sp, color = Muted))
        }
    }
}

@Composable private fun ProfileDetailField(
    label: String,
    value: String,
    isEditing: Boolean,
    onValueChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(label, style = bodyStyle(12.sp, color = Green), fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            if (isEditing) {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = bodyStyle(16.sp, color = Ink)
                )
            } else {
                Text(value, style = titleStyle(16.sp, weight = FontWeight.SemiBold, color = Ink))
            }
        }
    }
}

// ── Mood & Stress Analysis Screen (Matching Image 3 & 10 Patient Questions) ──
private data class MoodQuestion(
    val id: Int,
    val questionText: String,
    val options: List<Pair<String, String>> // Emoji to Label
)

@Composable private fun MoodScreen(
    onSessionFinished: (SessionResult) -> Unit,
    onDone: () -> Unit
) {
    val questions = remember {
        listOf(
            MoodQuestion(
                1,
                "How are you feeling right now?",
                listOf("😊" to "Happy", "😌" to "Calm", "😢" to "Sad", "😠" to "Angry", "😰" to "Stressed", "😴" to "Tired")
            ),
            MoodQuestion(
                2,
                "How was your sleep quality last night?",
                listOf("😴" to "Deep Sleep", "😌" to "Restful", "😐" to "Normal", "🥱" to "Restless", "⚡" to "Insomnia", "😫" to "Exhausted")
            ),
            MoodQuestion(
                3,
                "Are you experiencing any visual fatigue or eye strain?",
                listOf("👁️" to "No Strain", "🙂" to "Very Mild", "😐" to "Moderate", "😣" to "High Strain", "🕶️" to "Light Sensitive", "😫" to "Severe")
            ),
            MoodQuestion(
                4,
                "What is your physical energy level today?",
                listOf("⚡" to "High Energy", "😊" to "Good", "😐" to "Moderate", "🥱" to "Low Energy", "😴" to "Sluggish", "😫" to "Depleted")
            ),
            MoodQuestion(
                5,
                "How well were you able to focus during therapy?",
                listOf("🎯" to "Sharply Focused", "🙂" to "Good Focus", "😐" to "Mild Distraction", "🤯" to "Scattered", "📱" to "Distracted", "🥱" to "Fatigued")
            ),
            MoodQuestion(
                6,
                "Have you felt any headache or dizziness today?",
                listOf("🟢" to "None", "🙂" to "Slight", "😐" to "Intermittent", "🤕" to "Mild Headache", "🌀" to "Dizzy", "🔴" to "Severe")
            ),
            MoodQuestion(
                7,
                "How motivated do you feel for your recovery goals?",
                listOf("🔥" to "Super Motivated", "💪" to "High", "😊" to "Steady", "😐" to "Neutral", "😔" to "Low", "🔋" to "Needs Boost")
            ),
            MoodQuestion(
                8,
                "How calm and relaxed do you feel right now?",
                listOf("🧘" to "Very Peaceful", "😌" to "Relaxed", "🙂" to "Normal", "😬" to "Tense", "😰" to "Anxious", "💥" to "Overwhelmed")
            ),
            MoodQuestion(
                9,
                "Did you take your prescribed therapy rest breaks?",
                listOf("✅" to "All Breaks", "🙂" to "Most Breaks", "😐" to "Some Breaks", "⏸️" to "Skipped Few", "⏳" to "Delayed", "❌" to "No Breaks")
            ),
            MoodQuestion(
                10,
                "How confident do you feel about your neuro-motor progress?",
                listOf("🌟" to "Very Confident", "📈" to "Progressing", "🙂" to "Optimistic", "😐" to "Steady", "🤔" to "Unsure", "🌱" to "Building")
            )
        )
    }

    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var selectedMoodLabel by remember { mutableStateOf("Tired") }
    var stressLevel by remember { mutableFloatStateOf(10f) }
    var assessmentCompleted by remember { mutableStateOf(false) }

    val currentQuestion = questions[currentQuestionIndex]

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Clean Header matching Image 3
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDone) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Ink,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Text("Mood & Stress Analysis", style = titleStyle(20.sp, color = Ink))
                IconButton(onClick = {}) {
                    Text("🕒", fontSize = 22.sp)
                }
            }

            Spacer(Modifier.height(18.dp))

            // Clinical Question Title matching Image 3
            Text(
                currentQuestion.questionText,
                style = titleStyle(24.sp, color = Ink),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(30.dp))

            // 6 Emoji Selection Cards in 2 Rows matching Image 3
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                for (row in 0..1) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        for (col in 0..2) {
                            val optionIndex = row * 3 + col
                            if (optionIndex < currentQuestion.options.size) {
                                val (emoji, label) = currentQuestion.options[optionIndex]
                                val isSelected = (selectedMoodLabel == label)

                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(105.dp)
                                        .clickable { selectedMoodLabel = label }
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) Color(0xFF1E88E5) else Color(0xFFE0E0E0),
                                            shape = RoundedCornerShape(18.dp)
                                        ),
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) Color(0xFFF4F9FF) else Color.White
                                    ),
                                    elevation = CardDefaults.cardElevation(if (isSelected) 4.dp else 1.dp)
                                ) {
                                    Column(
                                        Modifier.fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(emoji, fontSize = 32.sp)
                                        Spacer(Modifier.height(6.dp))
                                        Text(
                                            label,
                                            style = bodyStyle(14.sp, color = if (isSelected) Color(0xFF1E88E5) else Ink),
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // Stress Level Slider matching Image 3
            Text("Your current stress level (1–10)", style = titleStyle(17.sp, color = Ink), textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))

            Slider(
                value = stressLevel,
                onValueChange = { stressLevel = it },
                valueRange = 1f..10f,
                steps = 8,
                colors = androidx.compose.material3.SliderDefaults.colors(
                    thumbColor = Color(0xFF1E88E5),
                    activeTrackColor = Color(0xFF1E88E5),
                    inactiveTrackColor = Color(0xFFBBDEFB)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Low", style = bodyStyle(12.sp, color = Color.Gray))
                Text("High", style = bodyStyle(12.sp, color = Color.Gray))
            }

            Spacer(Modifier.height(6.dp))
            Text(
                stressLevel.toInt().toString(),
                style = titleStyle(26.sp, color = Color(0xFF1E88E5)),
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(24.dp))

            // Action Button matching Image 3
            Button(
                onClick = {
                    if (currentQuestionIndex < questions.size - 1) {
                        currentQuestionIndex++
                    } else {
                        assessmentCompleted = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
            ) {
                Text(
                    if (currentQuestionIndex < questions.size - 1) "Next Question (${currentQuestionIndex + 1}/10)" else "Complete Assessment",
                    fontFamily = Serif,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(Modifier.height(24.dp))
        }

        // Completion Dialog Overlay
        if (assessmentCompleted) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(0.75f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .border(1.dp, CardBorder, RoundedCornerShape(26.dp)),
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    elevation = CardDefaults.cardElevation(10.dp)
                ) {
                    Column(
                        Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("📋", fontSize = 48.sp)
                        Spacer(Modifier.height(6.dp))
                        Text("Clinical Mood Assessment", style = titleStyle(22.sp, color = Ink), textAlign = TextAlign.Center)
                        Spacer(Modifier.height(4.dp))
                        Text("10 Patient Questions Evaluated", style = bodyStyle(14.sp, color = Color(0xFF1E88E5)), fontWeight = FontWeight.Bold)

                        Spacer(Modifier.height(16.dp))

                        Box(
                            Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF4F9FF), RoundedCornerShape(14.dp))
                                .border(1.dp, Color(0xFFBBDEFB), RoundedCornerShape(14.dp))
                                .padding(14.dp)
                        ) {
                            Text(
                                "Clinical Insight: Patient reports state: $selectedMoodLabel with stress index ${stressLevel.toInt()}/10. Recommended 10-min smooth pursuit relaxation before next visual session.",
                                style = bodyStyle(13.sp, color = Ink),
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(Modifier.height(22.dp))

                        Button(
                            onClick = {
                                val moodResult = SessionResult(
                                    exerciseTitle = "Mood & Stress Analysis",
                                    hits = 10,
                                    totalTargets = 10,
                                    accuracyPct = (100 - (stressLevel.toInt() * 4)).coerceIn(60, 100),
                                    avgReactionMs = 180,
                                    durationSeconds = 30
                                )
                                onSessionFinished(moodResult)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
                        ) {
                            Text("Save Assessment & Return Home", fontFamily = Serif, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

private @Composable fun MoodItem(text: String) {}


// ── Authentication Screen with Auto Sign-Up Login & Quick Access ───────
@Composable private fun AuthScreen(
    userProfiles: MutableMap<String, UserProfile>,
    registeredUsers: MutableMap<String, String>,
    onAuthenticated: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var creatingAccount by remember { mutableStateOf(false) }
    var forgotPassword by remember { mutableStateOf(false) }
    var registrationSuccessMessage by remember { mutableStateOf("") }

    when {
        forgotPassword -> ForgotPasswordScreen(
            registeredUsers = registeredUsers,
            onPasswordUpdated = { email, newPass ->
                scope.launch {
                    try {
                        FirebaseAuth.getInstance().sendPasswordResetEmail(email).await()
                    } catch (e: Exception) {
                        Toast.makeText(context, e.message ?: "Could not send reset email.", Toast.LENGTH_LONG).show()
                    }
                }
                forgotPassword = false
                registrationSuccessMessage = "Password reset link sent to $email. Check your inbox."
            },
            onBack = { forgotPassword = false }
        )
        creatingAccount -> SignUpScreen(
            onCreated = { fullName, username, email, phone, age, gender, password ->
                val cleanEmail = email.trim().lowercase()
                val formattedAgeGender = "${if (age.isBlank()) "23" else age} Years • ${if (gender.isBlank()) "Male" else gender}"
                val newProfile = UserProfile(
                    fullName = fullName.ifBlank { "Patient User" },
                    username = username.ifBlank { "patient" },
                    email = cleanEmail,
                    phone = phone.ifBlank { "9160542295" },
                    ageGender = formattedAgeGender
                )
                scope.launch {
                    try {
                        FirebaseAuth.getInstance().createUserWithEmailAndPassword(cleanEmail, password).await()
                        userProfiles[cleanEmail] = newProfile
                        AppStorageManager.saveUser(context, cleanEmail, "", newProfile)
                        FirestoreManager.saveUserProfile(newProfile)
                        onAuthenticated(cleanEmail)
                    } catch (e: Exception) {
                        Toast.makeText(context, e.message ?: "Could not create account.", Toast.LENGTH_LONG).show()
                    }
                }
            },
            onBackToLogin = { creatingAccount = false }
        )
        else -> LoginScreen(
            registeredUsers = registeredUsers,
            successMessage = registrationSuccessMessage,
            onSignIn = { email ->
                onAuthenticated(email)
            },
            onSignUp = {
                registrationSuccessMessage = ""
                creatingAccount = true
            },
            onForgotPassword = { forgotPassword = true }
        )
    }
}

@Composable private fun LoginScreen(
    registeredUsers: Map<String, String>,
    successMessage: String,
    onSignIn: (String) -> Unit,
    onSignUp: () -> Unit,
    onForgotPassword: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Box(Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.bg_welcome_visual_motor),
            contentDescription = "Welcome Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(Modifier.fillMaxSize().background(Color(0xD904140A)))

        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Welcome Back",
                        fontFamily = Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        color = Ink
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Sign in to continue your training",
                        fontFamily = Serif,
                        fontSize = 14.sp,
                        color = Muted,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(18.dp))

                    // Registered Accounts Quick Selection Row
                    if (registeredUsers.isNotEmpty()) {
                        Text(
                            "Tap registered account to auto-fill:",
                            style = bodyStyle(12.sp, color = Green),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(Modifier.height(6.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        ) {
                            items(registeredUsers.keys.toList()) { userEmail ->
                                Card(
                                    modifier = Modifier
                                        .clickable {
                                            email = userEmail
                                            password = registeredUsers[userEmail] ?: ""
                                            errorMessage = ""
                                        }
                                        .border(1.dp, CardBorder, RoundedCornerShape(12.dp)),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = FieldBg)
                                ) {
                                    Text(
                                        userEmail,
                                        style = bodyStyle(12.sp, color = Ink),
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (successMessage.isNotBlank()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                        ) {
                            Text(
                                "✅ " + successMessage,
                                style = bodyStyle(13.sp, color = Green),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    if (errorMessage.isNotBlank()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF9A9A))
                        ) {
                            Text(
                                "⚠️ " + errorMessage,
                                style = bodyStyle(13.sp, color = Color(0xFFC62828)),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    LoginField("Email", email, password = false) {
                        email = it
                        errorMessage = ""
                    }
                    Spacer(Modifier.height(14.dp))
                    LoginField("Password", password, password = true) {
                        password = it
                        errorMessage = ""
                    }

                    TextButton(
                        onClick = onForgotPassword,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(
                            "Forgot Password?",
                            color = Green,
                            fontFamily = Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Button(
                        onClick = {
                            val cleanEmail = email.trim().lowercase()
                            if (cleanEmail.isBlank() || !cleanEmail.endsWith("@gmail.com")) {
                                errorMessage = "Access Denied! Email must be a valid @gmail.com address."
                            } else if (password.isBlank()) {
                                errorMessage = "Please enter your password."
                            } else {
                                scope.launch {
                                    try {
                                        FirebaseAuth.getInstance().signInWithEmailAndPassword(cleanEmail, password).await()
                                        errorMessage = ""
                                        onSignIn(cleanEmail)
                                    } catch (e: Exception) {
                                        errorMessage = e.message ?: "Sign in failed. Check your email and password."
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Green)
                    ) {
                        Text("Sign In", fontFamily = Serif, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Spacer(Modifier.height(18.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Don't have an account?",
                            fontFamily = Serif,
                            fontSize = 14.sp,
                            color = Muted
                        )
                        TextButton(onClick = onSignUp) {
                            Text(
                                "Sign Up",
                                color = Green,
                                fontFamily = Serif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }

                    Text(
                        "Powered by SIMATS Engineering",
                        fontFamily = Serif,
                        fontSize = 12.sp,
                        color = Color(0xFF666666),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable private fun LoginField(
    label: String,
    value: String,
    password: Boolean = false,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                label,
                fontFamily = Serif,
                color = Muted,
                fontSize = 15.sp
            )
        },
        singleLine = true,
        visualTransformation = if (password)
            PasswordVisualTransformation()
        else
            androidx.compose.ui.text.input.VisualTransformation.None,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        textStyle = TextStyle(
            fontFamily = Serif,
            fontSize = 17.sp,
            color = Ink
        ),
        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = Green,
            unfocusedBorderColor = Color(0xFFB0C4B1),
            focusedLabelColor    = Green,
            unfocusedLabelColor  = Muted,
            cursorColor          = Green
        )
    )
}

@Composable private fun SignUpScreen(
    onCreated: (fullName: String, username: String, email: String, phone: String, age: String, gender: String, password: String) -> Unit,
    onBackToLogin: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(Modifier.height(34.dp))
            Image(
                painter = painterResource(id = R.drawable.logo_cyber_eye),
                contentDescription = "App Logo",
                modifier = Modifier.size(80.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text("Create Account", style = titleStyle(28.sp, color = Color.White), textAlign = TextAlign.Center)
            Text("Start your rehabilitation journey", style = bodyStyle(15.sp, color = Color(0xFFE8F5E9)), textAlign = TextAlign.Center)
            Spacer(Modifier.height(20.dp))
        }

        if (errorMessage.isNotBlank()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF9A9A))
                ) {
                    Text(
                        "⚠️ " + errorMessage,
                        style = bodyStyle(13.sp, color = Color(0xFFC62828)),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        item { AuthField("Full Name", fullName) { fullName = it; errorMessage = "" } }
        item { AuthField("Username", username) { username = it; errorMessage = "" } }
        item { AuthField("Email Address (must end with @gmail.com)", email) { email = it; errorMessage = "" } }
        item { AuthField("Mobile Number (10 Digits starting with 6, 7, 8, or 9)", phone) { phone = it; errorMessage = "" } }
        item { AuthField("Age (Years)", age) { age = it; errorMessage = "" } }
        item { AuthField("Gender (e.g. Male / Female)", gender) { gender = it; errorMessage = "" } }
        item { AuthField("Password (at least 6 characters)", password, password = true) { password = it; errorMessage = "" } }
        item { AuthField("Confirm Password", confirmPassword, password = true) { confirmPassword = it; errorMessage = "" } }

        item {
            Text("Primary Reason for Training", style = titleStyle(16.sp, color = Color.White), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
                value = "General Visual-Motor Rehabilitation",
                onValueChange = {},
                enabled = false,
                singleLine = true,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    disabledContainerColor = CardWhite,
                    disabledTextColor = Ink
                )
            )
        }

        item {
            Spacer(Modifier.height(14.dp))
            Button(
                onClick = {
                    val cleanEmail = email.trim().lowercase()
                    val digitsOnly = phone.filter { it.isDigit() }

                    if (fullName.isBlank()) {
                        errorMessage = "Please enter your full name."
                    } else if (cleanEmail.isBlank() || !cleanEmail.endsWith("@gmail.com")) {
                        errorMessage = "Access Denied! Email must be a valid @gmail.com address."
                    } else if (digitsOnly.length != 10) {
                        errorMessage = "Access Denied! Mobile number must be exactly 10 digits."
                    } else if (digitsOnly.firstOrNull() !in listOf('6', '7', '8', '9')) {
                        errorMessage = "Access Denied! Mobile number must start with 6, 7, 8, or 9."
                    } else if (password.length < 6) {
                        errorMessage = "Password must contain at least 6 characters."
                    } else if (password != confirmPassword) {
                        errorMessage = "Passwords do not match!"
                    } else {
                        errorMessage = ""
                        onCreated(fullName, username, cleanEmail, digitsOnly, age, gender, password)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(58.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Green)
            ) {
                Text("Create Account & Enter App", fontFamily = Serif, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            TextButton(onClick = onBackToLogin, modifier = Modifier.fillMaxWidth()) {
                Text("Already have an account? Sign In", color = Color(0xFFE8F5E9), fontFamily = Serif, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable private fun AuthField(label: String, value: String, password: Boolean = false, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontFamily = Serif, color = Muted) },
        singleLine = true,
        visualTransformation = if (password) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        modifier = Modifier.fillMaxWidth().height(66.dp),
        shape = RoundedCornerShape(16.dp),
        textStyle = bodyStyle(17.sp, color = Ink),
        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = CardWhite,
            focusedContainerColor = CardWhite,
            focusedBorderColor = Green,
            unfocusedBorderColor = CardBorder,
            focusedLabelColor = Green,
            unfocusedLabelColor = Muted
        )
    )
}

@Composable private fun ForgotPasswordScreen(
    registeredUsers: Map<String, String>,
    onPasswordUpdated: (String, String) -> Unit,
    onBack: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Box(Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.bg_forest_rain),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(Modifier.fillMaxSize().background(Color(0xF204140A)))

        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "🔐 Reset Password",
                        fontFamily = Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp,
                        color = Ink,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Enter your registered email and we will send a secure reset link.",
                        fontFamily = Serif,
                        fontSize = 14.sp,
                        color = Muted,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(20.dp))

                    if (errorMessage.isNotBlank()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF9A9A))
                        ) {
                            Text(
                                "⚠️ " + errorMessage,
                                style = bodyStyle(13.sp, color = Color(0xFFC62828)),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    LoginField("Registered Email", email, password = false) {
                        email = it
                        errorMessage = ""
                    }
                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = {
                            val cleanEmail = email.trim().lowercase()
                            if (cleanEmail.isBlank() || !cleanEmail.endsWith("@gmail.com")) {
                                errorMessage = "Please enter a valid registered @gmail.com email."
                            } else {
                                errorMessage = ""
                                onPasswordUpdated(cleanEmail, "")
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Green)
                    ) {
                        Text(
                            "Send Password Reset Link",
                            fontFamily = Serif,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "‹ Back to Sign In",
                            color = Green,
                            fontFamily = Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}
