package pw.binom.krpc.generation.kotlin

import pw.binom.krpc.Definition
import pw.binom.krpc.Interface
import pw.binom.krpc.Struct
import pw.binom.krpc.Type
import pw.binom.krpc.generation.Generator
import pw.binom.krpc.generation.ServiceGenerator
import pw.binom.krpc.generation.StructGenerator

class KotlinGenerator : Generator {
    private val structGenerator = StructGenerator()

    private val Type.nullAccesses
        get() = if (nullable) "?" else "!!"
    private val Type.nullDef
        get() = if (nullable) "?" else ""

    fun read(from: String, type: Type): String {
        if (type is Type.Primitive) {
            return when (type.type) {
                Type.Primitive.Type.INT -> "$from${type.nullAccesses}.int"
                Type.Primitive.Type.FLOAT -> "$from${type.nullAccesses}.float"
                Type.Primitive.Type.BOOL -> "$from${type.nullAccesses}.boolean"
                Type.Primitive.Type.STRING -> "$from${type.nullAccesses}.string"
                Type.Primitive.Type.LONG -> "$from${type.nullAccesses}.long"
                Type.Primitive.Type.STRUCT -> "${from}${type.nullAccesses}.obj${type.nullDef}.let { findFactory(it[\"@type\"]!!.string)!!.read(it) }"
            }
        }
        if (type is Type.Array) {
            return "$from${type.nullAccesses}.array${type.nullDef}.map { ${read("it", type.type)} }"
        }
        if (type is Type.Struct) {
            return "$from${type.nullAccesses}.obj${type.nullDef}.let { ${type.name}.read(it) }"
        }
        TODO()
    }

    fun writeArray(type: Type): String {
        if (type is Type.Primitive) {
            return when (type.type) {
                Type.Primitive.Type.INT,
                Type.Primitive.Type.LONG,
                Type.Primitive.Type.FLOAT -> if (type.nullable) "it?.let{JsonNumber(it.toString())}" else "JsonNumber(it.toString())"
                Type.Primitive.Type.BOOL -> if (type.nullable) "it?.let{JsonBoolean(it)}" else "JsonBoolean(it)"
                Type.Primitive.Type.STRING -> if (type.nullable) "it?.let{JsonString(it)}" else "JsonString(it)"
                else -> TODO()
            }
        }
        if (type is Type.Array) {
            return "it${type.nullDef}.asSequence()${type.nullDef}.map { ${writeArray(type.type)} }${type.nullDef}.toJsonArray()"
        }
        if (type is Type.Struct) {
            return "it${type.nullDef}.json()"
        }
        TODO()
    }

    fun writeObj(def: Definition): String {
        val type = def.type
        if (type is Type.Primitive) {
            return when (type.type) {
                Type.Primitive.Type.INT,
                Type.Primitive.Type.LONG,
                Type.Primitive.Type.FLOAT -> "number(\"${def.name}\", obj.${def.name})"
                Type.Primitive.Type.BOOL -> "bool(\"${def.name}\", obj.${def.name})"
                Type.Primitive.Type.STRING -> "string(\"${def.name}\", obj.${def.name})"
                else -> TODO()
            }
        }

        if (type is Type.Array) {
            return "array(\"${def.name}\", obj.${def.name}${def.type.nullDef}.asSequence()${def.type.nullDef}.map { ${writeArray(type.type)} }${def.type.nullDef}.toJsonArray())"
        }
        if (type is Type.Struct) {
            return "node(\"${def.name}\", obj.${def.name}${def.type.nullDef}.json())"
        }
        TODO()
    }

    fun typeProc(type: Type): String {
        return when {
            type is Type.Primitive -> when (type.type) {
                Type.Primitive.Type.INT -> "Int${if (type.nullable) "?" else ""}"
                Type.Primitive.Type.LONG -> "Long${if (type.nullable) "?" else ""}"
                Type.Primitive.Type.BOOL -> "Boolean${if (type.nullable) "?" else ""}"
                Type.Primitive.Type.FLOAT -> "Float${if (type.nullable) "?" else ""}"
                Type.Primitive.Type.STRING -> "String${if (type.nullable) "?" else ""}"
                Type.Primitive.Type.STRUCT -> "DTO${if (type.nullable) "?" else ""}"
            }
            type is Type.Array -> {
                "List<${typeProc(type.type)}>${type.nullDef}"
            }
            type is Type.Struct -> "${type.name}${type.nullDef}"
            else -> TODO("Unknown type $type")
        }
    }

    override fun struct(packageName: String?, struct: Struct, output: Appendable) {
        structGenerator.generate(packageName = packageName, struct = struct, out = output)
        return
        val r = output
        val isException = struct.extends == "Exception"
        if (packageName != null)
            output.append("package ${packageName}\n\n")

        output.append("import pw.binom.rpc.json.DTO\n")
                .append("import pw.binom.rpc.json.DTOFactory\n")
                .append("import pw.binom.json.*\n")
                .append("import pw.binom.rpc.json.json\n")
        output.append("\n")
        r.append("class ${struct.name}")
        r.append("(")

        struct.fields.forEachIndexed { index, field ->
            if (index > 0)
                r.append(",\n")
            else
                r.append("\n")
            r.append("\t\t")
            if (isException && field.name == "message")
                r.append("override ")
            r.append("val ").append(field.name).append(": ").append(typeProc(field.type))
            if (index == struct.fields.lastIndex)
                r.append("\n")
        }
        r.append(")")
        r.append(" : DTO")
        if (isException) {
            r.append(", RuntimeException()")
        }
        r.append(" {\n")

        r.append("\toverride val factory: DTOFactory<out DTO>\n").append("\t\tget() = ").append(struct.name).append("\n")
        r.append("\n")
        r.append("\tcompanion object : DTOFactory<${struct.name}> {\n")
        r.append("\t\toverride fun read(node: JsonObject) = ").append(struct.name).append("(")
        struct.fields.forEachIndexed { index, field ->
            if (index == 0)
                r.append("\n")
            else
                r.append(",\n")
            r.append("\t\t\t\t${field.name} = ").append(read("node[\"${field.name}\"]", field.type))
            if (index == struct.fields.lastIndex)
                r.append("\n\t\t")
        }
        r.append(")\n")
        r.append("\n")
        r.append("\t\toverride fun write(obj: ${struct.name}) = jsonNode {\n")

        var name = struct.name
        if (packageName != null && packageName.isNotBlank())
            name = "${packageName}.$name"

        r.append("\t\t\tstring(\"@type\", \"$name\")\n")

        struct.fields.forEachIndexed { index, field ->
            r.append("\t\t\t").append(writeObj(field)).append("\n")
        }
        r.append("\t\t}\n")
        r.append("\t}\n")
        r.append("}")
    }

    override fun service(packageName: String?, suspend: Boolean, service: Interface, output: Appendable) {
        ServiceGenerator().generate(packageName,service,output)
        return
        serviceLocal(packageName, suspend, service, output)
        serviceRemote(packageName, suspend, service, output)
    }

    private fun serviceRemote(packageName: String?, suspend: Boolean, service: Interface, output: Appendable) {
        val r = output
        r.append("class ${service.name}Remote(dtoMap: Map<String, DTOFactory<out DTO>>, remote: ")
        if (suspend)
            r.append("suspend ")
        r.append("(methodName: String, args: JsonObject) -> JsonNode?): ")
        if (suspend)
            r.append("ServiceRemoteAsync")
        else
            r.append("ServiceRemote")
        r.append("(dtoMap, remote) {\n")
        service.methods.forEach { method ->
            r.append("\t")
            if (suspend)
                r.append("suspend ")
            r.append("fun ").append(method.name).append("(")
            method.params.forEachIndexed { index, arg ->
                if (index > 0)
                    r.append(", ")
                r.append(arg.name).append(": ").append(typeProc(arg.type))
            }
            r.append("): ").append(method.result?.let { typeProc(it) } ?: "Unit").append("{\n")


            r.append("\t\t")
            if (method.result != null) {
                r
                        .append("val result")
                        .append(" = ")
            }
            r.append("remote(\"${method.name}\", JsonObject(mutableMapOf(")
            method.params.forEachIndexed { index, definition ->
                if (index > 0)
                    r.append(", ")
                r.append("\"${definition.name}\" to ${definition.name}${definition.type.nullDef}.toJson()")
            }
            r.append(")))\n")
            if (method.result != null) {
                r.append("\t\treturn ").append(read("result", method.result)).append("\n")
            }
            r.append("\t}\n")
        }
        r.append("}")
    }

    private fun serviceLocal(packageName: String?, suspend: Boolean, service: Interface, output: Appendable) {
        val r = output
        if (packageName != null)
            output.append("package ${packageName}\n\n")

        r
                .append("import pw.binom.json.JsonNode\n")
                .append("import pw.binom.json.JsonObject\n")
                .append("import pw.binom.json.long\n")
                .append("import pw.binom.json.obj\n")
                .append("import pw.binom.json.string\n")
                .append("import pw.binom.json.array\n")
                .append("import pw.binom.rpc.json.toJson\n")
                .append("import pw.binom.rpc.json.DTOFactory\n")
                .append("import pw.binom.rpc.json.DTO\n")
                .append("import pw.binom.rpc.json.ServiceRemoteAsync\n")
                .append("import pw.binom.json.int\n")
        r.append("\n")

        val parentClass = if (suspend) "ServiceLocalAsync" else "ServiceLocal"
        r.append("abstract class ${service.name}Local:pw.binom.rpc.json.$parentClass {\n")

        service.methods.forEach { method ->
            r.append("\tabstract ")
            if (suspend)
                r.append("suspend ")
            r.append("fun ").append(method.name).append("(")
            method.params.forEachIndexed { index, arg ->
                if (index > 0)
                    r.append(", ")
                r.append(arg.name).append(": ").append(typeProc(arg.type))
            }
            r.append("): ").append(method.result?.let { typeProc(it) } ?: "Unit")
            r.append("\n")
        }

        r.append("\toverride ")
        if (suspend)
            r.append("suspend ")
        r.append("fun call(methodName: String, args: JsonObject): Any? = when (methodName) {\n")
        service.methods.forEach { method ->
            r.append("\t\t\"").append(method.name).append("\" -> ").append(method.name).append("(")
            method.params.forEachIndexed { index, arg ->
                if (index > 0)
                    r.append(", ")
                r.append(readArg("args[\"${arg.name}\"]", arg.type))
//                r.append("args[\"${arg.name}\"] as ${typeProc(arg.type)}")
            }
            r.append(")\n")
        }
        r.append("\t\telse -> throw IllegalArgumentException(\"Unknown method \\\"\$methodName\\\"\")\n")
        r.append("\t}\n")
        r.append("}\n")
    }

    private fun readArg(from: String, type: Type): String {
        return when (type) {
            is Type.Primitive -> when (type.type) {
                Type.Primitive.Type.LONG -> "$from${type.nullAccesses}.long"
                Type.Primitive.Type.STRING -> "$from${type.nullAccesses}.string"
                Type.Primitive.Type.INT -> "$from${type.nullAccesses}.int"
                else -> TODO("Primitive $from -> ${type.type}")
            }
            is Type.Array -> "$from${type.nullAccesses}.array${type.nullDef}.map { ${readArg("it", type.type)} }"
            is Type.Struct -> "${from}${type.nullAccesses}.obj${type.nullDef}.let { findFactory(it[\"@type\"]!!.string)!!.read(it) as ${type.name} }"
        }

    }

}