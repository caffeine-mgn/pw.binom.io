package pw.binom.db.radis

import pw.binom.io.ByteBuffer
import kotlin.time.Duration

class KeyTransformRadisConnection(val keyTransform: (String) -> String, val source: RadisConnection) : RadisConnection {
  override val readyForRequest: Boolean
    get() = source.readyForRequest

  override suspend fun setString(
    key: String,
    value: String,
    ttl: Duration?,
    updateMode: UpdateMode,
  ) {
    source.setString(key = keyTransform(key), value = value, ttl = ttl, updateMode = updateMode)
  }

  override suspend fun delete(vararg key: String): Long =
    source.delete(*Array<String>(key.size) {
      keyTransform(key[it])
    })

  override suspend fun delete(key: String): Boolean =
    source.delete(keyTransform(key))

  override suspend fun lset(key: String, value: String, index: Int) =
    source.lset(
      key = keyTransform(key),
      value = value,
      index = index,
    )

  override suspend fun inc(key: String) =
    source.inc(
      key = keyTransform(key)
    )

  override suspend fun inc(key: String, value: Int) =
    source.inc(
      key = keyTransform(key),
      value = value,
    )

  override suspend fun inc(key: String, value: Float) =
    source.inc(
      key = keyTransform(key),
      value = value,
    )

  override suspend fun renameKey(oldName: String, newName: String) =
    source.renameKey(
      oldName = keyTransform(oldName),
      newName = keyTransform(newName),
    )

  override suspend fun getList(
    key: String,
    start: Int,
    end: Int,
  ) = source.getList(
    key = keyTransform(key),
    start = start,
    end = end,
  )

  override suspend fun getListSize(key: String) = source.getListSize(
    key = keyTransform(key),
  )

  override suspend fun setStringAsBytes(
    key: String,
    data: ByteArray,
    ttl: Duration?,
    updateMode: UpdateMode,
  ) = source.setStringAsBytes(
    key = keyTransform(key),
    data = data,
    ttl = ttl,
    updateMode = updateMode,
  )

  override suspend fun setStringAsBytes(
    key: String,
    data: ByteBuffer,
    ttl: Duration?,
    updateMode: UpdateMode,
  ) = source.setStringAsBytes(
    key = keyTransform(key),
    data = data,
    ttl = ttl,
    updateMode = updateMode,
  )

  override suspend fun getString(key: String) =
    source.getString(key = keyTransform(key))

  override suspend fun getStringAsByteArray(key: String) =
    source.getStringAsByteArray(
      key = keyTransform(key)
    )

  override suspend fun insertLast(key: String, value: String) =
    source.insertLast(
      key = keyTransform(key),
      value = value,
    )

  override suspend fun insertFirst(key: String, value: String): Long? =
    source.insertFirst(
      key = keyTransform(key),
      value = value,
    )

  override suspend fun asyncClose() {
    source.asyncClose()
  }
}

fun RadisConnection.keyTransform(transformer: (String) -> String) =
  KeyTransformRadisConnection(
    keyTransform = transformer,
    source = this
  )
