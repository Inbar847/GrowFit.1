package com.example.growfit1

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.DisposableEffect
import com.google.firebase.auth.FirebaseAuth

sealed class Dest(val route: String) {
    data object Login   : Dest("login")
    data object Plan    : Dest("plan")
    data object Track   : Dest("track")
    data object Journal : Dest("journal")
    data object Progress: Dest("progress")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNav() {
    val nav = rememberNavController()
    val backStackEntry by nav.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    // מצב התחברות עם מאזין ל-FirebaseAuth (Auto-login / Logout)
    var loggedIn by remember { mutableStateOf(AuthRepo.uid() != null) }

    DisposableEffect(Unit) {
        val auth = FirebaseAuth.getInstance()
        val listener = FirebaseAuth.AuthStateListener { fa ->
            loggedIn = (fa.currentUser != null)
        }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    // startDestination דינמי: אם מחובר → Plan, אחרת → Login
    val start = if (loggedIn) Dest.Plan.route else Dest.Login.route

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titleFor(currentRoute)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),

                navigationIcon = {
                    // חץ חזרה רק אם לא במסך Login ויש מסך קודם
                    if (nav.previousBackStackEntry != null && currentRoute != Dest.Login.route) {
                        IconButton(onClick = { nav.navigateUp() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    // כפתור Logout רק כשמחוברים ולא במסך Login
                    if (loggedIn && currentRoute != Dest.Login.route) {
                        TextButton(onClick = {
                            AuthRepo.signOut()
                            // מעבר למסך Login וניקוי היסטוריה
                            nav.navigate(Dest.Login.route) {
                                popUpTo(nav.graph.startDestinationId) { inclusive = true }
                                launchSingleTop = true
                            }
                        }) { Text("Logout") }
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = nav,
            startDestination = start,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Dest.Login.route) {
                // אחרי התחברות מוצלחת—ננווט ל-Plan (ניקוי back stack)
                LoginScreen(onLoggedIn = {
                    nav.navigate(Dest.Plan.route) {
                        popUpTo(Dest.Login.route) { inclusive = true }
                        launchSingleTop = true
                    }
                })
            }
            composable(Dest.Plan.route) {
                WorkoutPlanScreen(
                    onTrack = { nav.navigate(Dest.Track.route) },
                    onJournal = { nav.navigate(Dest.Journal.route) },
                    onProgress = { nav.navigate(Dest.Progress.route) }
                )
            }
            composable(Dest.Track.route) {
                WorkoutTrackScreen(onDone = { nav.navigateUp() })
            }
            composable(Dest.Journal.route) {
                JournalScreen()
            }
            composable(Dest.Progress.route) {
                ProgressScreen()
            }
        }
    }
}

private fun titleFor(route: String?) = when (route) {
    Dest.Login.route    -> "GrowFit"
    Dest.Plan.route     -> "Workout Plan"
    Dest.Track.route    -> "Log Workout"
    Dest.Journal.route  -> "Journal"
    Dest.Progress.route -> "Progress"
    else -> ""
}
