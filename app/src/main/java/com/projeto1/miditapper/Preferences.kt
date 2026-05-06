package com.projeto1.miditapper

import android.content.Context

object Preferences {
    private const val NAME = "projeto1_prefs"

    fun loadKeyPoints(context: Context): List<KeyPoint> {
        val prefs = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
        val defaults = defaultKeyPoints(context)
        return KeyMapper.defaultKeys.map { key ->
            val def = defaults.first { it.key == key }
            KeyPoint(
                key = key,
                x = prefs.getInt("${key}_x", def.x),
                y = prefs.getInt("${key}_y", def.y)
            )
        }
    }

    fun saveKeyPoints(context: Context, points: List<KeyPoint>) {
        val editor = context.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit()
        points.forEach { point ->
            editor.putInt("${point.key}_x", point.x)
            editor.putInt("${point.key}_y", point.y)
        }
        editor.apply()
    }

    fun defaultKeyPoints(context: Context): List<KeyPoint> {
        val display = context.resources.displayMetrics
        val w = display.widthPixels
        val h = display.heightPixels
        val whiteY = (h * 0.34f).toInt()
        val blackY = (h * 0.21f).toInt()
        val whiteXs = listOf(0.255f, 0.312f, 0.366f, 0.425f, 0.474f, 0.532f, 0.589f, 0.645f, 0.701f, 0.757f)
        val black = mapOf(
            "Q" to 0.280f, "E" to 0.333f, "R" to 0.447f, "T" to 0.501f, "Y" to 0.555f, "U" to 0.667f, "P" to 0.721f
        )
        val whites = listOf("1","2","3","4","5","6","7","8","9","0").zip(whiteXs).map { (k, rx) -> KeyPoint(k, (w * rx).toInt(), whiteY) }
        val blacks = black.map { (k, rx) -> KeyPoint(k, (w * rx).toInt(), blackY) }
        return (whites + blacks).sortedBy { KeyMapper.defaultKeys.indexOf(it.key) }
    }
}
