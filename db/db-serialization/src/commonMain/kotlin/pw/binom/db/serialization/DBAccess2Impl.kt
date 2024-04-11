package pw.binom.db.serialization

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.SerializersModule
import pw.binom.collections.defaultMutableList
import pw.binom.date.DateTime
import pw.binom.db.ResultSet
import pw.binom.db.SQLException
import pw.binom.db.async.AsyncResultSet
import pw.binom.db.async.pool.PooledAsyncConnection
import pw.binom.io.useAsync
import pw.binom.uuid.UUID
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

private class SingleValueDateContainer : DateContainer {
  var value: Any? = null
  var useQuotes: Boolean = false

  override fun set(
    key: String,
    value: Any?,
    useQuotes: Boolean,
  ) {
    this.value = value
    this.useQuotes = useQuotes
  }
}

class ResultSetDataProvider(val rs: ResultSet) : DataProvider {
  override fun get(key: String): Any? = rs.getString(key)

  private fun columnNotDefined(key: String): Nothing = throw SQLException("Column \"$key\" is null")

  override fun getString(key: String): String = rs.getString(key) ?: columnNotDefined(key)

  override fun getBoolean(key: String): Boolean = rs.getBoolean(key) ?: columnNotDefined(key)

  override fun isNull(key: String): Boolean = rs.isNull(key)

  override fun getInt(key: String): Int = rs.getInt(key) ?: columnNotDefined(key)

  override fun getLong(key: String): Long = rs.getLong(key) ?: columnNotDefined(key)

  override fun getFloat(key: String): Float = rs.getFloat(key) ?: columnNotDefined(key)

  override fun getDouble(key: String): Double = rs.getDouble(key) ?: columnNotDefined(key)

  override fun getShort(key: String): Short = getInt(key).toShort()

  override fun getByte(key: String): Byte = getInt(key).toByte()

  override fun getByteArray(key: String): ByteArray = rs.getBlob(key) ?: columnNotDefined(key)

  override fun getChar(key: String): Char {
    val s = rs.getString(key) ?: columnNotDefined(key)
    if (s.length != 0) {
      throw SQLException("Column \"$key\" has invalid char")
    }
    return s[0]
  }

  override fun getUUID(key: String): UUID = rs.getUUID(key) ?: columnNotDefined(key)

  override fun getDateTime(key: String): DateTime = rs.getDateTime(key) ?: columnNotDefined(key)

  override fun contains(key: String): Boolean {
    val kk = key.lowercase()
    return rs.columns.find { it.lowercase() == kk } != null
  }
}

private class QueryContextImpl(override val serializersModule: SerializersModule) : UpdateContext {
  var args = defaultMutableList<Any?>()
  var returning: Returning<out Any>? = null
  val startQuery = StringBuilder()

  class Returning<T : Any>(val serializer: KSerializer<T>, val func: suspend (T) -> Unit)

  override fun <T : Any> returning(
    serializer: KSerializer<T>,
    func: suspend (T) -> Unit,
  ) {
    if (returning != null) {
      throw IllegalStateException("Returning function already defined")
    }
    returning =
      Returning(
        serializer = serializer,
        func = func,
      )
  }

  override fun <T : Any> param(
    serializer: KSerializer<T>,
    value: T?,
  ): String {
    if (value == null) {
      args += null
    } else {
      val singleValueDateContainer = SingleValueDateContainer()
      DefaultSQLSerializePool.encode(
        serializer = serializer,
        value = value,
        name = "",
        output = singleValueDateContainer,
        serializersModule = serializersModule,
        useQuotes = false,
        excludeGenerated = false,
      )
      args += singleValueDateContainer.value
    }
    return "?"
  }

  override fun String.unaryPlus(): String {
    startQuery.append(this)
    return ""
  }
}

@OptIn(ExperimentalTime::class)
class DBAccess2Impl internal constructor(
  val con: PooledAsyncConnection,
  internal val ctx: DBContextImpl,
  override val serializersModule: SerializersModule,
) : DBAccess2 {
  override suspend fun <T : Any> insert(
    serializer: KSerializer<T>,
    value: T,
    excludeGenerated: Boolean,
    onConflict: DBAccess2.ActionOnConflict,
  ): Boolean {
    var changed = 0L
    insert2(
      serializer = serializer,
      value = value,
      returning = false,
      excludeGenerated = excludeGenerated,
      onConflict = onConflict,
      changedRow = {
        changed = it
      },
    )
    return changed > 0
  }

  override suspend fun <T : Any> insertAndReturn(
    serializer: KSerializer<T>,
    value: T,
    excludeGenerated: Boolean,
    onConflict: DBAccess2.ActionOnConflict,
  ): T? =
    insert2(
      serializer = serializer,
      value = value,
      returning = true,
      excludeGenerated = excludeGenerated,
      onConflict = onConflict,
      changedRow = {},
    )

  private suspend inline fun <T : Any> insert2(
    serializer: KSerializer<T>,
    value: T,
    returning: Boolean,
    excludeGenerated: Boolean,
    onConflict: DBAccess2.ActionOnConflict,
    changedRow: ((Long) -> Unit),
  ): T? {
    val dsc = ctx.getDescription2(serializer)
    val params = HashMap<String, Pair<Boolean, Any?>>()
//    val output = object : DataBinder {
//      override fun get(key: String): Any? = params[key]
//      override fun contains(key: String): Boolean = params.containsKey(key)
//      override fun set(key: String, value: Any?, useQuotes: Boolean) {
//        params[key] = useQuotes to value
//      }
//    }
    DefaultSQLSerializePool.encode(
      serializer = serializer,
      value = value,
      name = "",
      output = dsc.getBinder(params),
      serializersModule = serializersModule,
      useQuotes = serializer.descriptor.isUseQuotes(),
      excludeGenerated = excludeGenerated,
    )

    val args =
      ArrayList<Any?>(params.size + if (onConflict == DBAccess2.ActionOnConflict.DoUpdate) params.size else 0)
    params.entries.forEachIndexed { index, param ->
      args += param.value.second
    }

    if (onConflict == DBAccess2.ActionOnConflict.DoUpdate || onConflict is DBAccess2.ActionOnConflict.DoUpdateOnColumns) {
      params.entries.forEachIndexed { index, param ->
        args += param.value.second
      }
    }
    val sql =
      dsc.getInsertStatement(
        params = params,
        onConflict = onConflict,
        returning = returning,
      )
    val ps = con.usePreparedStatement(sql)
    return if (returning) {
      return ps.executeQuery(args).useAsync { result ->
        val r = ResultSetDataProvider(result)
        if (result.next()) {
          DefaultSQLSerializePool.decode(
            serializer = serializer,
            name = "",
            input = r,
            serializersModule = serializersModule,
          )
        } else {
          null
        }
      }
    } else {
      changedRow(ps.executeUpdate(args))
      null
    }
  }

  override suspend fun selectRaw(func: suspend QueryContext.() -> String): AsyncResultSet {
    val params = QueryContextImpl(serializersModule)
    val endPart = func(params)
    val startPart = params.startQuery.toString()
    val query = startPart + endPart
    val ps = con.usePreparedStatement(query)
    return ps.executeQuery(params.args)
  }

  override suspend fun <T : Any> select(
    serializer: KSerializer<T>,
    func: suspend QueryContext.() -> String,
  ): Flow<T> {
    val result = selectRaw(func)
    val r = ResultSetDataProvider(result)
    return flow {
      try {
        while (result.next()) {
          val obj =
            measureTimedValue {
              DefaultSQLSerializePool.decode(
                serializer = serializer,
                name = "",
                input = r,
                serializersModule = serializersModule,
              )
            }
          SerializationMetrics.decodeAvrTime.put(obj.duration)
          emit(obj.value)
        }
      } finally {
        result.asyncClose()
      }
    }
  }

  override suspend fun update(func: suspend QueryContext.() -> String): Long {
    val params = QueryContextImpl(serializersModule)
    val firstPart = func(params)
    val query = params.startQuery.toString() + firstPart
    val ps = con.usePreparedStatement(query)
    return ps.executeUpdate(params.args)
  }

  override suspend fun <T : Any> selectAll(
    serializer: KSerializer<T>,
    condition: (suspend QueryContext.() -> String)?,
  ): Flow<T> =
    select(serializer = serializer) {
      if (condition != null) {
        "select * from ${tableName(serializer.descriptor)} where ${condition(this)}"
      } else {
        "select * from ${tableName(serializer.descriptor)}"
      }
    }
}
