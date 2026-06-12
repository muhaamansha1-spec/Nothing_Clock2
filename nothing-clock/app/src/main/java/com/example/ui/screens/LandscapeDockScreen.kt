package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.DotMatrixString
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

/**
 * Highly aesthetic, full screen landscape dock clock designed for AMOLED displays.
 * Incorporates a subtle shift animation that slowly orbits the time based on minutes
 * to prevent pixel burn-in.
 */
@Composable
fun LandscapeDockScreen(
    currentTimestamp: Long,
    onDismiss: () -> Unit
) {
    val date = Date(currentTimestamp)
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())
    
    val timeStr = timeFormat.format(date)
    val dateStr = dateFormat.format(date).uppercase()

    // Calculate shift offset to prevent Amoled burn-in
    // The offset slowly orbits a small circle over time
    val calendar = Calendar.getInstance().apply { timeInMillis = currentTimestamp }
    val minute = calendar.get(Calendar.MINUTE)
    val second = calendar.get(Calendar.SECOND)
    val angle = (minute + second / 60.0) * (2 * Math.PI / 60.0)
    
    // Max 15 pixels of motion
    val offsetX = (15 * cos(angle)).toInt()
    val offsetY = (15 * sin(angle)).toInt()

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
        Column(
            modifier = Modifier
                .offset { IntOffset(offsetX, offsetY) },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Elegant dot matrix display for hours and minutes
            DotMatrixString(
                text = timeStr.take(5), // HH:mm
                activeColor = Color.White,
                inactiveColor = Color(0x11FFFFFF),
                charSpacing = 16.dp,
                dotSize = 10.dp,
                dotSpacing = 3.dp
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Pulsing second digits
            Text(
                text = ". ${timeStr.takeLast(2)}",
                color = Color(0xFFFF2B2B), // Distinct red second indicator
                fontSize = 32.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Minimalist date label
            Text(
                text = dateStr,
                color = Color(0xFF888888),
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                letterSpacing = 3.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "TAP TO EXIT",
                color = Color(0x33FFFFFF),
                fontSize = 10.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Normal,
                letterSpacing = 4.sp
            )
        }
    }
}
