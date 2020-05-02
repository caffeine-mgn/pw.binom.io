package pw.binom.process

import pw.binom.io.Closeable

/**
 * For work in java run "java -Xrs -jar <path to you program>"
 */
expect object Signal {
    enum class Type {
        CTRL_C,
        CTRL_B,
        CLOSE,
        LOGOFF,
        SHUTDOWN
    }
    fun listen(signal:Type,handler:(Signal.Type)->Unit):Closeable
    fun closeAll()
}