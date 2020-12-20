package pw.binom.krpc.generation.kotlin

import pw.binom.krpc.Interface

object ServiceGenerator {
    fun generate(packageName: String?, service: Interface, out: Appendable) {
        if (packageName != null)
            out.append("package ${packageName}\n\n")

        out
                .append("import pw.binom.krpc.RPCService\n")
                .append("import pw.binom.krpc.StructFactory\n")
        out.append("\n")

        out.append("object ${service.name}:RPCService<${service.name}Sync,${service.name}Async> {\n")
        out.append("\toverride val methods=\n")
        out.append("\t\tlistOf(\n")
        service.methods.forEachIndexed { index, method ->
            if (index != 0)
                out.append(",\n")
            val args = method.params.map { "\"${it.name}\" to ${it.type.asPsevdoClass()}" }.joinToString(", ")
            out.append("\t\t\tRPCService.Method($index, \"${method.name}\", listOf($args), ${method.result?.asPsevdoClass()
                    ?: "StructFactory.Class.Void"})")

        }
        out.append(")\n")

        out.append("\toverride fun call(service: ${service.name}Sync, index: Int, args: List<Any?>): Any? =\n")
        out.append("\t\twhen (index){\n")
        service.methods.forEachIndexed { index, method ->
            out.append("\t\t\t${index} -> service.${method.name}(${method.params.mapIndexed { index, arg -> "args[$index] as ${arg.type.asCode()}" }.joinToString(", ")})\n")
        }
        out.append("\t\t\telse -> TODO()\n")
        out.append("\t}\n")

        out.append("\toverride suspend fun callAsync(service: ${service.name}Async, index: Int, args: List<Any?>): Any? =\n")
        out.append("\t\twhen (index){\n")
        service.methods.forEachIndexed { index, method ->
            out.append("\t\t\t${index} -> service.${method.name}(${method.params.mapIndexed { index, arg -> "args[$index] as ${arg.type.asCode()}" }.joinToString(", ")})\n")
        }
        out.append("\t\t\telse -> TODO()\n")
        out.append("\t}\n")


        out.append("}\n\n")
        out.append("interface ${service.name}Async {\n")
        service.methods.forEach {
            out.append("\tsuspend fun ${it.name}(${it.params.map { "${it.name}: ${it.type.asCode()}" }.joinToString(", ")}):${it.result?.asCode()
                    ?: "Unit"}\n")
        }
        out.append("}\n\n")
        out.append("interface ${service.name}Sync {\n")
        service.methods.forEach {
            out.append("\tfun ${it.name}(${it.params.map { "${it.name}: ${it.type.asCode()}" }.joinToString(", ")}):${it.result?.asCode()
                    ?: "Unit"}\n")
        }
        out.append("}\n")

        out.append("class ${service.name}RemoteSync(private val func:(RPCService.Method, List<Any?>)->Any?) {\n")
        service.methods.forEachIndexed { index, method ->
            out.append("\tfun ${method.name}(${method.params.map { "${it.name}: ${it.type.asCode()}" }.joinToString(", ")})")
            if (method.result == null) {
                out.append("{\n")
                out.append("\t\tfunc(${service.name}.methods[$index], listOf(${method.params.map { it.name }.joinToString(", ")}))\n")
                out.append("\t}\n")
            } else {
                out.append(" =\n")
                out.append("\t\tfunc(${service.name}.methods[$index], listOf(${method.params.map { it.name }.joinToString(", ")})) as ${method.result.asCode()}\n")
            }
        }
        out.append("}\n")

        out.append("class ${service.name}RemoteAsync(private val func: suspend (RPCService.Method, List<Any?>)->Any?) {\n")
        service.methods.forEachIndexed { index, method ->
            out.append("\tsuspend fun ${method.name}(${method.params.map { "${it.name}: ${it.type.asCode()}" }.joinToString(", ")})")
            if (method.result == null) {
                out.append("{\n")
                out.append("\t\tfunc(${service.name}.methods[$index], listOf(${method.params.map { it.name }.joinToString(", ")}))\n")
                out.append("\t}\n")
            } else {
                out.append(" =\n")
                out.append("\t\tfunc(${service.name}.methods[$index], listOf(${method.params.map { it.name }.joinToString(", ")})) as ${method.result.asCode()}\n")
            }
        }

        out.append("}")
    }
}