package io.github.landerlyoung.flashappsearch.search.widget

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import io.github.landerlyoung.flashappsearch.R
import io.github.landerlyoung.flashappsearch.search.ui.AppSearchActivity


/**
 * <pre>
 * Author: landerlyoung@gmail.com
 * Date:   2018-06-26
 * Time:   00:27
 * Life with Passion, Code with Creativity.
 * </pre>
 */
class AppSearchWidgetProvider : AppWidgetProvider() {
    @SuppressLint("InlinedApi")
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        for (element in appWidgetIds) {
            val intent = Intent(context, AppSearchActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

            // to the button
            val views = RemoteViews(context.packageName, R.layout.search_widget_layout)
            views.setOnClickPendingIntent(R.id.search_widget, pendingIntent)

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(element, views)
        }
    }
}