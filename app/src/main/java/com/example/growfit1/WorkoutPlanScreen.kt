package com.example.growfit1

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.compose.material3.ExperimentalMaterial3Api


@Composable
fun WorkoutPlanScreen(
    onTrack: () -> Unit,
    onJournal: () -> Unit,
    onProgress: () -> Unit
) {
    val uid = remember { AuthRepo.uid() }
    var items by remember { mutableStateOf(listOf<PlanItem>()) }
    var loading by remember { mutableStateOf(false) }
    var err by remember { mutableStateOf<String?>(null) }

    // דיאלוגים
    var showAdd by remember { mutableStateOf(false) }
    var editItem by remember { mutableStateOf<PlanItem?>(null) }
    var deleteItem by remember { mutableStateOf<PlanItem?>(null) }

    val scope = rememberCoroutineScope()

    suspend fun refresh() {
        if (uid == null) { err = "Not logged in"; return }
        loading = true; err = null
        try { items = GrowRepo.getPlan(uid) }
        catch (e: Exception) { err = e.message ?: "Failed to load plan" }
        finally { loading = false }
    }

    LaunchedEffect(uid) { refresh() }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { inner ->
        Column(Modifier.fillMaxSize().padding(inner).padding(16.dp)) {
            Text("Workout Plan", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))

            if (loading) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
            }
            err?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            LazyColumn(Modifier.weight(1f)) {
                items(items, key = { it.id }) { it ->
                    ListItem(
                        headlineContent = { Text("${it.day} – ${it.exercise}") },
                        supportingContent = { Text("${it.sets} x ${it.reps} @ ${it.weight}kg") },
                        trailingContent = {
                            Row {
                                IconButton(onClick = { editItem = it }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                                }
                                IconButton(onClick = { deleteItem = it }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                                }
                            }
                        }
                    )
                    Divider()
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onTrack, modifier = Modifier.weight(1f)) { Text("Log Workout") }
                OutlinedButton(onClick = onJournal, modifier = Modifier.weight(1f)) { Text("Journal") }
                OutlinedButton(onClick = onProgress, modifier = Modifier.weight(1f)) { Text("Progress") }
            }
        }
    }

    // דיאלוג הוספה
    if (showAdd && uid != null) {
        PlanEditDialog(
            title = "Add plan item",
            initial = PlanItem(day = "Sun"),
            onDismiss = { showAdd = false },
            onConfirm = { day, ex, sets, reps, weight ->
                scope.launch {
                    try {
                        GrowRepo.addPlanItem(
                            uid,
                            PlanItem(day = day, exercise = ex, sets = sets, reps = reps, weight = weight)
                        )
                        showAdd = false
                        refresh()
                    } catch (e: Exception) { err = e.message }
                }
            }
        )
    }

    // דיאלוג עריכה
    if (editItem != null && uid != null) {
        PlanEditDialog(
            title = "Edit plan item",
            initial = editItem!!,
            onDismiss = { editItem = null },
            onConfirm = { day, ex, sets, reps, weight ->
                scope.launch {
                    try {
                        val fields = mapOf(
                            "day" to day,
                            "exercise" to ex,
                            "sets" to sets,
                            "reps" to reps,
                            "weight" to weight
                        )
                        GrowRepo.updatePlanItem(uid, editItem!!.id, fields)
                        editItem = null
                        refresh()
                    } catch (e: Exception) { err = e.message }
                }
            }
        )
    }

    // דיאלוג מחיקה
    if (deleteItem != null && uid != null) {
        AlertDialog(
            onDismissRequest = { deleteItem = null },
            title = { Text("Delete plan item") },
            text = { Text("Are you sure you want to delete \"${deleteItem!!.exercise}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        try {
                            GrowRepo.deletePlanItem(uid, deleteItem!!.id)
                            deleteItem = null
                            refresh()
                        } catch (e: Exception) { err = e.message }
                    }
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { deleteItem = null }) { Text("Cancel") } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlanEditDialog(
    title: String,
    initial: PlanItem,
    onDismiss: () -> Unit,
    onConfirm: (day: String, exercise: String, sets: Int, reps: Int, weight: Float) -> Unit
) {
    var day by remember(initial) { mutableStateOf(initial.day.ifBlank { "Sun" }) }
    var ex by remember(initial) { mutableStateOf(initial.exercise) }
    var sets by remember(initial) { mutableStateOf(initial.sets.toString()) }
    var reps by remember(initial) { mutableStateOf(initial.reps.toString()) }
    var weight by remember(initial) { mutableStateOf(initial.weight.toString()) }

    val days = listOf("Sun","Mon","Tue","Wed","Thu","Fri","Sat")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        // בתוך PlanEditDialog(...)
        text = {
            Column {
                val days = listOf("Sun","Mon","Tue","Wed","Thu","Fri","Sat")

                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        readOnly = true,
                        value = day,
                        onValueChange = {},
                        label = { Text("Day") },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        days.forEach {
                            DropdownMenuItem(
                                text = { Text(it) },
                                onClick = { day = it; expanded = false }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                val exError = ex.isBlank()
                val setsError = sets.isNotBlank() && (sets.toIntOrNull() == null || (sets.toIntOrNull() ?: -1) < 0)
                val repsError = reps.isNotBlank() && (reps.toIntOrNull() == null || (reps.toIntOrNull() ?: -1) < 0)
                val weightError = weight.isNotBlank() && (weight.toFloatOrNull() == null || (weight.toFloatOrNull() ?: -1f) < 0f)
                val valid = !exError && !setsError && !repsError && !weightError

                OutlinedTextField(ex, { ex = it }, label = { Text("Exercise") }, modifier = Modifier.fillMaxWidth(),
                    isError = exError, supportingText = { if (exError) Text("Required") })
                OutlinedTextField(sets, { sets = it.filter(Char::isDigit) }, label = { Text("Sets") }, modifier = Modifier.fillMaxWidth(),
                    isError = setsError, supportingText = { if (setsError) Text("Enter ≥ 0") })
                OutlinedTextField(reps, { reps = it.filter(Char::isDigit) }, label = { Text("Reps") }, modifier = Modifier.fillMaxWidth(),
                    isError = repsError, supportingText = { if (repsError) Text("Enter ≥ 0") })
                OutlinedTextField(
                    weight,
                    { weight = it.filter { c -> c.isDigit() || c=='.' } },
                    label = { Text("Weight (kg)") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = weightError, supportingText = { if (weightError) Text("Enter ≥ 0") }
                )

                // נשמור "valid" ב-CompositionLocal כדי להשתמש בכפתור למטה
                CompositionLocalProvider(LocalContentColor provides LocalContentColor.current) {
                    // כלום – רק כדי להשאיר valid בהישג יד מחוץ ל-scope הזה במקרה הצורך
                }
            }
        },
        confirmButton = {
            val exError = ex.isBlank()
            val setsError = sets.isNotBlank() && (sets.toIntOrNull() == null || (sets.toIntOrNull() ?: -1) < 0)
            val repsError = reps.isNotBlank() && (reps.toIntOrNull() == null || (reps.toIntOrNull() ?: -1) < 0)
            val weightError = weight.isNotBlank() && (weight.toFloatOrNull() == null || (weight.toFloatOrNull() ?: -1f) < 0f)
            val valid = !exError && !setsError && !repsError && !weightError

            TextButton(
                onClick = {
                    onConfirm(
                        day,
                        ex.trim(),
                        sets.toIntOrNull() ?: 0,
                        reps.toIntOrNull() ?: 0,
                        weight.toFloatOrNull() ?: 0f
                    )
                },
                enabled = valid
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
