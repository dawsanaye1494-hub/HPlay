package com.example.ui.dialogs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.logger.FileLogManager
import com.example.logger.LogEntry
import com.example.logger.LogLevel
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EvGreen
import kotlinx.coroutines.launch
import java.io.File

enum class LogFilterCategory(val label: String) {
    ALL("All Logs"),
    ERRORS("Errors & Crashes"),
    WARNINGS("Warnings"),
    CAN("CAN Bus"),
    USB_HW("USB & Dongle"),
    AIRPLAY("CarPlay Stream")
}

@Composable
fun LogViewerDialog(
    telemetry: com.example.model.VehicleTelemetry? = null,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val logs by FileLogManager.logsFlow.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(LogFilterCategory.ALL) }
    var autoScrollToBottom by remember { mutableStateOf(true) }
    var savedFile by remember { mutableStateOf<File?>(null) }

    val listState = rememberLazyListState()

    // Filter logs
    val filteredLogs = remember(logs, searchQuery, selectedCategory) {
        logs.filter { entry ->
            val matchesCategory = when (selectedCategory) {
                LogFilterCategory.ALL -> true
                LogFilterCategory.ERRORS -> entry.level == LogLevel.ERROR || entry.level == LogLevel.CRASH
                LogFilterCategory.WARNINGS -> entry.level == LogLevel.WARN
                LogFilterCategory.CAN -> entry.tag.contains("CAN", ignoreCase = true)
                LogFilterCategory.USB_HW -> entry.tag.contains("USB", ignoreCase = true) || entry.tag.contains("CH341", ignoreCase = true)
                LogFilterCategory.AIRPLAY -> entry.tag.contains("CARPLAY", ignoreCase = true) || entry.tag.contains("AIRPLAY", ignoreCase = true) || entry.tag.contains("MEDIACODEC", ignoreCase = true)
            }

            val matchesSearch = if (searchQuery.isBlank()) true else {
                entry.message.contains(searchQuery, ignoreCase = true) ||
                    entry.tag.contains(searchQuery, ignoreCase = true) ||
                    (entry.throwableStackTrace?.contains(searchQuery, ignoreCase = true) == true)
            }

            matchesCategory && matchesSearch
        }
    }

    // Auto-scroll when new logs arrive if enabled
    LaunchedEffect(filteredLogs.size, autoScrollToBottom) {
        if (autoScrollToBottom && filteredLogs.isNotEmpty()) {
            listState.animateScrollToItem(filteredLogs.size - 1)
        }
    }

    val errorCount = remember(logs) { logs.count { it.level == LogLevel.ERROR || it.level == LogLevel.CRASH } }
    val warnCount = remember(logs) { logs.count { it.level == LogLevel.WARN } }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF030712))
                .padding(12.dp)
                .testTag("log_viewer_dialog")
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E293B))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "SYSTEM & ERROR LOG FILE",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                // Error count badge
                                if (errorCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFFEF4444).copy(alpha = 0.25f))
                                            .border(1.dp, Color(0xFFEF4444), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "$errorCount ERRORS",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFEF4444)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "Path: ${FileLogManager.getLogFilePath()} (${FileLogManager.getLogFileSizeFormatted()})",
                                fontSize = 9.sp,
                                color = Color(0xFF94A3B8),
                                maxLines = 1
                            )
                        }
                    }

                    // Action Buttons (Simulate Error, Copy, Export)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Simulate Test Error Button
                        Button(
                            onClick = {
                                FileLogManager.simulateTestError()
                                Toast.makeText(context, "Simulated CAN bus error captured in log file!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444).copy(alpha = 0.2f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.BugReport,
                                contentDescription = "Test Error",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Test Error", fontSize = 11.sp, color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                        }

                        // Copy All Button
                        Button(
                            onClick = {
                                val text = logs.joinToString("\n") { it.toLogLine() }
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("HPlay Logs", text)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Copied ${logs.size} log lines to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Copy All", fontSize = 11.sp, color = Color.White)
                        }

                        // Export & Save .TXT File to Device Storage
                        Button(
                            onClick = {
                                try {
                                    val file = FileLogManager.exportTxtLogFile(context, telemetry)
                                    savedFile = file
                                    Toast.makeText(context, "Log file saved to device: ${file.name}", Toast.LENGTH_LONG).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(34.dp).testTag("save_txt_to_device_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = "Save .TXT",
                                tint = Color.Black,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Save .TXT", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Black)
                        }

                        // Clear Logs Button
                        IconButton(
                            onClick = {
                                FileLogManager.clearLogs()
                                savedFile = null
                                Toast.makeText(context, "Log file cleared", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1E293B))
                        ) {
                            Icon(
                                imageVector = Icons.Default.ClearAll,
                                contentDescription = "Clear",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // File Saved In Device Storage Confirmation Banner
                if (savedFile != null) {
                    val file = savedFile!!
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF062817)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EvGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("saved_file_banner")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Saved",
                                    tint = EvGreen,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "TXT LOG FILE SAVED IN DEVICE STORAGE",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                    Text(
                                        text = file.absolutePath,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = EvGreen,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "Size: ${(file.length() / 1024).coerceAtLeast(1)} KB • Saved in Downloads folder ready for transfer or inspection",
                                        fontSize = 9.sp,
                                        color = Color(0xFFCBD5E1)
                                    )
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = {
                                        val intent = FileLogManager.createShareTxtIntent(context, file)
                                        context.startActivity(intent)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = EvGreen),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Share",
                                        tint = Color.Black,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Share .TXT", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                                IconButton(
                                    onClick = { savedFile = null },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Dismiss",
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Search Bar & Filter Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Search Input Field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search logs by keyword (e.g. error, 0x3B0, MediaCodec)...", fontSize = 11.sp, color = Color(0xFF64748B)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear search",
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = Color(0xFF1E293B),
                            focusedContainerColor = Color(0xFF0A0F1D),
                            unfocusedContainerColor = Color(0xFF0A0F1D),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                    )

                    // Auto-scroll toggle
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (autoScrollToBottom) ElectricBlue.copy(alpha = 0.25f) else Color(0xFF1E293B))
                            .border(1.dp, if (autoScrollToBottom) ElectricBlue else Color.Transparent, RoundedCornerShape(8.dp))
                            .clickable { autoScrollToBottom = !autoScrollToBottom }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (autoScrollToBottom) "Auto-Scroll ON" else "Auto-Scroll OFF",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (autoScrollToBottom) ElectricCyan else Color(0xFF94A3B8)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Filter Category Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    LogFilterCategory.values().forEach { cat ->
                        val isSelected = selectedCategory == cat
                        val count = when (cat) {
                            LogFilterCategory.ALL -> logs.size
                            LogFilterCategory.ERRORS -> errorCount
                            LogFilterCategory.WARNINGS -> warnCount
                            else -> null
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) ElectricCyan else Color(0xFF0E1626))
                                .border(1.dp, if (isSelected) ElectricCyan else Color(0xFF1E293B), RoundedCornerShape(6.dp))
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (count != null) "${cat.label} ($count)" else cat.label,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                color = if (isSelected) Color.Black else Color(0xFFCBD5E1)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Log Records List
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF060B14)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
                ) {
                    if (filteredLogs.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = "No logs",
                                    tint = Color(0xFF334155),
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (logs.isEmpty()) "Log file initialized. No events recorded yet." else "No log entries match the current filter.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(filteredLogs, key = { it.id }) { entry ->
                                LogItemRow(entry = entry)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LogItemRow(entry: LogEntry) {
    var isExpanded by remember { mutableStateOf(false) }

    val levelColor = when (entry.level) {
        LogLevel.CRASH, LogLevel.ERROR -> Color(0xFFEF4444)
        LogLevel.WARN -> Color(0xFFF59E0B)
        LogLevel.INFO -> ElectricCyan
        LogLevel.DEBUG -> Color(0xFF94A3B8)
    }

    val itemBg = when (entry.level) {
        LogLevel.CRASH -> Color(0xFF3B0707)
        LogLevel.ERROR -> Color(0xFF290808)
        LogLevel.WARN -> Color(0xFF1C1304)
        else -> Color(0xFF0A0F1D)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(itemBg)
            .border(1.dp, levelColor.copy(alpha = 0.25f), RoundedCornerShape(6.dp))
            .clickable {
                if (entry.throwableStackTrace != null) {
                    isExpanded = !isExpanded
                }
            }
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Timestamp
                Text(
                    text = entry.timestamp,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF64748B)
                )
                Spacer(modifier = Modifier.width(6.dp))

                // Level Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(levelColor.copy(alpha = 0.2f))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = entry.level.label,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = levelColor
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))

                // Tag
                Text(
                    text = "[${entry.tag}]",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFCBD5E1)
                )
            }

            if (entry.throwableStackTrace != null) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand Stack Trace",
                    tint = levelColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(3.dp))

        // Message
        Text(
            text = entry.message,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = if (entry.level == LogLevel.ERROR || entry.level == LogLevel.CRASH) Color(0xFFFCA5A5) else Color.White
        )

        // Expandable Stack Trace
        AnimatedVisibility(visible = isExpanded && entry.throwableStackTrace != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF030712))
                    .padding(6.dp)
            ) {
                Text(
                    text = "STACK TRACE:",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFEF4444)
                )
                Text(
                    text = entry.throwableStackTrace ?: "",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFFF87171)
                )
            }
        }
    }
}
