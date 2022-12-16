package pw.binom.date

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

operator fun DateTime.compareTo(expDate: DateTime): Int = when {
    time > expDate.time -> 1
    time < expDate.time -> -1
    else -> 0
}

fun minOf(a: DateTime, b: DateTime) = if (a < b) a else b
fun maxOf(a: DateTime, b: DateTime) = if (a > b) a else b

operator fun DateTime.plus(duration: Duration) =
    DateTime(time + duration.inWholeMilliseconds)

operator fun DateTime.minus(duration: Duration) =
    DateTime(time - duration.inWholeMilliseconds)

operator fun DateTime.minus(other: DateTime) = (time - other.time).milliseconds
