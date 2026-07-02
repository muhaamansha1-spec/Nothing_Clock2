package com.example

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.AlarmsScreen
import com.example.ui.screens.LandscapeDockScreen
import com.example.ui.screens.StopwatchScreen
import com.example.ui.screens.TimerScreen
import com.example.ui.screens.WorldClockScreen
import com.example.ui.theme.NothingClockTheme
import com.example.ui.viewmodel.ClockSection
import com.example.ui.viewmodel.ClockViewModel
import com.example.ui.viewmodel.CameraCutout
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.window.Dialog

class MainActivity : ComponentActivity() {
    private val viewModel: ClockViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Immersive full-screen: hide both status bar and navigation bar with transient swipe logic
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        insetsController.hide(WindowInsetsCompat.Type.systemBars())

        enableEdgeToEdge()
        setContent {
            NothingClockTheme {
                MainClockApp(viewModel)
            }
        }
    }
}

@Composable
fun MainClockApp(viewModel: ClockViewModel) {
    val currentSection by viewModel.currentSection.collectAsState()
    val alarms by viewModel.alarms.collectAsState()
    val worldClocks by viewModel.worldClocks.collectAsState()
    val ticker by viewModel.ticker.collectAsState()

    // Camera Cutout state
    val cutout by viewModel.selectedCutout.collectAsState()
    var showSettingsDialog by remember { mutableStateOf(false) }
    
    // Stopwatch States
    val stopwatchTime by viewModel.stopwatchTime.collectAsState()
    val isStopwatchRunning by viewModel.isStopwatchRunning.collectAsState()
    val laps by viewModel.laps.collectAsState()

    // Timer States
    val timerDurationInput by viewModel.timerDurationInput.collectAsState()
    val timerSecondsRemaining by viewModel.timerSecondsRemaining.collectAsState()
    val timerState by viewModel.timerState.collectAsState()

    // Configuration for landscape dock checking
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    
    // If we're landscape and in World Clock section, render the Dock overlay
    if (isLandscape && currentSection == ClockSection.WORLD_CLOCK) {
        LandscapeDockScreen(
            currentTimestamp = ticker,
            onDismiss = {
                // Return activity back to portrait or handle gracefully by switching sections
                viewModel.selectSection(ClockSection.ALARMS)
            }
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                bottomBar = {
                    // High fidelity segmented pill nav bar with flowing indicator
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(WindowInsets.navigationBars.asPaddingValues())
                            .padding(
                                horizontal = if (isLandscape) 48.dp else 24.dp,
                                vertical = if (isLandscape) 8.dp else 20.dp
                            )
                            .clip(RoundedCornerShape(28.dp))
                            .border(1.dp, Color(0x26FFFFFF), RoundedCornerShape(28.dp))
                            .background(Color(0x2B18181B))
                            .padding(4.dp)
                    ) {
                        val tabs = listOf(
                            ClockSection.ALARMS to "ALARMS",
                            ClockSection.WORLD_CLOCK to "WORLD",
                            ClockSection.STOPWATCH to "STOPWATCH",
                            ClockSection.TIMER to "TIMER"
                        )

                        val density = LocalDensity.current
                        var barHeight by remember { mutableStateOf(if (isLandscape) 36.dp else 44.dp) }
                        val selectedIdx = tabs.indexOfFirst { it.first == currentSection }.coerceAtLeast(0)
                        val tabWidth = maxWidth / tabs.size

                        // The flowing fluid background pill
                        val animatedOffset by animateDpAsState(
                            targetValue = tabWidth * selectedIdx,
                            animationSpec = spring(
                                dampingRatio = 0.9f, // Highly controlled, crisp and premium feel
                                stiffness = 1100f // Instant, fluid snapping response
                            ),
                            label = "pill_offset_anim"
                        )

                        Box(
                            modifier = Modifier
                                .width(tabWidth)
                                .height(barHeight)
                                .graphicsLayer {
                                    translationX = animatedOffset.toPx()
                                }
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color.White)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .onGloballyPositioned { coordinates ->
                                    barHeight = with(density) { coordinates.size.height.toDp() }
                                },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            tabs.forEach { (section, label) ->
                                val isSelected = currentSection == section
                                val tabText by animateColorAsState(
                                    targetValue = if (isSelected) Color.Black else Color(0xFF71717A),
                                    animationSpec = tween(200),
                                    label = "tab_label_color"
                                )

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(24.dp))
                                        .clickable { viewModel.selectSection(section) }
                                        .padding(vertical = if (isLandscape) 8.dp else 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        color = tabText,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }
                        }
                    }
                },
                contentWindowInsets = WindowInsets(0.dp)
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                        .padding(innerPadding)
                        .statusBarsPadding()
                ) {
                    // App Logo Title bar (hidden in landscape to optimize space)
                    if (!isLandscape) {
                        val headerPaddingStart by animateDpAsState(
                            targetValue = when (cutout) {
                                CameraCutout.LEFT_CORNER -> 50.dp
                                else -> 24.dp
                            },
                            label = "header_start_padding"
                        )

                        val headerPaddingEnd by animateDpAsState(
                            targetValue = when (cutout) {
                                CameraCutout.RIGHT_CORNER -> 54.dp
                                else -> 24.dp
                            },
                            label = "header_end_padding"
                        )

                        val headerPaddingTop by animateDpAsState(
                            targetValue = when (cutout) {
                                CameraCutout.LEFT_CORNER, CameraCutout.RIGHT_CORNER -> 6.dp
                                else -> 16.dp
                            },
                            label = "header_top_padding"
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    start = headerPaddingStart,
                                    end = headerPaddingEnd,
                                    top = headerPaddingTop,
                                    bottom = 16.dp
                               ),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "NOTHING",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )

                            // Minimalist signature + settings button
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .width(6.dp)
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(Color(0xFFFF2B2B))
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "CLOCK(R)",
                                        color = Color(0x77FFFFFF),
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Medium,
                                        letterSpacing = 1.sp
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(16.dp))

                                IconButton(
                                    onClick = { showSettingsDialog = true },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(18.dp))
                                        .testTag("settings_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Settings",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        // Divider line
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color(0xFF141414))
                        )
                    }

                    // Render Active Content Screen based on current section with sliding transitions
                    val sectionList = listOf(
                        ClockSection.ALARMS,
                        ClockSection.WORLD_CLOCK,
                        ClockSection.STOPWATCH,
                        ClockSection.TIMER
                    )

                    AnimatedContent(
                        targetState = currentSection,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        transitionSpec = {
                            val initialIdx = sectionList.indexOf(initialState)
                            val targetIdx = sectionList.indexOf(targetState)
                            
                            // Custom crisp spring spec for horizontal movement
                            val slideSpringSpec = spring<androidx.compose.ui.unit.IntOffset>(
                                dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy, // Sleek, clean slide without bouncy wobble
                                stiffness = 1500f // snappy medium-high stiffness for instant sliding
                            )
                            val fadeSpec = tween<Float>(
                                durationMillis = 180,
                                easing = androidx.compose.animation.core.FastOutLinearInEasing
                            )
                            
                            if (targetIdx > initialIdx) {
                                (slideInHorizontally(animationSpec = slideSpringSpec) { it } + fadeIn(animationSpec = fadeSpec))
                                    .togetherWith(slideOutHorizontally(animationSpec = slideSpringSpec) { -it } + fadeOut(animationSpec = fadeSpec))
                            } else {
                                (slideInHorizontally(animationSpec = slideSpringSpec) { -it } + fadeIn(animationSpec = fadeSpec))
                                    .togetherWith(slideOutHorizontally(animationSpec = slideSpringSpec) { it } + fadeOut(animationSpec = fadeSpec))
                            }
                        },
                        label = "tab_switch_anim"
                    ) { targetSection ->
                        when (targetSection) {
                            ClockSection.ALARMS -> {
                                AlarmsScreen(
                                    alarms = alarms,
                                    onToggleAlarm = { viewModel.toggleAlarm(it) },
                                    onAddAlarm = { h, m, days, label, vib ->
                                        viewModel.addAlarm(h, m, days, label, vib)
                                    },
                                    onDeleteAlarm = { viewModel.deleteAlarm(it) }
                                )
                            }
                            ClockSection.WORLD_CLOCK -> {
                                WorldClockScreen(
                                    currentTimestamp = ticker,
                                    clocks = worldClocks,
                                    onAddClock = { name, tz, country ->
                                        viewModel.addWorldClock(name, tz, country)
                                    },
                                    onDeleteClock = { viewModel.deleteWorldClock(it) }
                                )
                            }
                            ClockSection.STOPWATCH -> {
                                StopwatchScreen(
                                    timeMs = stopwatchTime,
                                    isRunning = isStopwatchRunning,
                                    laps = laps,
                                    onStart = { viewModel.startStopwatch() },
                                    onPause = { viewModel.pauseStopwatch() },
                                    onReset = { viewModel.resetStopwatch() },
                                    onLap = { viewModel.addLap() }
                                )
                            }
                            ClockSection.TIMER -> {
                                TimerScreen(
                                    durationInput = timerDurationInput,
                                    secondsRemaining = timerSecondsRemaining,
                                    timerState = timerState,
                                    percentage = viewModel.timerPercentage,
                                    onSetDuration = { viewModel.setTimerDuration(it) },
                                    onStart = { viewModel.startTimer() },
                                    onPause = { viewModel.pauseTimer() },
                                    onReset = { viewModel.resetTimer() },
                                    onClear = { viewModel.clearTimer() }
                                )
                            }
                        }
                    }
                }
            }

            // Visual overlay for simulated hardware/software bezel camera cutout
            if (!isLandscape) {
                CameraCutoutVisualizer(cutout = cutout)
            }

            // Overlay settings dialog when active
            if (showSettingsDialog) {
                CameraCutoutsSettingsDialog(
                    currentCutout = cutout,
                    onCutoutSelected = { viewModel.setCameraCutout(it) },
                    onDismiss = { showSettingsDialog = false }
                )
            }
        }
    }
}

@Composable
fun CameraCutoutVisualizer(cutout: CameraCutout) {
    if (cutout == CameraCutout.NONE) return

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        val alignment = when (cutout) {
            CameraCutout.LEFT_CORNER -> Alignment.TopStart
            CameraCutout.CENTER -> Alignment.TopCenter
            CameraCutout.RIGHT_CORNER -> Alignment.TopEnd
            else -> Alignment.TopCenter
        }

        val paddingStart = if (cutout == CameraCutout.LEFT_CORNER) 18.dp else 0.dp
        val paddingEnd = if (cutout == CameraCutout.RIGHT_CORNER) 18.dp else 0.dp
        val paddingTop = 12.dp

        // Sleek virtual camera lens with realistic reflections
        Box(
            modifier = Modifier
                .align(alignment)
                .padding(start = paddingStart, end = paddingEnd, top = paddingTop)
                .size(22.dp)
                .border(2.dp, Color(0xFF1E1E1E), CircleShape)
                .background(Color(0xFF070707))
                .clip(CircleShape)
        ) {
            // Lens micro aperture highlight (semi-reflective glass)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 3.dp, end = 3.dp)
                    .size(5.dp)
                    .background(Color(0x44FFFFFF), CircleShape)
            )
            // Lens core sensor dot
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(8.dp)
                    .background(Color(0xFF030303), CircleShape)
                    .border(0.5.dp, Color(0x22FFFFFF), CircleShape)
            )
        }
    }
}

@Composable
fun CameraCutoutsSettingsDialog(
    currentCutout: CameraCutout,
    onCutoutSelected: (CameraCutout) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF0E0E0E))
                .border(1.dp, Color(0x20FFFFFF), RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "SETTINGS",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "SELECT SCREEN CAMERA CUTOUT",
                    color = Color(0x88FFFFFF),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Cutout Selector Options
                val options = listOf(
                    CameraCutout.NONE to "NONE",
                    CameraCutout.LEFT_CORNER to "LEFT CORNER",
                    CameraCutout.CENTER to "CENTER CLOUD",
                    CameraCutout.RIGHT_CORNER to "RIGHT CORNER"
                )
                
                options.forEach { (option, label) ->
                    val isSelected = option == currentCutout
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color(0xFF141414) else Color.Transparent)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Color(0xFFFF2B2B) else Color(0x1AFFFFFF),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { onCutoutSelected(option) }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color.White else Color(0x99FFFFFF),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                        
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, if (isSelected) Color(0xFFFF2B2B) else Color(0x40FFFFFF), CircleShape)
                                .background(if (isSelected) Color(0xFFFF2B2B) else Color.Transparent)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Finish / Save button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .border(1.dp, Color.White, RoundedCornerShape(24.dp))
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "DONE",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                }
            }
        }
    }
}
