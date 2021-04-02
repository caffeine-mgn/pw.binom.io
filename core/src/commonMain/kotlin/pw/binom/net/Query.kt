package pw.binom.net

import pw.binom.io.UTF8

inline class Query internal constructor(val raw: String) {
    companion object {
        fun new(key: String, value: String? = null): Query {
            return if (value == null) {
                Query(UTF8.encode(key))
            } else {
                Query("${UTF8.encode(key)}=${UTF8.encode(value)}")
            }
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
     * Search any key named as [key]. If [key] exist returns null, in other case returns false
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

    override fun toString(): String = raw
}

val String.toQuery
    get() = Query(this)