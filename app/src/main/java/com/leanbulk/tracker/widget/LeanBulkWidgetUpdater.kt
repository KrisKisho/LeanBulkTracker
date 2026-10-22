package com.leanbulk.tracker.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object LeanBulkWidgetUpdater {
    fun update(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                LeanBulkGlanceWidget().updateAll(context)
            } catch (_: Throwable) {
                // Ignore if no widget placed on homescreen yet
            }
        }
    }
}
