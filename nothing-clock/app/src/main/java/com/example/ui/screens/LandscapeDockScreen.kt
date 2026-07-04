package com.example.ui.screens

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.DotMatrixString
import com.example.service.MediaPlaybackManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

data class AudioTrack(
    val title: String,
    val artist: String,
    val durationSeconds: Int
)

/**
 * Checks if the notification access permission is granted for this application.
 */
fun isNotificationServiceEnabled(context: Context): Boolean {
    val pkgName = context.packageName
    val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
    if (!flat.isNullOrEmpty()) {
        val names = flat.split(":")
        for (name in names) {
            val cn = ComponentName.unflattenFromString(name)
            if (cn != null && cn.packageName == pkgName) {
                return true
            }
        }
    }
    return false
}

/**
 * Highly aesthetic, full screen landscape dock clock designed for AMOLED displays.
 * Incorporates a subtle shift animation that slowly orbits the time based on minutes
 * to prevent pixel burn-in.
 * Features an integrated physical-mode Music Control Module styled in Nothing's monochromatic
 * design, with live support for streaming apps (Spotify, YT Music, etc.) via Notification Access.
 */
@Composable
fun LandscapeDockScreen(
    currentTimestamp: Long,
    is24Hour: Boolean = true,
    onDismiss: () -> Unit
) {
    val date = Date(currentTimestamp)
    val timeFormat = if (is24Hour) {
        SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    } else {
        SimpleDateFormat("hh:mm:ss", Locale.getDefault())
    }
    val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())
    
    val timeStr = timeFormat.format(date)
    val dateStr = dateFormat.format(date).uppercase()
    val period = if (is24Hour) "" else SimpleDateFormat("a", Locale.getDefault()).format(date).uppercase()

    // Calculate shift offset to prevent Amoled burn-in (max 15 pixels)
    val calendar = Calendar.getInstance().apply { timeInMillis = currentTimestamp }
    val minute = calendar.get(Calendar.MINUTE)
    val second = calendar.get(Calendar.SECOND)
    val angle = (minute + second / 60.0) * (2 * Math.PI / 60.0)
    
    val offsetX = (15 * cos(angle)).toInt()
    val offsetY = (15 * sin(angle)).toInt()

    val context = LocalContext.current
    var isNotificationAccessGranted by remember { mutableStateOf(false) }

    // Periodically poll notification access to react instantly when the user grants it
    LaunchedEffect(Unit) {
        while (true) {
            isNotificationAccessGranted = isNotificationServiceEnabled(context)
            kotlinx.coroutines.delay(1000L)
        }
    }

    // Collect real-time active system media session flows
    val realIsPlaying by MediaPlaybackManager.isPlaying.collectAsState()
    val realTitle by MediaPlaybackManager.trackTitle.collectAsState()
    val realArtist by MediaPlaybackManager.trackArtist.collectAsState()
    val realDuration by MediaPlaybackManager.trackDuration.collectAsState()
    val realPosition by MediaPlaybackManager.trackPosition.collectAsState()
    val hasActiveSession by MediaPlaybackManager.hasActiveSession.collectAsState()

    // Live ticking simulation for the real media player position
    var simulatedProgressSeconds by remember { mutableIntStateOf(0) }

    LaunchedEffect(realIsPlaying, realPosition, hasActiveSession) {
        if (hasActiveSession) {
            simulatedProgressSeconds = realPosition
            if (realIsPlaying) {
                while (true) {
                    kotlinx.coroutines.delay(1000L)
                    simulatedProgressSeconds++
                    if (realDuration > 0 && simulatedProgressSeconds >= realDuration) {
                        simulatedProgressSeconds = realDuration
                    }
                }
            }
        }
    }

    // Local Playlist Fallback (Demo Mode) when system notification is connected but no active session or access is missing
    var isLocalPlaying by remember { mutableStateOf(false) }
    var localTrackIdx by remember { mutableIntStateOf(0) }
    var localProgressSeconds by remember { mutableIntStateOf(0) }

    val playlist = remember {
        listOf(
            AudioTrack("GLYPH RAPID", "NOTHING BEAT", 145),
            AudioTrack("VOX UNISON", "SYNTH(0)", 210),
            AudioTrack("TEENAGE AMBIENT", "TE-1 SYSTEM", 180),
            AudioTrack("SILENT STATE", "WHITE LENS", 320)
        )
    }

    val currentLocalTrack = playlist[localTrackIdx]

    // Local ticking track progress
    LaunchedEffect(isLocalPlaying, localTrackIdx) {
        if (isLocalPlaying) {
            while (true) {
                kotlinx.coroutines.delay(1000L)
                localProgressSeconds = (localProgressSeconds + 1) % currentLocalTrack.durationSeconds
            }
        }
    }

    // Bind UI states dynamically based on status
    val isUsingSystemMedia = isNotificationAccessGranted && hasActiveSession
    val activePlaying = if (isUsingSystemMedia) realIsPlaying else isLocalPlaying
    val displayTitle = if (isUsingSystemMedia) realTitle else currentLocalTrack.title
    val displayArtist = if (isUsingSystemMedia) realArtist else currentLocalTrack.artist
    val displayDuration = if (isUsingSystemMedia) realDuration else currentLocalTrack.durationSeconds
    val displayProgress = if (isUsingSystemMedia) simulatedProgressSeconds else localProgressSeconds

    // Formatting helper
    fun formatTrackTime(secs: Int): String {
        val m = secs / 60
        val s = secs % 60
        return String.format(Locale.getDefault(), "%02d:%02d", m, s)
    }

    // Interactive spectra looping animation when playing
    val infiniteTransition = rememberInfiniteTransition(label = "visualizer_transit")
    val animatedAmplitudes = (0..7).map { i ->
        infiniteTransition.animateFloat(
            initialValue = 0.2f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 280 + (i * 90),
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "amp_$i"
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 24.dp)
                .offset { IntOffset(offsetX, offsetY) },
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Column: Dot Matrix visual clock
            Column(
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                DotMatrixString(
                    text = timeStr.take(5), // HH:mm
                    activeColor = Color.White,
                    inactiveColor = Color(0x11FFFFFF),
                    charSpacing = 12.dp,
                    dotSize = 8.dp,
                    dotSpacing = 2.dp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = ".${timeStr.takeLast(2)}",
                        color = Color(0xFFFF2B2B), // Distinct physical style red second dot
                        fontSize = 28.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    if (period.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = period,
                            color = Color(0xFFFF2B2B),
                            fontSize = 18.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = dateStr,
                        color = Color(0xFF888888),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 2.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "TAP BACKYARD TO EXIT",
                    color = Color(0x28FFFFFF),
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 3.sp
                )
            }

            // Vertical partition line
            Box(
                modifier = Modifier
                    .fillMaxHeight(0.65f)
                    .width(1.dp)
                    .background(Color(0x1AFFFFFF))
            )

            // Right Column: Physical-style Audio Controller widget
            Column(
                modifier = Modifier
                    .weight(1.0f)
                    .fillMaxHeight()
                    .padding(start = 24.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {} // Capture clicks inside this column to prevent backdrop dismiss
                    ),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                // If notification access is missing, show high-fidelity call-to-action
                if (!isNotificationAccessGranted) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(8.dp))
                            .clickable {
                                try {
                                    val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Log.e("LandscapeDock", "Could not open settings", e)
                                }
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFF2B2B))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "LINK REAL PHONE PLAYER",
                                    color = Color.White,
                                    fontSize = 8.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "GRANT ↗",
                                color = Color(0xFFFF2B2B),
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Top tag: audio framework metadata
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(if (activePlaying) Color(0xFFFF2B2B) else Color(0x33FFFFFF))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isUsingSystemMedia) "PHONE LINK [SPOTIFY/MEDIA]" else "LOCAL DECK [SIMULATED]",
                        color = Color(0x55FFFFFF),
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    // Small bouncing sound graphs
                    Row(
                        modifier = Modifier.height(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        for (i in 0..7) {
                            val targetAmp = if (activePlaying) animatedAmplitudes[i].value else 0.15f
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .fillMaxHeight(targetAmp)
                                    .background(if (activePlaying) Color(0xFFFF2B2B) else Color(0x40FFFFFF))
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Track title & metadata
                Text(
                    text = displayTitle,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = displayArtist,
                    color = Color(0xFF888888),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Progress timeline meter
                val progressRatio = if (displayDuration > 0) displayProgress.toFloat() / displayDuration.toFloat() else 0f
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(Color(0x18FFFFFF))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progressRatio)
                            .fillMaxHeight()
                            .background(Color.White)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Time indicators of timeline
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatTrackTime(displayProgress),
                        color = Color(0x66FFFFFF),
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = formatTrackTime(displayDuration),
                        color = Color(0x66FFFFFF),
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Hardware-style player keys
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous Key
                    IconButton(
                        onClick = {
                            if (isUsingSystemMedia) {
                                MediaPlaybackManager.skipToPrevious()
                            } else {
                                val newIdx = (localTrackIdx - 1 + playlist.size) % playlist.size
                                localTrackIdx = newIdx
                                localProgressSeconds = 0
                            }
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .border(1.dp, Color(0x1EFFFFFF), CircleShape)
                    ) {
                        Text(
                            text = "◀",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Play/Pause circular highlight Key
                    IconButton(
                        onClick = {
                            if (isUsingSystemMedia) {
                                if (realIsPlaying) MediaPlaybackManager.pause() else MediaPlaybackManager.play()
                            } else {
                                isLocalPlaying = !isLocalPlaying
                            }
                        },
                        modifier = Modifier
                            .size(46.dp)
                            .background(if (activePlaying) Color(0xFFFF2B2B) else Color.White, CircleShape)
                    ) {
                        Text(
                            text = if (activePlaying) "▮▮" else "▶",
                            color = if (activePlaying) Color.White else Color.Black,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Next Key
                    IconButton(
                        onClick = {
                            if (isUsingSystemMedia) {
                                MediaPlaybackManager.skipToNext()
                            } else {
                                val newIdx = (localTrackIdx + 1) % playlist.size
                                localTrackIdx = newIdx
                                localProgressSeconds = 0
                            }
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .border(1.dp, Color(0x1EFFFFFF), CircleShape)
                    ) {
                        Text(
                            text = "▶",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
