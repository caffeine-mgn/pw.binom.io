package pw.binom.io

/**
 * Calls [Buffer.clear] and returns [this]
 */
fun <T : Buffer> T.clean(): T {
    clear()
    return this
}

expect interface Buffer {
    companion object;
    var position: Int
    var limit: Int
    val capacity: Int
    val elementSizeInBytes: Int
    val remaining: Int
    fun flip()
    fun compact()
    fun clear()
}
