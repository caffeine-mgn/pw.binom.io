package pw.binom.process

import pw.binom.io.Closeable

/**
 * For work in java run as "java -Xrs -jar <path to you program>"
 */
expect object Signal {
    val isSigint:Boolean
    val isSigbreak:Boolean
    val isSigterm:Boolean
    val isInterrupted:Boolean
    val isClose:Boolean
    val isLogoff:Boolean
    val isShutdown:Boolean
}