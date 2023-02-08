package pw.binom.io.httpServer

import pw.binom.io.AsyncReader
import pw.binom.io.IOException
import pw.binom.io.http.MutableHeaders

internal fun interface IdlePool {
    suspend fun returnToPool(channel: ServerAsyncAsciiChannel)
}

internal object HttpUtils {
    suspend fun readHeaders(dest: MutableHeaders, reader: AsyncReader) {
        while (true) {
            val s = reader.readln() ?: break
            if (s.isEmpty()) {
                break
            }
            val p = s.indexOf(':')
            if (p < 0) {
                throw IOException("Invalid HTTP Header: \"$s\"")
            }
            val headerKey = s.substring(0, p)
            val headerValue = s.substring(p + 2)
            dest.add(headerKey, headerValue)
        }
    }
}
