package pw.binom

expect interface Buffer {
    val remaining: Int
    var position: Int
    var limit: Int
    val capacity: Int
    val elementSizeInBytes: Int
    fun flip()
    fun compact()
    fun clear()
}