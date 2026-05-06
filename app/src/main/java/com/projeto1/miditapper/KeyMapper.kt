package com.projeto1.miditapper

object KeyMapper {
    val defaultKeys = listOf("1","2","3","4","5","6","7","8","9","0","Q","E","R","T","Y","U","P")

    private val noteToKey = mapOf(
        0 to "1", 1 to "Q", 2 to "2", 3 to "E", 4 to "3", 5 to "4", 6 to "R", 7 to "5", 8 to "T", 9 to "6", 10 to "Y", 11 to "7"
    )

    fun midiNoteToKey(note: Int): String? {
        val octaveShift = ((note - 60) / 12).coerceIn(0, 1)
        val base = ((note % 12) + 12) % 12
        val lower = noteToKey[base] ?: return null
        if (octaveShift == 0) return lower
        return when (lower) {
            "1" -> "8"
            "Q" -> "U"
            "2" -> "9"
            "E" -> "P"
            "3" -> "0"
            else -> lower
        }
    }
}
