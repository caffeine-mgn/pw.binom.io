package pw.binom.io.http

object EmptyReadOnlyMutableHeaders : MutableHeaders {
    private fun throwError(): Nothing = throw IllegalStateException("Updating headers not supported")
    override fun set(key: String, value: List<String>): MutableHeaders = throwError()

    override fun set(key: String, value: String?): MutableHeaders = throwError()

    override fun add(key: String, value: List<String>): MutableHeaders = throwError()

    override fun add(key: String, value: String): MutableHeaders = throwError()

    override fun add(headers: Headers): MutableHeaders = throwError()

    override fun remove(key: String): MutableHeaders = throwError()

    override fun remove(key: String, value: String): Boolean = throwError()

    override fun get(key: String): List<String>? = null

    override fun clear() = throwError()

    override val entries: Set<Map.Entry<String, List<String>>>
        get() = emptySet()
    override val keys: Set<String>
        get() = emptySet()
    override val size: Int
        get() = 0
    override val values: Collection<List<String>>
        get() = emptyList()

    override fun containsKey(key: String): Boolean = false

    override fun containsValue(value: List<String>): Boolean = false

    override fun isEmpty(): Boolean = true
}
