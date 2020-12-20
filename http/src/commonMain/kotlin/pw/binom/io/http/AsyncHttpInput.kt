package pw.binom.io.http

import pw.binom.AsyncInput

/**
 * Base Http Async Input Stream
 */
interface AsyncHttpInput : AsyncInput {
    val isEof: Boolean
}