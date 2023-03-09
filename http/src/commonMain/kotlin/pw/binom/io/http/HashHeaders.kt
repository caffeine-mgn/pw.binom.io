package pw.binom.io.http

import pw.binom.collections.defaultMutableList
import pw.binom.collections.defaultMutableMap
import pw.binom.collections.useName

class HashHeaders : MutableHeaders, Map<String, List<String>> {
    private var body = defaultMutableMap<String, MutableList<String>>().useName("HashHeaders.body")
    private var ref = defaultMutableMap<String, String>().useName("HashHeaders.ref")
    override fun set(key: String, value: List<String>): MutableHeaders {
        if (value.isEmpty()) {
            remove(key)
            return this
        }
        val lowercase = key.lowercase()
        val v = ref[lowercase]
        if (v == null) {
            val list = defaultMutableList(value)
            body[key] = list
            ref[lowercase] = key
        } else {
            body[v]!!.clear()
            body[v]!!.addAll(value)
        }
        return this
    }

    override fun set(key: String, value: String?): MutableHeaders =
        if (value == null) {
            remove(key)
            this
        } else {
            set(key, listOf(value))
        }

    override fun add(key: String, value: List<String>): MutableHeaders {
        val v = ref[key.lowercase()] ?: return set(key, value)
        body[v]!!.addAll(value)
        return this
    }

    override fun add(key: String, value: String): MutableHeaders {
        val v = ref[key.lowercase()] ?: return set(key, value)
        body[v]!!.add(value)
        return this
    }

    override fun add(headers: Headers): MutableHeaders {
        headers.forEach { e ->
            e.value.forEach {
                add(e.key, it)
            }
        }
        return this
    }

    override fun remove(key: String): MutableHeaders {
        val v = ref.remove(key.lowercase()) ?: return this
        body.remove(v)
        return this
    }

    override fun remove(key: String, value: String): Boolean {
        val low = key.lowercase()
        val v = ref[low] ?: return false
        val list = body[v]!!
        list.remove(value)
        if (list.isEmpty()) {
            ref.remove(low)
            body.remove(v)
        }
        return true
    }

    override fun get(key: String): MutableList<String>? {
        val v = ref[key.lowercase()] ?: return null
        return body[v]!!
    }

    override fun clear() {
        ref.clear()
        body.clear()
    }

    override val entries: Set<Map.Entry<String, List<String>>>
        get() = body.entries

    override val keys: Set<String>
        get() = body.keys

    override val size: Int
        get() = body.size

    override val values: Collection<List<String>>
        get() = body.values

    override fun containsKey(key: String): Boolean = key.lowercase() in ref

    override fun containsValue(value: List<String>): Boolean =
        value.all { it.lowercase() in ref }

    override fun isEmpty(): Boolean =
        ref.isEmpty()

    override fun toString(): String {
        val sb = StringBuilder()
        body.forEach { e ->
            e.value.forEach {
                sb.append("${e.key}: $it\n")
            }
        }
        return sb.toString()
    }
}
