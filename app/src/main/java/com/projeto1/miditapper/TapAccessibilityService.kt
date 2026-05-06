package com.projeto1.miditapper

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent

class TapAccessibilityService : AccessibilityService() {
    private val handler = Handler(Looper.getMainLooper())
    private var running = false

    override fun onServiceConnected() {
        current = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

    override fun onInterrupt() {
        stop()
    }

    override fun onDestroy() {
        if (current === this) current = null
        stop()
        super.onDestroy()
    }

    private fun stop() {
        running = false
        handler.removeCallbacksAndMessages(null)
    }

    private fun tap(x: Int, y: Int) {
        val path = Path().apply { moveTo(x.toFloat(), y.toFloat()) }
        val stroke = GestureDescription.StrokeDescription(path, 0, 60)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        dispatchGesture(gesture, null, null)
    }

    private fun play(events: List<TapEvent>, points: Map<String, KeyPoint>, speed: Float, startDelayMs: Long) {
        stop()
        running = true
        val safeSpeed = speed.coerceIn(0.25f, 4f)
        events.forEach { event ->
            val point = points[event.key.uppercase()]
            if (point != null) {
                val at = startDelayMs + (event.delayMs / safeSpeed).toLong()
                handler.postDelayed({ if (running) tap(point.x, point.y) }, at)
            }
        }
        val end = startDelayMs + ((events.maxOfOrNull { it.delayMs } ?: 0L) / safeSpeed).toLong() + 1000
        handler.postDelayed({ running = false }, end)
    }

    companion object {
        @Volatile private var current: TapAccessibilityService? = null

        fun isReady(): Boolean = current != null

        fun stopPlayback() {
            current?.stop()
        }

        fun play(events: List<TapEvent>, points: List<KeyPoint>, speed: Float, startDelayMs: Long): Boolean {
            val service = current ?: return false
            service.play(events, points.associateBy { it.key.uppercase() }, speed, startDelayMs)
            return true
        }
    }
}
