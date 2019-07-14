package pw.binom.io.http

import pw.binom.io.AsyncInputStream

/**
 * Base Http Async Input Stream
 */
interface AsyncHttpInputStream : AsyncInputStream {
    val isEof: Boolean
}