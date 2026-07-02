package com.example

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.widget.RemoteViews

class ClockWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidgetWithOptions(context, appWidgetManager, appWidgetId, null)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        updateAppWidgetWithOptions(context, appWidgetManager, appWidgetId, newOptions)
    }

    private fun updateAppWidgetWithOptions(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        options: Bundle?
    ) {
        val views = RemoteViews(context.packageName, R.layout.clock_widget_layout)

        val finalOptions = options ?: appWidgetManager.getAppWidgetOptions(appWidgetId)
        val minWidth = finalOptions?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH) ?: 140
        val minHeight = finalOptions?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT) ?: 60

        // Calculate dynamic font sizes based on widget width & height boundaries
        val widthFontFactor = minWidth * 0.35f
        val heightFontFactor = minHeight * 0.65f
        val textFontSize = minOf(widthFontFactor, heightFontFactor).coerceIn(32f, 160f)
        val dateFontSize = (textFontSize * 0.25f).coerceIn(10f, 28f)

        // Set text sizes on remote views dynamically
        views.setTextViewTextSize(R.id.widget_text_clock, TypedValue.COMPLEX_UNIT_SP, textFontSize)
        views.setTextViewTextSize(R.id.widget_date_clock, TypedValue.COMPLEX_UNIT_SP, dateFontSize)

        // Clicking the widget launches MainActivity
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
