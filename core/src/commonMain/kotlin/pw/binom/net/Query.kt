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

    fun forEach(func: (key: String, value: String?) -> Unit) {
        if (raw.isEmpty()) {
            return
        }
        raw.splitToSequence("&")
            .forEach {
                if (it.isEmpty()) {
                    return@forEach
                }
                val items = it.split('=', limit = 2)
                func(UTF8.decode(items[0]), items.getOrNull(1)?.let { UTF8.decode(it) })
            }
    }

    override fun toString(): String = raw
}

val String.toQuery
    get() = Query(this)