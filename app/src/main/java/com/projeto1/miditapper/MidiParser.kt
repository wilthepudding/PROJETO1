package com.projeto1.miditapper

import java.io.ByteArrayInputStream
import java.io.DataInputStream
import kotlin.math.max

object MidiParser {
    fun parse(bytes: ByteArray): List<TapEvent> {
        val input = DataInputStream(ByteArrayInputStream(bytes))
        if (readAscii(input, 4) != "MThd") return emptyList()
        val headerLength = input.readInt()
        val format = input.readUnsignedShort()
        val trackCount = input.readUnsignedShort()
        val division = input.readUnsignedShort()
        if (headerLength > 6) input.skipBytes(headerLength - 6)
        val ticksPerQuarter = division and 0x7FFF
        var tempoUsPerQuarter = 500000
        val events = mutableListOf<Pair<Long, Int>>()

        repeat(trackCount) {
            if (input.available() <= 0) return@repeat
            val id = readAscii(input, 4)
            val length = input.readInt()
            val trackBytes = ByteArray(length)
            input.readFully(trackBytes)
            if (id == "MTrk") {
                events += parseTrack(trackBytes, ticksPerQuarter, tempoUsPerQuarter) { tempoUsPerQuarter = it }
            }
        }

        return events
            .sortedBy { it.first }
            .mapNotNull { (ms, note) -> KeyMapper.midiNoteToKey(note)?.let { TapEvent(ms, it) } }
            .distinctBy { it.delayMs to it.key }
    }

    private fun parseTrack(bytes: ByteArray, tpq: Int, initialTempo: Int, onTempo: (Int) -> Unit): List<Pair<Long, Int>> {
        val out = mutableListOf<Pair<Long, Int>>()
        var i = 0
        var tick = 0L
        var tempo = initialTempo
        var status = 0

        fun readVar(): Int {
            var value = 0
            while (i < bytes.size) {
                val b = bytes[i++].toInt() and 0xFF
                value = (value shl 7) or (b and 0x7F)
                if ((b and 0x80) == 0) break
            }
            return value
        }

        while (i < bytes.size) {
            tick += readVar()
            if (i >= bytes.size) break
            var b = bytes[i++].toInt() and 0xFF
            if (b < 0x80) {
                i--
                b = status
            } else {
                status = b
            }

            when {
                b == 0xFF -> {
                    if (i >= bytes.size) break
                    val type = bytes[i++].toInt() and 0xFF
                    val len = readVar()
                    if (type == 0x51 && len == 3 && i + 2 < bytes.size) {
                        tempo = ((bytes[i].toInt() and 0xFF) shl 16) or ((bytes[i + 1].toInt() and 0xFF) shl 8) or (bytes[i + 2].toInt() and 0xFF)
                        onTempo(tempo)
                    }
                    i = (i + len).coerceAtMost(bytes.size)
                }
                b == 0xF0 || b == 0xF7 -> i = (i + readVar()).coerceAtMost(bytes.size)
                else -> {
                    val command = b and 0xF0
                    val dataLen = if (command == 0xC0 || command == 0xD0) 1 else 2
                    if (i + dataLen > bytes.size) break
                    val d1 = bytes[i++].toInt() and 0xFF
                    val d2 = if (dataLen == 2) bytes[i++].toInt() and 0xFF else 0
                    if (command == 0x90 && d2 > 0) {
                        val ms = max(0L, tick * tempo / max(1, tpq) / 1000L)
                        out += ms to d1
                    }
                }
            }
        }
        return out
    }

    private fun readAscii(input: DataInputStream, len: Int): String {
        val b = ByteArray(len)
        input.readFully(b)
        return b.toString(Charsets.US_ASCII)
    }
}
