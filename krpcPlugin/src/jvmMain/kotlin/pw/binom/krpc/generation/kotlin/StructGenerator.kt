package pw.binom.krpc.generation.kotlin

import pw.binom.krpc.Struct
import pw.binom.krpc.Type

fun Type.asPsevdoClass(): String {
    return when {
        this is Type.Primitive -> when (type) {
            Type.Primitive.Type.INT -> "StructFactory.Class.Int($nullable)"
            Type.Primitive.Type.LONG -> "StructFactory.Class.Long($nullable)"
            Type.Primitive.Type.BOOL -> "StructFactory.Class.Boolean($nullable)"
            Type.Primitive.Type.FLOAT -> "StructFactory.Class.Float($nullable)"
            Type.Primitive.Type.STRING -> "StructFactory.Class.String($nullable)"
            Type.Primitive.Type.STRUCT -> "StructFactory.Class.Any($nullable)"
        }
        this is Type.Array -> {
            "StructFactory.Class.Array(${this.type.asPsevdoClass()} ,$nullable)"
        }
        this is Type.Struct -> "StructFactory.Class.Struct(${this.name} ,$nullable)"
        else -> TODO("Unknown type $this")
    }
}

fun Type.asKotlinType(): String {
    return when {
        this is Type.Primitive -> when (type) {
            Type.Primitive.Type.INT -> "Int"
            Type.Primitive.Type.LONG -> "Long"
            Type.Primitive.Type.BOOL -> "Boolean"
            Type.Primitive.Type.FLOAT -> "Float"
            Type.Primitive.Type.STRING -> "String"
            Type.Primitive.Type.STRUCT -> "DTO"
        }
        this is Type.Array -> {
            "List<${type.asCode()}>"
        }
        this is Type.Struct -> "${name}"
        else -> TODO("Unknown type $this")
    }
}

fun Type.asCode(): String = "${asKotlinType()}${if (nullable) "?" else ""}"

object StructGenerator {
    fun generate(packageName: String?, struct: Struct, out: Appendable) {
        if (packageName != null)
            out.append("package ${packageName}\n\n")

        val fullStructName = if (packageName == null)
            struct.name
        else
            "$packageName.${struct.name}"


        out
                .append("import pw.binom.krpc.Struct\n")
                .append("import pw.binom.krpc.StructFactory\n")

        out.append("\n")

        val fields = struct.fields.map { "val ${it.name}: ${it.type.asCode()}" }.joinToString(", ")
        out.append("class ${struct.name}($fields): Struct")
        if (struct.extends == "Exception")
            out.append(", RuntimeException()")
        out.append(" {\n")
        out.append("\tcompanion object:StructFactory<${struct.name}>{\n")
        out.append("\t\toverride val name\n")
        out.append("\t\t\tget() = \"${fullStructName}\"\n")

        out.append("\t\toverride fun getField(dto: ${struct.name}, index: Int): Any? =\n")
        out.append("\t\t\twhen (index){\n")
        struct.fields.forEachIndexed { index, field ->
            out.append("\t\t\t\t$index -> dto.${field.name}\n")
        }
        out.append("\t\t\t\telse -> TODO()\n")
        out.append("\t\t\t}\n")

        out.append("\t\toverride val uid\n")
        out.append("\t\t\tget() = ${fullStructName.hashCode().toUInt()}u\n")
        out.append("\t\toverride fun newInstance(fields: List<Any?>) =\n")
        out.append("\t\t\t").append(struct.name).append("(")
        struct.fields.forEachIndexed { index, field ->
            if (index != 0)
                out.append(", ")
            out.append("fields[$index] as ").append(field.type.asCode())
        }
        out.append(")\n")
        out.append("\t\toverride val fields =\n")
        val vv = struct.fields.mapIndexed { index, it -> "StructFactory.Field($index,\"${it.name}\",${it.type.asPsevdoClass()})" }.joinToString(", ")
        out.append("\t\t\tlistOf<StructFactory.Field>($vv)\n")
        out.append("\t}\n")
        out.append("\toverride val factory\n")
        out.append("\t\tget() = ${struct.name}\n")

        out.append("}")
    }
}