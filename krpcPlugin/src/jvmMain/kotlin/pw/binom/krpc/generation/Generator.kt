package pw.binom.krpc.generation

import pw.binom.krpc.Interface
import pw.binom.krpc.Struct

interface Generator {
    fun struct(packageName: String?, struct: Struct, output: Appendable)
    fun service(packageName: String?, suspend: Boolean, service: Interface, output: Appendable)
}