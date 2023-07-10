package pw.binom.io.http

import kotlin.jvm.JvmInline

@JvmInline
value class SimpleHeaders private constructor(private val raw: Array<String>) {
  class HeaderBuilderContext {
    internal val list = ArrayList<String>()
    fun put(key: String, value: String) {
      list += key
      list += value
    }
  }

  companion object {
    val EMPTY = SimpleHeaders(emptyArray<String>())

    @Suppress("UNCHECKED_CAST")
    fun build(vararg headers: Pair<String, String>): SimpleHeaders {
      val raw = arrayOfNulls<String>(headers.size * 2)
      headers.forEachIndexed { index, pair ->
        raw[index * 2 + 0] = pair.first
        raw[index * 2 + 1] = pair.second
      }
      return SimpleHeaders(raw as Array<String>)
    }

    fun build(func: HeaderBuilderContext.() -> Unit): SimpleHeaders {
      val ctx = HeaderBuilderContext()
      func(ctx)
      if (ctx.list.isEmpty()) {
        return EMPTY
      }
      return SimpleHeaders(ctx.list.toTypedArray())
    }

    private fun headersToRaw(headers: Headers): Array<String> {
      if (headers.isEmpty()) {
        return emptyArray()
      }
      return build {
        headers.forEachHeader { key, value ->
          put(key = key, value = value)
        }
      }.raw
    }
  }

  constructor(headers: Headers) : this(headersToRaw(headers))

  val size
    get() = if (raw.isEmpty()) {
      0
    } else {
      raw.size / 2
    }

  fun forEach(func: (key: String, value: String) -> Unit) {
    var i = 0
    while (i < raw.size) {
      val key = raw[i * 2 + 0]
      val value = raw[i * 2 + 1]
      func(key, value)
      i += 2
    }
  }

  fun getLastOrNull(key: String): String? {
    var i = 0
    var result: String? = null
    while (i < raw.size) {
      if (raw[i * 2 + 0] == key) {
        result = raw[i * 2 + 1]
      }
      i += 2
    }
    return result
  }

  fun getFirstOrNull(key: String): String? {
    var i = 0
    while (i < raw.size) {
      if (raw[i * 2 + 0] == key) {
        return raw[i * 2 + 1]
      }
      i += 2
    }
    return null
  }

  @Suppress("UNCHECKED_CAST")
  operator fun plus(other: SimpleHeaders): SimpleHeaders {
    val result = arrayOfNulls<String>(raw.size + other.size) as Array<String>
    raw.copyInto(
      destination = result,
      destinationOffset = 0,
    )
    other.raw.copyInto(
      destination = result,
      destinationOffset = raw.size,
    )
    return SimpleHeaders(result)
  }
}
