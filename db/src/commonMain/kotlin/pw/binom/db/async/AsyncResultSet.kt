package pw.binom.db.async

import pw.binom.collections.defaultMutableList
import pw.binom.db.ResultSet
import pw.binom.db.SQLException
import pw.binom.io.AsyncCloseable
import pw.binom.io.useAsync

interface AsyncResultSet : ResultSet, AsyncCloseable {
  suspend fun next(): Boolean
}

suspend fun AsyncResultSet.collect(func: suspend (AsyncResultSet) -> Unit) {
  useAsync {
    while (next()) {
      func(this)
    }
  }
}

/**
 * Calls [mapper] for each rows of this [AsyncResultSet]. Makes list of result [mapper] and returns it.
 * Closes this [AsyncResultSet] after call [mapper] for each values of this [AsyncResultSet]
 */
suspend inline fun <T> AsyncResultSet.map(mapper: (AsyncResultSet) -> T): List<T> {
  val out = defaultMutableList<T>()
  useAsync {
    while (next()) {
      out += mapper(this)
    }
  }
  return out
}

suspend inline fun AsyncResultSet.forEach(func: (AsyncResultSet) -> Unit) {
  useAsync {
    while (next()) {
      func(this)
    }
  }
}

suspend fun AsyncResultSet.count(): Int {
  var count = 0
  forEach {
    count++
  }
  return count
}

/**
 * Returns result of call [mapper] for single value from this [AsyncResultSet]. If this [AsyncResultSet] contains row more than one
 * will throw [SQLException]. If this [AsyncResultSet] is empty will return null.
 * Closes this [AsyncResultSet] after call [mapper]
 */
suspend fun <T> AsyncResultSet.singleOrNull(mapper: (ResultSet) -> T): T? =
  useAsync {
    if (!next()) {
      return@useAsync null
    }
    val result = mapper(this)
    if (next()) {
      throw SQLException("Found more than one results")
    }
    result
  }

/**
 * Returns result of call [mapper] for single value from this [AsyncResultSet]. If this [AsyncResultSet] contains row more than one
 * will throw [SQLException].
 * Closes this [AsyncResultSet] after call [mapper]
 */
suspend fun <T> AsyncResultSet.single(mapper: (ResultSet) -> T): T =
  useAsync {
    if (!next()) {
      throw SQLException("Can't find any value")
    }
    val result = mapper(this)
    if (next()) {
      throw SQLException("Found more than one results")
    }
    result
  }

/**
 * Returns result of call [mapper] for first value from this [AsyncResultSet]. If this [AsyncResultSet] is empty will
 * return null.
 * Closes this [AsyncResultSet] after call [mapper]
 */
suspend fun <T> AsyncResultSet.firstOrNull(mapper: (ResultSet) -> T): T? =
  useAsync {
    if (!next()) {
      return@useAsync null
    }
    mapper(this)
  }

/**
 * Returns result of call [mapper] for first value from this [AsyncResultSet]. If this [AsyncResultSet] is empty will
 * throw [SQLException].
 * Closes this [AsyncResultSet] after call [mapper]
 */
suspend fun <T> AsyncResultSet.first(mapper: (ResultSet) -> T): T? =
  useAsync {
    if (!next()) {
      throw SQLException("Can't find any value")
    }
    mapper(this)
  }
