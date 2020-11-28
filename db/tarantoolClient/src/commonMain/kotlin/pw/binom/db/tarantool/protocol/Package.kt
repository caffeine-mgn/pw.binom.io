package pw.binom.db.tarantool.protocol

internal data class Package(val header: Map<Int, Any?>, val body: Map<Int, Any?>) {

    fun assertException() {
        val code = header[Key.CODE.id] ?: return
        if (code != 0) {
            val message = body[Key.ERROR.id] as String?
            throw if (message == null)
                RuntimeException("Tarantool Exception")
            else
                RuntimeException("Tarantool Exception: $message")
        }
    }

    val data
        get() = body[Key.DATA.id]
}