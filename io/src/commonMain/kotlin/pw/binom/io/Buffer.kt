package pw.binom.io

expect interface Buffer {
    var position: Int
    var limit: Int
    val capacity: Int
    val elementSizeInBytes: Int
    val remaining: Int
    fun flip()
    fun compact()
    fun clear()
}

/**
 * Calls [Buffer.clear] and returns [this]
 */
fun <T : Buffer> T.clean(): T {
    clear()
    return this
}