package pw.binom.date

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

const val DAYS_PER_CYCLE = 146097
const val DAYS_0000_TO_1970 = DAYS_PER_CYCLE * 5 - (30 * 365 + 7)
const val MILLISECONDS_IN_SECOND = 1000L
const val MILLISECONDS_IN_MINUTE = MILLISECONDS_IN_SECOND * 60
const val MILLISECONDS_IN_HOUR = MILLISECONDS_IN_MINUTE * 60
const val MILLISECONDS_IN_DAY = MILLISECONDS_IN_HOUR * 24

@OptIn(ExperimentalContracts::class)
inline fun extractDateItems(
  utc: Long,
  offset: Duration,
  year: (Int) -> Unit = {},
  month: (Int) -> Unit = {},
  dayOfMonth: (Int) -> Unit = {},
  hours: (Int) -> Unit = {},
  minutes: (Int) -> Unit = {},
  seconds: (Int) -> Unit = {},
  millisecond: (Int) -> Unit = {},
  dayOfWeek: (Int) -> Unit = {},
) {
  contract {
    callsInPlace(year, InvocationKind.EXACTLY_ONCE)
    callsInPlace(month, InvocationKind.EXACTLY_ONCE)
    callsInPlace(dayOfMonth, InvocationKind.EXACTLY_ONCE)
    callsInPlace(hours, InvocationKind.EXACTLY_ONCE)
    callsInPlace(minutes, InvocationKind.EXACTLY_ONCE)
    callsInPlace(seconds, InvocationKind.EXACTLY_ONCE)
    callsInPlace(millisecond, InvocationKind.EXACTLY_ONCE)
    callsInPlace(dayOfWeek, InvocationKind.EXACTLY_ONCE)
  }
  val utcTime = utc + offset.inWholeMilliseconds
  var epochDay = (utcTime / MILLISECONDS_IN_DAY).toInt()
  var time = utcTime % MILLISECONDS_IN_DAY // (utcTime - (epochDay * DateMath.MILLISECONDS_IN_DAY))

  if (time < 0) {
    time += MILLISECONDS_IN_DAY
    epochDay -= 1
  }
  require(time in 0..MILLISECONDS_IN_DAY) { "Invalid time" }
  var zeroDay = epochDay + DAYS_0000_TO_1970
  // find the march-based year
  zeroDay -= 60 // adjust to 0000-03-01 so leap day is at end of four year cycle

  var adjust = 0
  if (zeroDay < 0) { // adjust negative years to positive for calculation
    val adjustCycles = (zeroDay + 1) / DAYS_PER_CYCLE - 1
    adjust = adjustCycles * 400
    zeroDay += -adjustCycles * pw.binom.date.DAYS_PER_CYCLE
  }
  var yearEst = ((400 * zeroDay.toLong() + 591) / DAYS_PER_CYCLE).toInt()
  var doyEst = zeroDay - (365 * yearEst + yearEst / 4 - yearEst / 100 + yearEst / 400)
  if (doyEst < 0) { // fix estimate
    yearEst--
    doyEst = zeroDay - (365 * yearEst + yearEst / 4 - yearEst / 100 + yearEst / 400)
  }
  yearEst += adjust // reset any negative year

  val marchDoy0 = doyEst

  // convert march-based values back to january-based
  val marchMonth0 = (marchDoy0 * 5 + 2) / 153
  val monthTmp = (marchMonth0 + 2) % 12 + 1
  val dom = marchDoy0 - (marchMonth0 * 306 + 5) / 10 + 1
  yearEst += marchMonth0 / 10

  year(yearEst)
  month(monthTmp)
  dayOfMonth(dom)
  var newtime = time
  val hoursTmp = (newtime / MILLISECONDS_IN_HOUR).toInt()
  hours(hoursTmp)
  newtime -= hoursTmp * pw.binom.date.MILLISECONDS_IN_HOUR
  val minutesTmp = (newtime / MILLISECONDS_IN_MINUTE).toInt()
  minutes(minutesTmp)
  newtime -= minutesTmp * pw.binom.date.MILLISECONDS_IN_MINUTE
  val secondsTmp = (newtime / MILLISECONDS_IN_SECOND).toInt()
  seconds(secondsTmp)
  newtime -= secondsTmp * pw.binom.date.MILLISECONDS_IN_SECOND
  millisecond(newtime.toInt())
  dayOfWeek(
    when (val dw = (epochDay + 3).mod(7)) {
      0 -> 1
      1 -> 2
      2 -> 3
      3 -> 4
      4 -> 5
      5 -> 6
      6 -> 0
      else -> TODO("Invalid day of week $dw")
    },
  )
}

internal fun toEpochDay(
  year: Int,
  monthNumber: Int,
  dayOfMonth: Int,
): Long {
  val y = year
  val m = monthNumber
  var total = 0L
  total += 365 * y
  if (y >= 0) {
    total += (y + 3) / 4 - (y + 99) / 100 + (y + 399) / 400
  } else {
    total -= y / -4 - y / -100 + y / -400
  }
  total += ((367 * m - 362) / 12)
  total += dayOfMonth - 1
  if (m > 2) {
    total--
    if (!isLeapYear(year)) {
      total--
    }
  }
  return total - DAYS_0000_TO_1970
}

private fun isLeapYear(year: Int): Boolean {
  if (year % 400 == 0) return true
  if (year % 100 == 0) return false
  return year % 4 == 0
}

internal fun dateTimeCompareTo(
  first: DateTime,
  second: DateTime,
) = when {
  first.milliseconds > second.milliseconds -> 1
  first.milliseconds < second.milliseconds -> -1
  else -> 0
}

internal fun dateTimePlus(
  date: DateTime,
  duration: Duration,
) = DateTime(date.milliseconds + duration.inWholeMilliseconds)

internal fun dateTimeMinus(
  date: DateTime,
  duration: Duration,
) = DateTime(date.milliseconds - duration.inWholeMilliseconds)

internal fun dateTimeMinus(
  date: DateTime,
  other: DateTime,
): Duration = (date.milliseconds - other.milliseconds).milliseconds
