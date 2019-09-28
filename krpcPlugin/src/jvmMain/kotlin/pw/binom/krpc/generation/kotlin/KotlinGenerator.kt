package pw.binom.krpc.generation.kotlin

import pw.binom.krpc.Interface
import pw.binom.krpc.Struct
import pw.binom.krpc.generation.Generator

object KotlinGenerator : Generator {

    override fun struct(packageName: String?, struct: Struct, output: Appendable) {
        StructGenerator.generate(packageName = packageName, struct = struct, out = output)
    }

    override fun service(packageName: String?, service: Interface, output: Appendable) {
        ServiceGenerator.generate(packageName, service, output)
    }
}