package pw.binom

expect internal fun calcTime(year: Int, month: Int, dayOfMonth: Int, hours: Int, min: Int, sec: Int): Long
expect internal fun nowTime(): Long
expect class NTime {
    var tm_hour: Int
    var tm_mday: Int
    var tm_min: Int
    var tm_mon: Int
    var tm_sec: Int
    var tm_wday: Int
    var tm_yday: Int
    var tm_year: Int
}

expect internal fun currentTimezoneOffset():Int
expect internal fun getNativeTime(time: Long): NTime