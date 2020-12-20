package pw.binom.krpc.generation.csharp

import pw.binom.io.file.File
import pw.binom.io.file.outputStream
import pw.binom.io.use
import pw.binom.io.utf8Appendable
import pw.binom.krpc.Interface
import pw.binom.krpc.ProtoFile
import pw.binom.krpc.Struct
import pw.binom.krpc.Type

object CsharpGenerator {
    private fun gen(type: Type?): String {
        type ?: return "void"
        val vv = when (type) {
            is Type.Primitive -> when (type.type) {
                Type.Primitive.Type.INT -> "int"
                Type.Primitive.Type.STRING -> "string"
                Type.Primitive.Type.BOOL -> "bool"
                Type.Primitive.Type.LONG -> "long"
                Type.Primitive.Type.FLOAT -> "float"
                Type.Primitive.Type.STRUCT -> "float"
            }
            is Type.Struct -> type.name
            is Type.Array -> "List<${gen(type.type)}>"
        }

        return if (type.nullable) "$vv?" else vv
    }

    fun generate(file: ProtoFile, outputFile: File) {
        outputFile.outputStream!!.use {
            val out = it.utf8Appendable()
            out.append("using System.Collections.Generic;\n\n")
            if (file.packageName != null) {
                out.append("namespace ${file.packageName}\n{\n")
            }
            file.structs.forEach {
                struct(it, out)
            }
            file.services.forEach {
                service(it, out)
            }
            if (file.packageName != null) {
                out.append("}")
            }
        }
    }

    private fun struct(struct: Struct, output: Appendable) {
        output.append("\tpublic struct ${struct.name}\n\t{\n")
        struct.fields.forEach {
            output.append("\t\t\tpublic ").append(gen(it.type)).append(" ").append(it.name).append(";\n")
        }
        output.append("\t}\n")
    }

    private fun service(service: Interface, output: Appendable) {
        output.append("\tpublic interface ${service.name}\n\t{\n")
        service.methods.forEach {
            output.append("\t\t\t").append(gen(it.result))
                    .append(" ").append(it.name).append("(")
                    .append(it.params.map { "${gen(it.type)} ${it.name}" }.joinToString(", "))
                    .append(");\n")


//                    .append(gen(it.)).append(" ").append(it.name).append("\n")
//            output.append("\t\t\tpublic ").append(gen(it.)).append(" ").append(it.name).append("\n")
        }
        output.append("\t}\n")
    }
}