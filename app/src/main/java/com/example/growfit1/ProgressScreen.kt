package com.example.growfit1

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max
import kotlin.math.roundToInt

enum class Metric { MaxWeight, TotalDuration }
private data class Point(val x: Long, val y: Float)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen() {
    val uid = remember { AuthRepo.uid() }
    var journal by remember { mutableStateOf(emptyList<WorkoutEntry>()) }
    var loading by remember { mutableStateOf(false) }
    var err by remember { mutableStateOf<String?>(null) }

    // בוחרים
    var selectedType by remember { mutableStateOf("All") }
    var metric by remember { mutableStateOf(Metric.MaxWeight) }
    var daysWindow by remember { mutableStateOf(30) }

    val scope = rememberCoroutineScope()

    // טעינה/רענון מהענן
    suspend fun refresh() {
        if (uid == null) { err = "Not logged in"; return }
        loading = true; err = null
        try { journal = GrowRepo.getJournal(uid) }
        catch (e: Exception) { err = e.message ?: "Failed to load data" }
        finally { loading = false }
    }
    LaunchedEffect(uid) { refresh() }

    // רשימת סוגי תרגילים (מהנתונים)
    val types = remember(journal) {
        val set = linkedSetOf<String>()
        journal.forEach { if (it.type.isNotBlank()) set.add(it.type.trim()) }
        (listOf("All") + set.toList())
    }
    if (selectedType !in types) selectedType = "All"

    // נקודות לגרף (ע"פ בחירה)
    val points = remember(journal, selectedType, metric, daysWindow) {
        buildChartPoints(journal, selectedType, metric, daysWindow)
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Progress", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
            IconButton(onClick = { scope.launch { refresh() } }) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
            }
        }
        Spacer(Modifier.height(8.dp))

        if (loading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
        }
        err?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        // שורה 1: בחירת תרגיל
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            SmallDropDown(
                label = "Exercise",
                options = types,
                selected = selectedType,
                onSelected = { selectedType = it }
            )
        }

        Spacer(Modifier.height(6.dp))

        // שורה 2: מדד (צ'יפים ברורים)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = metric == Metric.MaxWeight,
                onClick = { metric = Metric.MaxWeight },
                label = { Text("Max weight") }
            )
            FilterChip(
                selected = metric == Metric.TotalDuration,
                onClick = { metric = Metric.TotalDuration },
                label = { Text("Total duration") }
            )
        }

        Spacer(Modifier.height(6.dp))

        // שורה 3: טווח ימים (צ'יפים ברורים)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(7, 30, 90).forEach { d ->
                FilterChip(
                    selected = daysWindow == d,
                    onClick = { daysWindow = d },
                    label = { Text("${d}d") }
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // אינדיקציית מצב
        Text(
            "Showing ${points.size} points | Type: $selectedType | Metric: ${if (metric==Metric.MaxWeight) "kg" else "min"} | Window: ${daysWindow}d",
            style = MaterialTheme.typography.labelMedium
        )

        Spacer(Modifier.height(8.dp))

        if (points.isEmpty()) {
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text(
                    "No data for selection.\nTips:\n• ודא ששדה Type זהה בכל הרישומים (אותיות גדולות/קטנות).\n• אם בחרת Max weight אבל הכנסת Weight=0 – נסה 'Total duration' או עדכן.",
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Card(Modifier.fillMaxWidth().weight(1f)) {
                Box(Modifier.fillMaxSize().padding(12.dp)) {
                    LineChart(
                        points = points,
                        yLabel = if (metric == Metric.MaxWeight) "kg" else "min",
                        gridSteps = 5,
                        bottomPadding = 28.dp,
                        leftPadding = 40.dp
                    )
                }
            }
        }
    }
}

private fun buildChartPoints(
    journal: List<WorkoutEntry>,
    type: String,
    metric: Metric,
    daysWindow: Int
): List<Point> {
    if (journal.isEmpty()) return emptyList()
    val now = System.currentTimeMillis()
    val since = now - daysWindow * 24L * 60 * 60 * 1000

    val filtered = journal.filter { it.ts in since..now && (type == "All" || it.type.trim() == type) }
    if (filtered.isEmpty()) return emptyList()

    fun dayStart(ts: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = ts
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    val byDay = filtered.groupBy { dayStart(it.ts) }

    return byDay.map { (day, items) ->
        val value = when (metric) {
            Metric.MaxWeight     -> items.maxOf { it.weight }
            Metric.TotalDuration -> items.sumOf { it.durationMin }.toFloat()
        }
        Point(x = day, y = value)
    }.sortedBy { it.x }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SmallDropDown(
    label: String,
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            readOnly = true,
            value = selected,
            onValueChange = {},
            label = { Text(label) },
            modifier = Modifier
                .menuAnchor()
                .widthIn(min = 160.dp)
                .zIndex(1f),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach {
                DropdownMenuItem(
                    text = { Text(it) },
                    onClick = { onSelected(it); expanded = false }
                )
            }
        }
    }
}

@Composable
private fun LineChart(
    points: List<Point>,
    yLabel: String,
    gridSteps: Int = 5,
    bottomPadding: Dp = 28.dp,
    leftPadding: Dp = 40.dp
) {
    val minX = points.first().x
    val maxX = points.last().x
    val minY = 0f
    val maxY = max(points.maxOf { it.y }, 1f)
    val dateFmt = remember { SimpleDateFormat("dd/MM", Locale.getDefault()) }

    Canvas(Modifier.fillMaxSize().background(Color.Transparent)) {
        val w = size.width
        val h = size.height
        val left = leftPadding.toPx()
        val bottom = bottomPadding.toPx()
        val chartW = w - left - 8f
        val chartH = h - bottom - 8f
        if (chartW <= 0f || chartH <= 0f) return@Canvas

        fun mapX(x: Long): Float {
            if (maxX == minX) return left
            val t = (x - minX).toFloat() / (maxX - minX)
            return left + t * chartW
        }
        fun mapY(y: Float): Float {
            val t = (y - minY) / (maxY - minY)
            return (8f + chartH) - t * chartH
        }

        // צירים
        drawLine(Color.Gray, start = Offset(left, 8f), end = Offset(left, 8f + chartH))
        drawLine(Color.Gray, start = Offset(left, 8f + chartH), end = Offset(left + chartW, 8f + chartH))

        // גריד Y + תוויות
        val step = maxY / gridSteps
        for (i in 0..gridSteps) {
            val yVal = i * step
            val yPx = mapY(yVal)
            drawLine(Color(0xFFE0E0E0), start = Offset(left, yPx), end = Offset(left + chartW, yPx))
            drawIntoCanvas { canvas ->
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.DKGRAY
                    textSize = 28f
                }
                val txt = (if (yVal % 1f == 0f) yVal.roundToInt().toString() else String.format("%.1f", yVal)) + " $yLabel"
                canvas.nativeCanvas.drawText(txt, 4f, yPx - 4f, paint)
            }
        }

        // תוויות X (תחילה/אמצע/סוף)
        val xPositions = listOf(minX, (minX + maxX) / 2, maxX).distinct()
        xPositions.forEach { x ->
            val xPx = mapX(x)
            val txt = dateFmt.format(Date(x))
            drawIntoCanvas { canvas ->
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.DKGRAY
                    textSize = 28f
                }
                canvas.nativeCanvas.drawText(txt, xPx - 40f, 8f + chartH + 24f, paint)
            }
        }

        // קו הנתונים + נקודות
        val path = Path()
        points.forEachIndexed { idx, p ->
            val px = mapX(p.x)
            val py = mapY(p.y)
            if (idx == 0) path.moveTo(px, py) else path.lineTo(px, py)
        }
        drawPath(path, color = Color(0xFF3F51B5), style = Stroke(width = 4f))
        points.forEach { p ->
            drawCircle(Color(0xFF3F51B5), radius = 6f, center = Offset(mapX(p.x), mapY(p.y)))
        }
    }
}
