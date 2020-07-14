package pw.binom.date

import kotlin.jvm.JvmName

expect class Calendar {
    val year: Int
    val month: Int
    val dayOfMonth: Int
    val dayOfWeek: Int
    val hours: Int
    val minutes: Int
    val seconds: Int
    val millisecond: Int
    val date: Date

    fun timeZone(timeZoneOffset: Int): Calendar
}