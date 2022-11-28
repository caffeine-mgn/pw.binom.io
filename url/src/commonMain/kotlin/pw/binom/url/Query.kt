package pw.binom.url

import kotlin.jvm.JvmInline

@JvmInline
value class Query internal constructor(val raw: String) {
    companion object {
        val EMPTY = Query("")
        fun new(key: String, value: String? = null): Query {
            if (key.isEmpty()) {
                throw IllegalArgumentException("Key can't be empty")
            }
            return if (value == null) {
                Query(UrlEncoder.encode(key))
            } else {
                Query("${UrlEncoder.encode(key)}=${UrlEncoder.encode(value)}")
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
                        UrlEncoder.encode(it.key)
                    } else {
                        "${UrlEncoder.encode(it.key)}=${UrlEncoder.encode(value)}"
                    }
                )
            }
            return sb.toString().toQuery
        }
    }

    val isEmpty
        get() = raw.isEmpty()

    val isNotEmpty
        get() = raw.isNotEmpty()

    fun append(key: String, value: String?): Query {
        val queryForAppend = new(key = key, value = value)
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
                if (func(UrlEncoder.decode(items[0]), items.getOrNull(1)?.let { UrlEncoder.decode(it) })) {
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
    fun toMap(dst: MutableMap<String, String?>): MutableMap<String, String?> {
        search { key, value ->
            dst[key] = value
            false
        }
        return dst
    }

    fun toMap(): Map<String, String?> = toMap(HashMap())

    fun toList(dst: MutableList<Pair<String, String?>>): MutableList<Pair<String, String?>> {
        search { key, value ->
            dst += key to value
            false
        }
        return dst
    }

    fun toList(): List<Pair<String, String?>> = toList(ArrayList())

    fun find(key: String) = toMap()[key]

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
