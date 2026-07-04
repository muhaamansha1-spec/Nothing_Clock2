package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.WorldClock
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WorldClockScreen(
    currentTimestamp: Long,
    clocks: List<WorldClock>,
    is24Hour: Boolean = true,
    onAddClock: (cityName: String, timezoneId: String, country: String) -> Unit,
    onDeleteClock: (WorldClock) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    // Hardcoded high-quality pre-configured locations to add from
    val availableCities = listOf(
        CityData("London", "Europe/London", "UK"),
        CityData("New York", "America/New_York", "USA"),
        CityData("Auckland", "Pacific/Auckland", "New Zealand"),
        CityData("Bangkok", "Asia/Bangkok", "Thailand"),
        CityData("Beijing", "Asia/Shanghai", "China"),
        CityData("Berlin", "Europe/Berlin", "Germany"),
        CityData("Cairo", "Africa/Cairo", "Egypt"),
        CityData("Cape Town", "Africa/Johannesburg", "South Africa"),
        CityData("Chicago", "America/Chicago", "USA"),
        CityData("Dubai", "Asia/Dubai", "UAE"),
        CityData("Hong Kong", "Asia/Hong_Kong", "China"),
        CityData("Istanbul", "Europe/Istanbul", "Turkey"),
        CityData("Los Angeles", "America/Los_Angeles", "USA"),
        CityData("Mumbai", "Asia/Kolkata", "India"),
        CityData("Paris", "Europe/Paris", "France"),
        CityData("Rio de Janeiro", "America/Sao_Paulo", "Brazil"),
        CityData("Seoul", "Asia/Seoul", "South Korea"),
        CityData("Singapore", "Asia/Singapore", "Singapore"),
        CityData("Sydney", "Australia/Sydney", "Australia"),
        CityData("Tokyo", "Asia/Tokyo", "Japan"),
        CityData("Vancouver", "America/Vancouver", "Canada")
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // Home / Primary Clock (dot matrix styling) represented dramatically
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val cal = Calendar.getInstance()
                val tzName = cal.timeZone.id.substringAfter("/").replace("_", " ")
                val timePattern = if (is24Hour) "HH:mm" else "hh:mm a"
                val mainTime = SimpleDateFormat(timePattern, Locale.getDefault()).format(Date(currentTimestamp))
                
                Text(
                    text = "LOCAL TIME · $tzName".uppercase(),
                    color = Color(0xFFFF2B2B), // Nothing red accent
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Extra big elegant digital readout
                Text(
                    text = mainTime,
                    color = Color.White,
                    fontSize = 54.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-1).sp
                )
            }

            // Divider styled with subtle dot pattern or subtle border
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFF161616))
            )

            NothingWorldMap(
                clocks = clocks,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )

            if (clocks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "World Map",
                            tint = Color(0x33FFFFFF),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "NO WORLD CLOCKS",
                            color = Color(0xFF666666),
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 2.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    items(clocks, key = { it.id }) { clock ->
                        WorldClockItemRow(
                            clock = clock,
                            currentTimestamp = currentTimestamp,
                            is24Hour = is24Hour,
                            onDelete = { onDeleteClock(clock) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }
            }
        }

        // Search/Add floating pill button
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier
                    .testTag("add_world_clock_button")
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.White)
                    .clickable { showAddDialog = true }
                    .padding(horizontal = 24.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add World Clock",
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ADD CITY",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }
        }

        if (showAddDialog) {
            AddCityDialog(
                availableCities = availableCities,
                currentClocks = clocks,
                onDismiss = { showAddDialog = false },
                onAddCity = { city ->
                    onAddClock(city.cityName, city.timezoneId, city.country)
                    showAddDialog = false
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WorldClockItemRow(
    clock: WorldClock,
    currentTimestamp: Long,
    is24Hour: Boolean,
    onDelete: () -> Unit
) {
    val pattern = if (is24Hour) "HH:mm" else "hh:mm a"
    val formatter = SimpleDateFormat(pattern, Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone(clock.timezoneId)
    }
    
    val timeString = formatter.format(Date(currentTimestamp))

    // Calculate timezone difference compared to local timezone
    val localTz = Calendar.getInstance().timeZone
    val targetTz = TimeZone.getTimeZone(clock.timezoneId)
    val diffMs = targetTz.getOffset(currentTimestamp) - localTz.getOffset(currentTimestamp)
    val diffHours = diffMs / (1000 * 60 * 60)
    
    val diffText = when {
        diffHours > 0 -> "+${diffHours}H AHEAD"
        diffHours < 0 -> "${diffHours}H BEHIND"
        else -> "SAME TIME"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(40.dp))
            .border(1.dp, Color(0x26FFFFFF), RoundedCornerShape(40.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0x2B18181B))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            ) {
                Text(
                    text = clock.cityName.uppercase(),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    maxLines = 1,
                    modifier = Modifier.basicMarquee()
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${clock.country.uppercase()} · $diffText",
                    color = Color(0xFF888888),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp,
                    maxLines = 1,
                    modifier = Modifier.basicMarquee()
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = timeString,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.width(16.dp))

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .testTag("delete_world_clock_row_btn")
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1A1A1A))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove City",
                        tint = Color(0xFF888888),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

data class CityData(
    val cityName: String,
    val timezoneId: String,
    val country: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCityDialog(
    availableCities: List<CityData>,
    currentClocks: List<WorldClock>,
    onDismiss: () -> Unit,
    onAddCity: (CityData) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredCities = availableCities.filter {
        it.cityName.lowercase().contains(searchQuery.lowercase()) ||
        it.country.lowercase().contains(searchQuery.lowercase())
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
                .border(1.dp, Color(0xFF2C2C2C), RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Black)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Text(
                    text = "ADD WORLD CLOCK",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Search Box styled inside a neat pill
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("SEARCH CITY...", color = Color(0xFF444444), fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color(0xFF2C2C2C),
                        focusedContainerColor = Color(0xFF0F0F0F),
                        unfocusedContainerColor = Color(0xFF0F0F0F)
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 13.sp),
                    modifier = Modifier.fillMaxWidth().testTag("city_search_bar")
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val alreadyAddedNames = currentClocks.map { it.cityName.lowercase() }
                    val itemsToDisplay = filteredCities.filter { !alreadyAddedNames.contains(it.cityName.lowercase()) }

                    if (itemsToDisplay.isEmpty()) {
                        item {
                            Text(
                                text = "NO CITIES FOUND",
                                color = Color(0xFF444444),
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp)
                            )
                        }
                    } else {
                        items(itemsToDisplay) { city ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { onAddCity(city) },
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0A0A)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF131313))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = city.cityName.uppercase(),
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontFamily = FontFamily.SansSerif,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        )
                                        Text(
                                            text = city.country.uppercase(),
                                            color = Color(0xFF888888),
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            letterSpacing = 1.sp
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color.White)
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "ADD",
                                            color = Color.Black,
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

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF151515))
                ) {
                    Text(
                        text = "CLOSE",
                        color = Color(0xFF888888),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun NothingWorldMap(
    clocks: List<WorldClock>,
    modifier: Modifier = Modifier
) {
    val cityCoordinates = remember {
        mapOf(
            "london" to Pair(0.48f, 0.25f),
            "new york" to Pair(0.28f, 0.35f),
            "auckland" to Pair(0.95f, 0.88f),
            "bangkok" to Pair(0.77f, 0.52f),
            "beijing" to Pair(0.80f, 0.35f),
            "berlin" to Pair(0.52f, 0.25f),
            "cairo" to Pair(0.57f, 0.42f),
            "cape town" to Pair(0.53f, 0.78f),
            "chicago" to Pair(0.24f, 0.35f),
            "dubai" to Pair(0.63f, 0.45f),
            "hong kong" to Pair(0.80f, 0.48f),
            "istanbul" to Pair(0.55f, 0.35f),
            "los angeles" to Pair(0.12f, 0.40f),
            "mumbai" to Pair(0.70f, 0.50f),
            "paris" to Pair(0.49f, 0.27f),
            "rio de janeiro" to Pair(0.38f, 0.70f),
            "seoul" to Pair(0.83f, 0.36f),
            "singapore" to Pair(0.77f, 0.58f),
            "sydney" to Pair(0.92f, 0.82f),
            "tokyo" to Pair(0.86f, 0.38f),
            "vancouver" to Pair(0.12f, 0.25f)
        )
    }

    val mapRows = remember {
        listOf(
            "........X.......X...............", // 0
            "......XXX......XX......XXXXX....", // 1
            "    XXXXX    XXXXXX  XXXXXXXXX. ", // 2
            "   XXXXXX   XXXXXXXXXXXXXXXXXXX ", // 3
            "   XXXXXX   XXXXXXXXXXXXXXXXXX  ", // 4
            "   .XXXX.   .XXXXXXXXXXXXXXXXX. ", // 5
            "    .XX.     .XXXXXXXXXXXXXXX.  ", // 6
            "     ..      .XXXX..X...XXXX.   ", // 7
            "             .XXX.     .XXXX.   ", // 8
            "              XX.       .XX.    ", // 9
            "              X.                ", // 10
            "                                "  // 11
        )
    }

    val top5Clocks = remember(clocks) { clocks.take(5) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 3f,
        targetValue = 9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF0C0C0F))
            .border(1.dp, Color(0x1FFFFFFF), RoundedCornerShape(20.dp))
    ) {
        val width = maxWidth
        val height = maxHeight

        Canvas(modifier = Modifier.fillMaxSize()) {
            val rows = 12
            val cols = 32
            val cellWidth = size.width / cols
            val cellHeight = size.height / rows

            // Draw dot grid
            for (r in 0 until rows) {
                val rowStr = mapRows.getOrNull(r) ?: ""
                for (c in 0 until cols) {
                    val isLand = rowStr.getOrNull(c) == 'X' || rowStr.getOrNull(c) == '.'
                    val color = if (isLand) Color(0x2EFFFFFF) else Color(0x0AFFFFFF)
                    val cx = c * cellWidth + cellWidth / 2f
                    val cy = r * cellHeight + cellHeight / 2f
                    drawCircle(
                        color = color,
                        radius = 1.5.dp.toPx(),
                        center = androidx.compose.ui.geometry.Offset(cx, cy)
                    )
                }
            }

            // Draw technical lines (coordinate axes or grid lines)
            val accentPaintColor = Color(0x14FFFFFF)
            drawLine(
                color = accentPaintColor,
                start = androidx.compose.ui.geometry.Offset(0f, size.height / 2f),
                end = androidx.compose.ui.geometry.Offset(size.width, size.height / 2f),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = accentPaintColor,
                start = androidx.compose.ui.geometry.Offset(size.width / 2f, 0f),
                end = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height),
                strokeWidth = 1.dp.toPx()
            )

            // Draw pulsing and solid pins
            top5Clocks.forEach { clock ->
                val key = clock.cityName.lowercase()
                val coords = cityCoordinates[key] ?: run {
                    val offsetHrs = TimeZone.getTimeZone(clock.timezoneId).rawOffset / (1000 * 60 * 60f)
                    val fx = (0.48f + (offsetHrs / 12f) * 0.45f).coerceIn(0.05f, 0.95f)
                    Pair(fx, 0.4f)
                }

                val px = coords.first * size.width
                val py = coords.second * size.height

                // Pulse ring
                drawCircle(
                    color = Color(0xFFFF2B2B),
                    radius = pulseRadius.dp.toPx(),
                    alpha = pulseAlpha,
                    center = androidx.compose.ui.geometry.Offset(px, py)
                )

                // Solid center
                drawCircle(
                    color = Color(0xFFFF2B2B),
                    radius = 3.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(px, py)
                )
            }
        }

        // Technical borders/labels
        Text(
            text = "NOTHING GLYPH WORLD PROJECTION",
            color = Color(0x33FFFFFF),
            fontSize = 7.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp)
        )

        Text(
            text = "PINNED CITIES: ${top5Clocks.size}/5",
            color = Color(0x33FFFFFF),
            fontSize = 7.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp)
        )

        // Labels
        top5Clocks.forEach { clock ->
            val key = clock.cityName.lowercase()
            val coords = cityCoordinates[key] ?: run {
                val offsetHrs = TimeZone.getTimeZone(clock.timezoneId).rawOffset / (1000 * 60 * 60f)
                val fx = (0.48f + (offsetHrs / 12f) * 0.45f).coerceIn(0.05f, 0.95f)
                Pair(fx, 0.4f)
            }

            val xDp = width * coords.first
            val yDp = height * coords.second

            // Draw a tiny visual label card next to the dot coordinate
            Box(
                modifier = Modifier
                    .offset(x = xDp - 16.dp, y = yDp - 15.dp)
                    .background(Color(0xE60A0A0C), RoundedCornerShape(4.dp))
                    .border(0.5.dp, Color(0x26FFFFFF), RoundedCornerShape(4.dp))
                    .padding(horizontal = 3.dp, vertical = 1.dp)
            ) {
                Text(
                    text = clock.cityName.uppercase().take(3),
                    color = Color.White,
                    fontSize = 7.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
