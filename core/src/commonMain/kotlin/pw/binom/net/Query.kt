package pw.binom.net

import pw.binom.io.UTF8
import kotlin.jvm.JvmInline
import kotlin.jvm.JvmName

@JvmInline
value class Query internal constructor(val raw: String) {
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

    /**
     * Calls [func] for each variables. Keep in mind [func] can call for same variable several times. In this case
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
                if (func(UTF8.decode(items[0]), items.getOrNull(1)?.let { UTF8.decode(it) }))
                    return
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
    fun toMap(dst: MutableMap<String, String?> = HashMap()): Map<String, String?> {
        search { key, value ->
            dst[key]=value
            false
        }
        return dst
    }

    override fun toString(): String = raw
}

val String.toQuery
    get() = Query(this)