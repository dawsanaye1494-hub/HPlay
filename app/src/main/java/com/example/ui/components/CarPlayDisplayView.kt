package com.example.ui.components

import android.view.MotionEvent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Podcasts
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CarPlayConfig
import com.example.model.CarPlaySession
import com.example.model.SessionState
import com.example.model.VehicleTelemetry
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EvGreen

@Composable
fun CarPlayDisplayView(
    session: CarPlaySession,
    config: CarPlayConfig,
    telemetry: VehicleTelemetry,
    onWheelZoom: (Boolean) -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeApp by remember { mutableStateOf("Maps") }
    var zoomLevel by remember { mutableStateOf(14) }

    // ColorMatrix for Live Picture Controls: Brightness, Contrast, Saturation
    val colorMatrix = remember(config.brightness, config.contrast, config.saturation) {
        val c = config.contrast
        val b = config.brightness
        val cm = ColorMatrix(
            floatArrayOf(
                c, 0f, 0f, 0f, b,
                0f, c, 0f, 0f, b,
                0f, 0f, c, 0f, b,
                0f, 0f, 0f, 1f, 0f
            )
        )
        val satMatrix = ColorMatrix().apply { setToSaturation(config.saturation) }
        cm.timesAssign(satMatrix)
        cm
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .testTag("carplay_display_view")
    ) {
        val width = maxWidth
        val height = maxHeight

        if (session.state == SessionState.STREAMING) {
            // Main Interactive CarPlay Screen
            Row(modifier = Modifier.fillMaxSize()) {
                // Left Apple CarPlay Dock
                AppleCarPlayDock(
                    activeApp = activeApp,
                    onAppSelect = { activeApp = it },
                    onHome = { activeApp = "Home" }
                )

                // Main Content Canvas / App
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(Color(0xFF0F141C))
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = { offset ->
                                    // Touch mapped to CarPlay
                                }
                            )
                        }
                ) {
                    when (activeApp) {
                        "Maps" -> CarPlayNavigationMap(
                            turn = session.navigation,
                            zoomLevel = zoomLevel,
                            speedKmh = telemetry.speedKmh
                        )
                        "Music" -> CarPlayMusicView(
                            media = session.media,
                            onPlayPause = onPlayPause,
                            onNext = onNext,
                            onPrev = onPrev
                        )
                        "Home" -> CarPlayAppGrid(
                            onSelectApp = { activeApp = it }
                        )
                        else -> CarPlayGenericApp(title = activeApp)
                    }

                    // Resolution & Stream Quality Badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.7f))
                            .border(1.dp, ElectricCyan.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(EvGreen)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${config.resolutionPercent}% • ${session.metrics.fps.toInt()} FPS • ${session.metrics.latencyMs}ms",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color.White
                            )
                        }
                    }

                    // Opt-in Map Zoom Buttons (Controlled by steering wheel or on-screen)
                    if (config.wheelMapZoom && activeApp == "Maps") {
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = 12.dp, bottom = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1E293B).copy(alpha = 0.9f))
                                    .border(1.dp, ElectricCyan.copy(alpha = 0.5f), CircleShape)
                                    .clickable {
                                        zoomLevel = (zoomLevel + 1).coerceAtMost(18)
                                        onWheelZoom(true)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Zoom In", tint = Color.White)
                            }

                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1E293B).copy(alpha = 0.9f))
                                    .border(1.dp, ElectricCyan.copy(alpha = 0.5f), CircleShape)
                                    .clickable {
                                        zoomLevel = (zoomLevel - 1).coerceAtLeast(8)
                                        onWheelZoom(false)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Zoom Out", tint = Color.White)
                            }
                        }
                    }

                    // 5-Second Dashboard Song-On-Change Window (with Timer Invalidation)
                    androidx.compose.animation.AnimatedVisibility(
                        visible = session.media.isPopoverActive,
                        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 12.dp)
                    ) {
                        SongChangePopoverCard(
                            media = session.media,
                            onPlayPause = onPlayPause,
                            onNext = onNext
                        )
                    }

                    // Parked Video Safety Lock Banner
                    if (telemetry.parkedVideoLockActive) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.85f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Safety Lock",
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    text = "SAFETY DRIVING LOCK ACTIVE",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "Video playback is locked while vehicle is in motion.\nAudio streaming and turn navigation remain fully active.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Standby / Connecting State Screen
            CarPlayConnectingStateView(session = session, config = config)
        }
    }
}

@Composable
fun AppleCarPlayDock(
    activeApp: String,
    onAppSelect: (String) -> Unit,
    onHome: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(68.dp)
            .fillMaxHeight()
            .background(Color(0xFF070A0F))
            .border(width = 1.dp, color = Color(0xFF1E293B))
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top status indicators
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "22:05",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "5G",
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                color = ElectricCyan
            )
        }

        // Recent / Fast Switch Apps
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DockIcon(
                icon = Icons.Default.Navigation,
                label = "Maps",
                isSelected = activeApp == "Maps",
                color = Color(0xFF38BDF8),
                onClick = { onAppSelect("Maps") }
            )
            DockIcon(
                icon = Icons.Default.MusicNote,
                label = "Music",
                isSelected = activeApp == "Music",
                color = Color(0xFFFA2D48),
                onClick = { onAppSelect("Music") }
            )
            DockIcon(
                icon = Icons.Default.Call,
                label = "Phone",
                isSelected = activeApp == "Phone",
                color = Color(0xFF34C759),
                onClick = { onAppSelect("Phone") }
            )
            DockIcon(
                icon = Icons.Default.Message,
                label = "Messages",
                isSelected = activeApp == "Messages",
                color = Color(0xFF30D158),
                onClick = { onAppSelect("Messages") }
            )
        }

        // Bottom Apple Home Pill
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0xFF1E293B))
                .clickable { onHome() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Widgets,
                contentDescription = "Home",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun DockIcon(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) color.copy(alpha = 0.25f) else Color(0xFF121824))
            .border(
                width = if (isSelected) 1.5.dp else 0.dp,
                color = if (isSelected) color else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) color else Color(0xFFCBD5E1),
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
fun CarPlayNavigationMap(
    turn: com.example.model.NavigationTurn,
    zoomLevel: Int,
    speedKmh: Float
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // High-contrast Vector Map Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Background terrain / dark map road network
            drawRect(Color(0xFF0D131D))

            // Main Expressway corridor
            val roadPath = Path().apply {
                moveTo(w * 0.45f, h)
                cubicTo(
                    w * 0.45f, h * 0.6f,
                    w * 0.55f, h * 0.4f,
                    w * 0.85f, 0f
                )
            }
            // Secondary roads
            drawLine(
                color = Color(0xFF223046),
                start = Offset(0f, h * 0.45f),
                end = Offset(w, h * 0.35f),
                strokeWidth = 14f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color(0xFF1E293B),
                start = Offset(w * 0.2f, 0f),
                end = Offset(w * 0.35f, h),
                strokeWidth = 12f
            )

            // Highway border
            drawPath(
                path = roadPath,
                color = Color(0xFF1E293B),
                style = Stroke(width = 32f, cap = StrokeCap.Round)
            )
            // Highway active navigation lane (Electric Blue / Green)
            drawPath(
                path = roadPath,
                color = Color(0xFF007AFF),
                style = Stroke(width = 22f, cap = StrokeCap.Round)
            )

            // Vehicle location marker
            val carCenter = Offset(w * 0.45f, h * 0.72f)
            drawCircle(
                color = Color(0xFF00E5FF).copy(alpha = 0.25f),
                radius = 24f,
                center = carCenter
            )
            drawCircle(
                color = Color.White,
                radius = 8f,
                center = carCenter
            )
            drawCircle(
                color = Color(0xFF007AFF),
                radius = 6f,
                center = carCenter
            )
        }

        // Top-Left Turn Guidance Card (Apple CarPlay standard)
        Card(
            modifier = Modifier
                .padding(12.dp)
                .widthIn(max = 310.dp)
                .fillMaxWidth(0.85f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF111827).copy(alpha = 0.94f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF007AFF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Navigation,
                            contentDescription = "Turn",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = turn.distanceText,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = turn.streetName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFE2E8F0),
                            maxLines = 1
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = turn.nextManeuver,
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ETA ${turn.etaText} • ${turn.remainingDistanceKm} km",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan
                    )
                    // Speed limit badge
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(2.dp, Color(0xFFEF4444), CircleShape)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${turn.speedLimitKmh}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CarPlayMusicView(
    media: com.example.model.MediaMetadata,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Album Art
        Box(
            modifier = Modifier
                .size(190.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFFFA2D48), Color(0xFF7A092A), Color(0xFF1E1020))
                    )
                )
                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = "Album Art",
                tint = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.size(72.dp)
            )
        }

        Spacer(modifier = Modifier.width(32.dp))

        // Track Info & Controls
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = media.title,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "${media.artist} — ${media.album}",
                fontSize = 15.sp,
                color = Color(0xFF94A3B8)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Progress Bar
            val progress = media.positionSeconds.toFloat() / media.durationSeconds.toFloat()
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = ElectricCyan,
                trackColor = Color(0xFF1E293B)
            )

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${media.positionSeconds / 60}:${(media.positionSeconds % 60).toString().padStart(2, '0')}",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
                Text(
                    text = "-${(media.durationSeconds - media.positionSeconds) / 60}:${((media.durationSeconds - media.positionSeconds) % 60).toString().padStart(2, '0')}",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Playback buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPrev, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = "Prev", tint = Color.White, modifier = Modifier.size(32.dp))
                }
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onPlayPause() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (media.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color.Black,
                        modifier = Modifier.size(32.dp)
                    )
                }
                IconButton(onClick = onNext, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = Color.White, modifier = Modifier.size(32.dp))
                }
            }
        }
    }
}

@Composable
fun CarPlayAppGrid(onSelectApp: (String) -> Unit) {
    val apps = listOf(
        Pair("Maps", Icons.Default.Navigation),
        Pair("Music", Icons.Default.MusicNote),
        Pair("Phone", Icons.Default.Call),
        Pair("Messages", Icons.Default.Message),
        Pair("Podcasts", Icons.Default.Podcasts),
        Pair("Calendar", Icons.Default.DateRange),
        Pair("Settings", Icons.Default.Settings)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            apps.take(4).forEach { (name, icon) ->
                AppGridItem(name = name, icon = icon, onClick = { onSelectApp(name) })
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            apps.drop(4).forEach { (name, icon) ->
                AppGridItem(name = name, icon = icon, onClick = { onSelectApp(name) })
            }
        }
    }
}

@Composable
fun AppGridItem(name: String, icon: ImageVector, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                    )
                )
                .border(1.dp, ElectricCyan.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = name,
                tint = ElectricCyan,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = name, fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun CarPlayGenericApp(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = title, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Apple CarPlay Live Application Feed", fontSize = 14.sp, color = Color(0xFF94A3B8))
        }
    }
}

@Composable
fun SongChangePopoverCard(
    media: com.example.model.MediaMetadata,
    onPlayPause: () -> Unit,
    onNext: () -> Unit
) {
    Card(
        modifier = Modifier
            .widthIn(max = 360.dp)
            .fillMaxWidth(0.9f)
            .border(1.dp, ElectricCyan.copy(alpha = 0.6f), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0F1D).copy(alpha = 0.95f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Album Art
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFFFA2D48), Color(0xFF7A092A))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.MusicNote, contentDescription = "Art", tint = Color.White, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = media.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1
                    )
                    Text(
                        text = "${media.artist} — ${media.album}",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8),
                        maxLines = 1
                    )
                }

                IconButton(onClick = onPlayPause, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = if (media.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = ElectricCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onNext, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Song Changed • H Play Hi-Fi Audio",
                    fontSize = 9.sp,
                    color = Color(0xFF64748B)
                )
                Text(
                    text = "${media.popoverRemainingSec}s",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    color = ElectricCyan
                )
            }
        }
    }
}

@Composable
fun CarPlayConnectingStateView(
    session: CarPlaySession,
    config: CarPlayConfig
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070B14)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(ElectricCyan.copy(alpha = 0.15f))
                    .border(2.dp, ElectricCyan, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Navigation,
                    contentDescription = "Connecting",
                    tint = ElectricCyan,
                    modifier = Modifier.size(40.dp)
                )
            }

            Text(
                text = session.state.label,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = "Target Subnet: ${config.targetSubnet}* • Mode: ${config.connectionMode.title}\nWaiting for iPhone AirPlay/CarPlay handshake…",
                fontSize = 13.sp,
                color = Color(0xFF94A3B8),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            if (session.lastError != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEF4444).copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = session.lastError,
                        color = Color(0xFFF87171),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}
