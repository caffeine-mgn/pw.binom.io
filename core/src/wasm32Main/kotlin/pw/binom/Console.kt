package pw.binom

import pw.binom.io.*

actual object Console {
    actual val stdChannel: Output
        get() = NullOutput
    actual val errChannel: Output
        get() = NullOutput
    actual val inChannel: Input
        get() = NullInput
    actual val std: Appendable
        get() = NullAppendable
    actual val err: Appendable
        get() = NullAppendable
    actual val input: Reader
        get() = NullReader
}
