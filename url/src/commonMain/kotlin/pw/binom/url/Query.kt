package pw.binom.url

import kotlin.jvm.JvmInline

@JvmInline
value class Query constructor(val raw: String) {
  companion object {
    val EMPTY = Query("")

    fun new(
      key: String,
      value: String? = null,
    ): Query {
      if (key.isEmpty()) {
        throw IllegalArgumentException("Key can't be empty")
      }
      return if (value == null) {
        Query(encode(key))
      } else {
        Query("${encode(key)}=${encode(value)}")
      }
    }

    fun build(func: QueryBuilder.() -> Unit): Query {
      val sb = StringBuilder()
      val builder = QueryBuilderImpl(sb)
      func(builder)
      return Query(sb.toString())
    }

    /**
     * Creates new query from [map]
     */
    fun new(map: Map<String, String?>): Query {
      val sb = StringBuilder()
      var first = true
      map.forEach {
        if (!first) {
          sb.append("&")
        }
        first = false
        val value = it.value
        sb.append(
          if (value == null) {
            encode(it.key)
          } else {
            "${encode(it.key)}=${encode(value)}"
          },
        )
      }
      return sb.toString().toQuery
    }
  }

  val isEmpty
    get() = raw.isEmpty()

  val isNotEmpty
    get() = raw.isNotEmpty()

  fun append(
    key: String,
    value: String?,
  ): Query {
    val queryForAppend = new(key = key, value = value)
    if (isEmpty) {
      return queryForAppend
    }
    return this + queryForAppend
  }

  fun append(values: Map<String, String?>): Query {
    if (values.isEmpty()) {
      return this
    }
    val queryForAppend = new(values)
    if (isEmpty) {
      return queryForAppend
    }
    return this + queryForAppend
  }

  /**
   * Calls [func] for each variable. Keep in mind [func] can call for same variable several times. In this case
   * you should take last value of this variable
   */
  fun search(func: (key: String, value: String?) -> Boolean) {
    if (raw.isEmpty()) {
      return
    }
    raw.splitToSequence("&")
      .forEach {
        if (it.isEmpty()) {
          return@forEach
        }
        val items = it.split('=', limit = 2)
        val key = decode(items[0])
        val value = items.getOrNull(1)?.let { decode(it) }
        if (func(key, value)) {
          return
        }
      }
  }

  /**
   * Search any key named as [key]. If [key] exist returns true, in other case returns false.
   * If value found and value is null will return true
   */
  fun isExist(key: String): Boolean {
    var result = false
    search { qkey, _ ->
      if (qkey == key) {
        result = true
        return@search false
      }
      return@search true
    }
    return result
  }

  /**
   * Search all values and keys and store them in to [dst]. Default value of [dst] is new [HashMap]
   */
  fun toMap(dst: MutableMap<String, MutableList<String?>>): MutableMap<String, MutableList<String?>> {
    search { key, value ->
      dst.getOrPut(key) { ArrayList() }.add(value)
      false
    }
    return dst
  }

  fun toMap(): Map<String, List<String?>> = toMap(HashMap())

  fun toList(dst: MutableList<Pair<String, String?>>): MutableList<Pair<String, String?>> {
    search { key, value ->
      dst += key to value
      false
    }
    return dst
  }

  fun asSequence(): Sequence<Pair<String, String?>> {
    if (raw.isEmpty()) {
      return emptySequence()
    }
    return raw.splitToSequence("&")
      .mapNotNull {
        if (it.isEmpty()) {
          return@mapNotNull null
        }
        val items = it.split('=', limit = 2)
        val key = decode(items[0])
        val value = items.getOrNull(1)?.let { decode(it) }
        key to value
      }
  }

  fun toList(): List<Pair<String, String?>> = toList(ArrayList())

  fun find(key: String) = toMap()[key] ?: emptyList()

  override fun toString(): String = raw

  operator fun plus(new: Query): Query {
    if (isEmpty) {
      return new
    }
    if (new.isEmpty) {
      return this
    }
    return Query("$raw&${new.raw}")
  }
}

private fun decode(it: String) = UrlEncoder.decode(it.replace("+", "%20"))

private fun encode(it: String) = UrlEncoder.encode(it).replace("%20", "+")
