package pw.binom.io.http

object EmptyHeaders : Headers {
    override fun get(key: String): List<String>? = null

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
