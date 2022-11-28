package pw.binom.url

interface QueryBuilder {
    fun add(key: String, value: String?)
    fun add(key: String)
}
