package pw.binom

import pw.binom.io.Input
import pw.binom.io.Output
import pw.binom.io.Reader

actual object Console {
    actual val stdChannel: Output = TODO()
    actual val errChannel: Output = TODO()
    actual val inChannel: Input = TODO()

    actual val std: Appendable = TODO()
    actual val err: Appendable = TODO()
    actual val input: Reader = TODO()
}
