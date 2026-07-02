package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
                val mainTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(currentTimestamp))
                
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

@Composable
fun WorldClockItemRow(
    clock: WorldClock,
    currentTimestamp: Long,
    onDelete: () -> Unit
) {
    val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault()).apply {
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
            Column {
                Text(
                    text = clock.cityName.uppercase(),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${clock.country.uppercase()} · $diffText",
                    color = Color(0xFF888888),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp
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
