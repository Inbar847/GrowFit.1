package com.example.growfit1

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun JournalScreen() {
    val uid = remember { AuthRepo.uid() }
    var list by remember { mutableStateOf(listOf<WorkoutEntry>()) }
    var err by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    LaunchedEffect(uid) {
        if (uid == null) { err = "Not logged in"; return@LaunchedEffect }
        loading = true; err = null
        try { list = GrowRepo.getJournal(uid) }
        catch (e: Exception) { err = e.message ?: "Failed to load journal" }
        finally { loading = false }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Journal", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))

        if (loading) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
        }
        err?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        when {
            list.isEmpty() && !loading && err == null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.FitnessCenter,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("No workouts yet", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Log your first workout to see it here.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            else -> {
                LazyColumn(Modifier.weight(1f)) {
                    items(list, key = { it.id }) { e ->
                        JournalItem(e)
                        Divider()
                    }
                }
            }
        }
    }
}

@Composable
private fun JournalItem(e: WorkoutEntry) {
    val date = remember(e.ts) {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(e.ts))
    }
    Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        ListItem(
            headlineContent = { Text("${e.type} – ${e.durationMin} min, ${e.weight} kg") },
            supportingContent = {
                Column {
                    if (e.notes.isNotBlank()) Text(e.notes)
                    Text(date, style = MaterialTheme.typography.labelMedium)
                }
            }
        )
        if (!e.photoUrl.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            Image(
                painter = rememberAsyncImagePainter(e.photoUrl),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(180.dp),
                contentScale = ContentScale.Crop
            )
        }
    }
}
