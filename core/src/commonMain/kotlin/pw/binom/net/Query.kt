package pw.binom.net

import pw.binom.io.UTF8
import kotlin.jvm.JvmName

inline class Query internal constructor(val raw: String) {
    companion object {
        fun new(key: String, value: String? = null): Query {
            return if (value == null) {
                Query(UTF8.encode(key))
            } else {
                Query("${UTF8.encode(key)}=${UTF8.encode(value)}")
            }
        }

        /**
         * Creates new query from [map]
         */
        @JvmName("new2")
        fun new(map: Map<String, List<String?>>): Query {
            val sb = StringBuilder()
            var first = true
            map.forEach {
                if (it.value.isEmpty()) {
                    return@forEach
                }
                if (!first) {
                    sb.append("&")
                }
                first = false
                it.value.forEach { value ->
                    sb.append(
                        if (value == null) {
                            UTF8.encode(it.key)
                        } else {
                            "${UTF8.encode(it.key)}=${UTF8.encode(value)}"
                        }
                    )
                }
            }
            return sb.toString().toQuery
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
                        UTF8.encode(it.key)
                    } else {
                        "${UTF8.encode(it.key)}=${UTF8.encode(value)}"
                    }
                )

            }
            return sb.toString().toQuery
        }
    }

    fun append(key: String, value: String?): Query {
        if (key.isEmpty()) {
            throw IllegalArgumentException("Key can't be empty")
        }
        val result = if (value == null) UTF8.encode(key) else "${UTF8.encode(key)}=${UTF8.encode(value)}"

        return if (raw.isEmpty()) {
            Query(result)
        } else {
            Query("$raw&$result")
        }
    }

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
                if (func(UTF8.decode(items[0]), items.getOrNull(1)?.let { UTF8.decode(it) }))
                    return
            }
    }

    /**
     * Search first query param named as [key]. If [key] not found returns null
     *
     * Also perhaps key found, but value is null. In this case result will be null
     */
    fun firstOrNull(key: String): String? {
        var result: String? = null
        search { qkey, value ->
            if (qkey == key) {
                result = value
                return@search false
            }
            return@search true
        }
        return result
    }

    /**
     * Search any key named as [key]. If [key] exist returns true, in other case returns false.
     * If value found and value is null will return true
     */
    fun isExist(key: String): Boolean {
        var result = false
        search { qkey, value ->
            if (qkey == key) {
                result = true
                return@search false
            }
            return@search true
        }
        return result
    }

    /**
     * Returns all values of [key]
     */
    fun findAll(key: String): List<String?> {
        val result = ArrayList<String?>()
        search { qkey, value ->
            if (key == qkey) {
                result += value
            }
            true
        }
        return result
    }

    /**
     * Search all values and keys and store them in to [dst]. Default value of [dst] is [HashMap]
     */
    fun toMap(dst: MutableMap<String, ArrayList<String?>> = HashMap()): Map<String, List<String?>> {
        search { key, value ->
            dst.getOrPut(key) { ArrayList() }.add(value)
            false
        }
        return dst
    }

    /**
     * Search all values and keys and store them in to [dst]. Default value of [dst] is [HashMap]
     * If some key has several values will store in to [dst] last value
     */
    fun toDistinctMap(dst: MutableMap<String, String?> = HashMap()): Map<String, String?> {
        search { key, value ->
            dst[key] = value
            false
        }
        return dst
    }

    override fun toString(): String = raw
}

val String.toQuery
    get() = Query(this)