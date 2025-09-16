package com.example.growfit1

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun WorkoutTrackScreen(onDone: () -> Unit) {
    val uid = remember { AuthRepo.uid() }
    var type by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var photoBytes by remember { mutableStateOf<ByteArray?>(null) }
    var loading by remember { mutableStateOf(false) }
    var err by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current

    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            try { ctx.contentResolver.openInputStream(uri)?.use { photoBytes = it.readBytes() } }
            catch (e: Exception) { err = e.message }
        }
    }

    // ולידציה
    val typeError = type.isBlank()
    val durationError = duration.isNotBlank() && (duration.toIntOrNull() == null || (duration.toIntOrNull() ?: 0) < 0)
    val weightError = weight.isNotBlank() && (weight.toFloatOrNull() == null || (weight.toFloatOrNull() ?: 0f) < 0f)
    val formValid = !typeError && !durationError && !weightError && !loading

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Log Workout", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = type, onValueChange = { type = it },
            label = { Text("Type") },
            modifier = Modifier.fillMaxWidth(),
            isError = typeError,
            supportingText = { if (typeError) Text("Required") }
        )
        OutlinedTextField(
            value = duration, onValueChange = { duration = it.filter(Char::isDigit) },
            label = { Text("Duration (min)") },
            modifier = Modifier.fillMaxWidth(),
            isError = durationError,
            supportingText = { if (durationError) Text("Enter non-negative number") }
        )
        OutlinedTextField(
            value = weight, onValueChange = { weight = it.filter { c -> c.isDigit() || c == '.' } },
            label = { Text("Weight (kg)") },
            modifier = Modifier.fillMaxWidth(),
            isError = weightError,
            supportingText = { if (weightError) Text("Enter non-negative number") }
        )
        OutlinedTextField(
            value = notes, onValueChange = { notes = it },
            label = { Text("Notes") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = { pickImage.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
            Text(if (photoBytes != null) "Photo selected ✓ (change)" else "Upload Photo")
        }

        err?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(12.dp))
        Button(
            onClick = {
                if (uid == null) { err = "Not logged in"; return@Button }
                scope.launch {
                    loading = true; err = null
                    try {
                        // ננסה להעלות תמונה — לא חוסם שמירה אם נכשל (כפי שסיכמנו)
                        var url: String? = null
                        if (photoBytes != null) {
                            try { url = StorageRepo.uploadJournalPhoto(uid, photoBytes!!) } catch (_: Exception) {}
                        }
                        val entry = WorkoutEntry(
                            type = type.trim(),
                            durationMin = duration.toIntOrNull() ?: 0,
                            weight = weight.toFloatOrNull() ?: 0f,
                            notes = notes.trim(),
                            photoUrl = url
                        )
                        GrowRepo.addWorkout(uid, entry)
                        onDone()
                    } catch (e: Exception) {
                        err = e.message ?: "Failed to save workout"
                    } finally { loading = false }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = formValid
        ) { Text(if (loading) "Saving..." else "Save") }
    }
}
