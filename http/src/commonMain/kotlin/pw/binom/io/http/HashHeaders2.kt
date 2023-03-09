package pw.binom.io.http

import pw.binom.collections.defaultMutableMap

class HashHeaders2(val map: MutableMap<String, MutableList<String>>) : MutableHeaders {
    constructor() : this(map = defaultMutableMap<String, MutableList<String>>())

    override val entries: Set<Map.Entry<String, List<String>>>
        get() = map.entries
    override val keys: Set<String>
        get() = map.keys
    override val size: Int
        get() = map.size
    override val values: Collection<List<String>>
        get() = map.values

    override fun add(key: String, value: String): MutableHeaders {
        map.getOrPut(key) { ArrayList() }.add(value)
        return this
    }

    override fun add(key: String, value: List<String>): MutableHeaders {
        map.getOrPut(key) { ArrayList() }.addAll(value)
        return this
    }

    override fun add(headers: Headers): MutableHeaders {
        map.forEach { e ->
            add(e.key, e.value)
        }
        return this
    }

    override fun clear() {
        map.clear()
    }

    override fun containsKey(key: String): Boolean = map.containsKey(key)

    override fun containsValue(value: List<String>): Boolean = map.containsValue(value)

    override fun get(key: String): List<String>? = map[key]

    override fun isEmpty(): Boolean = map.isEmpty()

    override fun remove(key: String): MutableHeaders {
        map.remove(key)
        return this
    }

    override fun remove(key: String, value: String): Boolean {
        val list = map[key] ?: return false
        if (!list.remove(value)) {
            return false
        }
        if (list.isEmpty()) {
            map.remove(key)
        }
        return true
    }

    override fun set(key: String, value: String?): MutableHeaders {
        if (value == null) {
            remove(key)
            return this
        }
        val values = ArrayList<String>()
        values += value
        map[key] = values
        return this
    }

    override fun set(key: String, value: List<String>): MutableHeaders {
        if (value.isEmpty()) {
            remove(key)
        } else {
            map[key] = if (value is MutableList) {
                value
            } else {
                ArrayList(value)
            }
        }
        return this
    }
}
