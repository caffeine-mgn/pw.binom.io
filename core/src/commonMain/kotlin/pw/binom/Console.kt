package pw.binom

import pw.binom.io.Input
import pw.binom.io.Output
import pw.binom.io.Reader

expect object Console {
    val stdChannel: Output
    val errChannel: Output
    val inChannel: Input

    val std: Appendable
    val err: Appendable
    val input: Reader
}
