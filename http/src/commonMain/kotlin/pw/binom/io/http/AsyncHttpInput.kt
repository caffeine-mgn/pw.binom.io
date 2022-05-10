package pw.binom.io.http

import pw.binom.io.AsyncInput

/**
 * Base Http Async Input Stream
 */
interface AsyncHttpInput : AsyncInput {
    val isEof: Boolean
}
