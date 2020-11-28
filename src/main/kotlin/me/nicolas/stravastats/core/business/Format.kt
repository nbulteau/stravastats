package me.nicolas.stravastats.core.business

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

var inFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")

var outFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE dd MMMM yyyy - HH:mm")

fun String.formatDate(): String = LocalDateTime.parse(this, inFormatter).format(outFormatter)

fun Int.formatSeconds(): String {
    val hours = (this - (this % 3600)) / 3600
    val min = ((this % 3600) / 60)

    if (hours == 0) {
        if (min == 0) {
            return String.format("%02ds", this % 60)
        }
        return String.format("%02dm %02ds", min, this % 60)
    }
    return String.format("%02dh %02dm %02ds", hours, min, this % 60)
}

fun Double.formatSeconds(): String {
    var min = ((this % 3600) / 60).toInt()
    var sec = (this % 60).toInt()
    val hnd = ((this - min * 60 - sec) * 100 + 0.5).toInt()
    if (hnd == 100) {
        sec++
        if (sec == 60) {
            sec = 0
            ++min
        }
    }
    return String.format("%d:%02d", min, sec)
}

