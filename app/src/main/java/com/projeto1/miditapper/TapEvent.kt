package com.projeto1.miditapper

data class TapEvent(
    val delayMs: Long,
    val key: String
)

data class KeyPoint(
    val key: String,
    val x: Int,
    val y: Int
)
