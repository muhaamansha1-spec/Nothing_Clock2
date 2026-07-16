package com.example

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.widget.RemoteViews
import com.example.service.MediaPlaybackManager

class MusicWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_PREVIOUS = "com.example.action.PREVIOUS"
        const val ACTION_PLAY_PAUSE = "com.example.action.PLAY_PAUSE"
        const val ACTION_NEXT = "com.example.action.NEXT"

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, MusicWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            if (appWidgetIds.isNotEmpty()) {
                val intent = Intent(context, MusicWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
                }
                context.sendBroadcast(intent)
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_PREVIOUS -> {
                MediaPlaybackManager.skipToPrevious()
                updateAllWidgets(context)
            }
            ACTION_PLAY_PAUSE -> {
                if (MediaPlaybackManager.isPlaying.value) {
                    MediaPlaybackManager.pause()
                } else {
                    MediaPlaybackManager.play()
                }
                updateAllWidgets(context)
            }
            ACTION_NEXT -> {
                MediaPlaybackManager.skipToNext()
                updateAllWidgets(context)
            }
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.music_widget_layout)

        val isPlaying = MediaPlaybackManager.isPlaying.value
        val title = MediaPlaybackManager.trackTitle.value
        val artist = MediaPlaybackManager.trackArtist.value
        val hasActiveSession = MediaPlaybackManager.hasActiveSession.value
        val albumArtBitmap = MediaPlaybackManager.albumArt.value

        // 1. Dynamic styling for Album Art Box (with Nothing-themed custom placeholder fallback)
        if (albumArtBitmap != null) {
            try {
                val roundedArt = getRoundedAlbumArt(albumArtBitmap, 16f)
                views.setImageViewBitmap(R.id.widget_album_art, roundedArt)
            } catch (e: Exception) {
                // If rounding fails, fallback to direct bitmap
                views.setImageViewBitmap(R.id.widget_album_art, albumArtBitmap)
            }
        } else {
            val placeholder = createPlaceholderAlbumArt(120, 120)
            views.setImageViewBitmap(R.id.widget_album_art, placeholder)
        }

        // 2. Set dynamic Title and Artist labels (separately, with larger Title size)
        val titleText = if (hasActiveSession && title.isNotEmpty()) {
            title.uppercase()
        } else {
            "TAP TO PLAY MUSIC"
        }
        val artistText = if (hasActiveSession && artist.isNotEmpty()) {
            artist.uppercase()
        } else {
            "NO ACTIVE SESSION"
        }
        views.setTextViewText(R.id.widget_music_title, titleText)
        views.setTextViewText(R.id.widget_music_artist, artistText)

        // 3. Render dot matrix bitmap (Active color: White, Inactive color: subtle 10% translucent white)
        val activeColor = Color.WHITE
        val inactiveColor = Color.argb(26, 255, 255, 255) // 0x1AFFFFFF (approx 10% opacity)
        val dotMatrixBitmap = createMusicDotMatrixBitmap(activeColor, inactiveColor, isPlaying)

        // Set the rendered dot matrix bitmap on the ImageView
        views.setImageViewBitmap(R.id.widget_dot_matrix_image, dotMatrixBitmap)

        // 4. Dynamic styling for Play/Pause button based on state
        if (isPlaying) {
            views.setTextViewText(R.id.widget_btn_play_pause, "▮▮")
            views.setTextColor(R.id.widget_btn_play_pause, Color.WHITE)
            views.setInt(R.id.widget_btn_play_pause, "setBackgroundResource", R.drawable.widget_button_pause)
        } else {
            views.setTextViewText(R.id.widget_btn_play_pause, "▶")
            views.setTextColor(R.id.widget_btn_play_pause, Color.BLACK)
            views.setInt(R.id.widget_btn_play_pause, "setBackgroundResource", R.drawable.widget_button_play)
        }

        // BINDING BROADCAST INTENTS TO WIDGET BUTTONS:
        
        // Button: Previous
        val prevIntent = Intent(context, MusicWidgetProvider::class.java).apply {
            action = ACTION_PREVIOUS
        }
        val prevPendingIntent = PendingIntent.getBroadcast(
            context,
            2,
            prevIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_btn_prev, prevPendingIntent)

        // Button: Play / Pause
        val playPauseIntent = Intent(context, MusicWidgetProvider::class.java).apply {
            action = ACTION_PLAY_PAUSE
        }
        val playPausePendingIntent = PendingIntent.getBroadcast(
            context,
            3,
            playPauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_btn_play_pause, playPausePendingIntent)

        // Button: Next
        val nextIntent = Intent(context, MusicWidgetProvider::class.java).apply {
            action = ACTION_NEXT
        }
        val nextPendingIntent = PendingIntent.getBroadcast(
            context,
            4,
            nextIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_btn_next, nextPendingIntent)

        // Create robust intent to launch the default music application on background click
        val backgroundIntent = getMusicAppLaunchIntent(context)
        val backgroundPendingIntent = PendingIntent.getActivity(
            context,
            1, // request code 1 for background click
            backgroundIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, backgroundPendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun getMusicAppLaunchIntent(context: Context): Intent {
        // First check for user-selected music app package in SharedPreferences
        try {
            val sharedPrefs = context.getSharedPreferences("widget_settings", Context.MODE_PRIVATE)
            val selectedPackage = sharedPrefs.getString("widget_music_app_package", null)
            if (!selectedPackage.isNullOrEmpty()) {
                val launchIntent = context.packageManager.getLaunchIntentForPackage(selectedPackage)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    return launchIntent
                }
            }
        } catch (e: Exception) {
            // Fall back to automatic detection
        }

        // Standard Android way to query/launch default music app
        try {
            val intent = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_MUSIC).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                return intent
            }
        } catch (e: Exception) {
            // ignore and try next
        }

        try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_APP_MUSIC)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                return intent
            }
        } catch (e: Exception) {
            // ignore and try next
        }

        // Try standard MediaStore action
        try {
            val intent = Intent(android.provider.MediaStore.INTENT_ACTION_MUSIC_PLAYER).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                return intent
            }
        } catch (e: Exception) {
            // ignore
        }

        // Final fallback: open Main App
        return Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    private fun getRoundedAlbumArt(bitmap: Bitmap, cornerRadiusPx: Float): Bitmap {
        val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val rect = android.graphics.RectF(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())
        
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = Color.BLACK
        canvas.drawRoundRect(rect, cornerRadiusPx, cornerRadiusPx, paint)
        
        paint.xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return output
    }

    private fun createPlaceholderAlbumArt(width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        
        // Background: Dark Gray
        paint.color = Color.parseColor("#161616")
        val rect = android.graphics.RectF(0f, 0f, width.toFloat(), height.toFloat())
        canvas.drawRoundRect(rect, 16f, 16f, paint)
        
        // Draw dot matrix grid inside placeholder
        paint.color = Color.argb(30, 255, 255, 255) // light translucent dots
        val cols = 5
        val rows = 5
        val cellW = width.toFloat() / (cols + 1)
        val cellH = height.toFloat() / (rows + 1)
        for (r in 1..rows) {
            for (c in 1..cols) {
                // Draw a red dot in the very center, gray dots elsewhere
                if (r == 3 && c == 3) {
                    paint.color = Color.rgb(255, 43, 43)
                    canvas.drawCircle(c * cellW, r * cellH, 4f, paint)
                    paint.color = Color.argb(30, 255, 255, 255)
                } else {
                    canvas.drawCircle(c * cellW, r * cellH, 2f, paint)
                }
            }
        }
        return bitmap
    }

    private fun createMusicDotMatrixBitmap(
        activeColor: Int,
        inactiveColor: Int,
        isPlaying: Boolean
    ): Bitmap {
        val dotSize = 16f
        val dotSpacing = 4f
        val charSpacing = 24f
        val paddingX = 24f
        val paddingY = 16f

        val charWidth = 5 * dotSize + 4 * dotSpacing
        val charHeight = 7 * dotSize + 6 * dotSpacing

        // Dot matrix grid for Note:
        val noteGrid = booleanArrayOf(
            false, false, true,  true,  true,
            false, false, true,  false, true,
            false, false, true,  false, true,
            false, false, true,  false, true,
            true,  true,  true,  true,  true,
            true,  true,  true,  true,  true,
            false, false, false, false, false
        )

        val barCount = 10
        val barWidth = dotSize
        val barSpacing = dotSpacing + 2f
        val eqWidth = barCount * barWidth + (barCount - 1) * barSpacing
        
        val totalWidth = (paddingX * 2 + charWidth + charSpacing + eqWidth).toInt()
        val totalHeight = (charHeight + 2 * paddingY).toInt()

        val bitmap = Bitmap.createBitmap(totalWidth, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        canvas.drawColor(Color.TRANSPARENT)

        // Helper to draw a 5x7 grid
        fun drawGrid(startX: Float, grid: BooleanArray, isRed: Boolean = false) {
            for (row in 0 until 7) {
                for (col in 0 until 5) {
                    val isActive = grid[row * 5 + col]
                    paint.color = if (isActive) {
                        if (isRed) Color.rgb(255, 43, 43) else activeColor
                    } else {
                        inactiveColor
                    }
                    val cx = startX + col * (dotSize + dotSpacing) + dotSize / 2f
                    val cy = paddingY + row * (dotSize + dotSpacing) + dotSize / 2f
                    canvas.drawCircle(cx, cy, dotSize / 2f, paint)
                }
            }
        }

        var currentX = paddingX

        // 1. Draw Red Music Note Icon
        drawGrid(currentX, noteGrid, isRed = true)
        currentX += charWidth + charSpacing

        // 2. Draw dot matrix Equalizer
        val waveHeights = if (isPlaying) {
            intArrayOf(4, 6, 7, 5, 3, 6, 2, 4, 6, 3)
        } else {
            intArrayOf(1, 2, 1, 1, 2, 1, 1, 1, 2, 1)
        }
        for (b in 0 until barCount) {
            val h = waveHeights[b]
            for (row in 0 until 7) {
                val isActive = row >= (7 - h)
                paint.color = if (isActive) Color.rgb(255, 43, 43) else inactiveColor
                
                val cx = currentX + b * (barWidth + barSpacing) + barWidth / 2f
                val cy = paddingY + row * (dotSize + dotSpacing) + dotSize / 2f
                canvas.drawCircle(cx, cy, dotSize / 2f, paint)
            }
        }

        return bitmap
    }
}
