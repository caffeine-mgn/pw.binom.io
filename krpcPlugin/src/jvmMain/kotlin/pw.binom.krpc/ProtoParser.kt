package pw.binom.krpc

import pw.binom.io.Reader

fun typeParser(type: String): Type {
    return when {
        type.removeSuffix("?") == "int" -> Type.Primitive(Type.Primitive.Type.INT, type.endsWith("?"))
        type.removeSuffix("?") == "struct" -> Type.Primitive(Type.Primitive.Type.STRUCT, type.endsWith("?"))
        type.removeSuffix("?") == "long" -> Type.Primitive(Type.Primitive.Type.LONG, type.endsWith("?"))
        type.removeSuffix("?") == "bool" -> Type.Primitive(Type.Primitive.Type.BOOL, type.endsWith("?"))
        type.removeSuffix("?") == "float" -> Type.Primitive(Type.Primitive.Type.FLOAT, type.endsWith("?"))
        type.removeSuffix("?") == "string" -> Type.Primitive(Type.Primitive.Type.STRING, type.endsWith("?"))
        type.startsWith("array<") -> {
            val subType = typeParser(type.removeSuffix("?").removeSuffix(">").substring(6))
            Type.Array(type = subType, nullable = type.endsWith("?"))
        }
        else -> Type.Struct(type.removeSuffix("?"), type.endsWith("?"))
    }
}

fun parseProto(reader: Reader): ProtoFile {
    val p = Parser(reader)
    fun readPackageName(): String {
        val sb = StringBuilder()
        while (true) {
            val s = p.nextToken() ?: break
            if ('\n' in s)
                break
            if (s.isEmpty())
                continue
            if (!s.isBlank())
                sb.append(s)
        }
        return sb.toString()
    }

    fun readDefinition(first: String): Definition? {
        val list = ArrayList<String>()
        list.add(first)
        while (true) {
            val s = p.nextToken() ?: TODO()
            if ('\n' in s)
                break
            if (s.isBlank())
                continue
            list.add(s)
        }

        val list2 = list.filter { it.isNotEmpty() }
        if (list2.isEmpty())
            return null

        if (list2.size == 1)
            TODO()
        return Definition(name = list2.last(), type = typeParser(list2.subList(0, list2.lastIndex).joinToString("")))
    }

    fun readMethod(first: String): Method? {
        val list = ArrayList<String>()
        list.add(first)
        while (true) {
            val s = p.nextToken() ?: TODO()
            if (s == "(")
                break
            if (s.isBlank())
                continue
            list.add(s)
        }

        val resultType = list.subList(0, list.lastIndex).joinToString("").takeIf { it != "void" }
        val out = Method(name = list[list.lastIndex], result = resultType?.let { typeParser(it) })
        list.clear()

        while (true) {
            val s = p.nextTokenNoSpace() ?: TODO()
            if (s == ")" || s == ",") {
                if (list.isNotEmpty())
                    out.params += Definition(
                            name = list[list.lastIndex],
                            type = typeParser(list.subList(0, list.lastIndex).joinToString(""))
                    )

                list.clear()
                if (s == ")")
                    break
                continue
            }
            if (s.isBlank())
                continue
            list.add(s)
        }
        return out
    }

    fun readStruct(): Struct {
        val name = p.nextTokenNoSpace() ?: TODO()

        var extends: String? = null
        when (p.nextTokenNoSpace()) {
            ":" -> {
                extends = p.nextTokenNoSpace()
                if (p.nextTokenNoSpace() != "{")
                    TODO()
            }
            "{" -> {
            }
            else -> TODO()
        }
        val out = Struct(name, extends)
        while (true) {
            val s = p.nextTokenNoSpace() ?: TODO()
            if (s == "}")
                break
            out.fields += readDefinition(s) ?: continue
        }
        return out
    }

    fun readComment(): String {
        val next = p.nextTokenNoSpace()
        if (next == "/") {
            val sb = StringBuilder()
            while (true) {
                val g = p.nextToken() ?: break
                if ("\n" in g)
                    break
                sb.append(g)
            }
            return sb.toString()
        }
        if (next == "*") {
            val sb = StringBuilder()
            while (true) {
                val g = p.nextToken() ?: break
                if (g == "*") {
                    val v = p.nextToken()
                    if (v == null) {
                        sb.append("*")
                        break
                    }
                    if (v == "/")
                        break
                    else {
                        p.back(v)
                        continue
                    }
                    sb.append("*").append(v)
                    continue
                }
                sb.append(g)
            }
            return sb.toString()
        }
        TODO()
    }

    fun readInterface(): Interface {
        val name = p.nextTokenNoSpace() ?: TODO()
        val out = Interface(name)
        if (p.nextTokenNoSpace() != "{")
            TODO()
        while (true) {
            val s = p.nextTokenNoSpace() ?: TODO()
            if (s == "}")
                break
            out.methods += readMethod(s) ?: continue
        }
        return out
    }

    val out = ProtoFile()
    while (true) {
        val str = p.nextTokenNoSpace() ?: break
        when (str) {
            "package" -> out.packageName = readPackageName()
            "struct" -> out.structs += readStruct()
            "interface" -> out.services += readInterface()
            "/" -> readComment()
            else -> TODO("Unknown text \"$str\"")
        }
    }
    return out
}

class ProtoFile {
    var packageName: String? = null
    val structs = ArrayList<Struct>()
    val services = ArrayList<Interface>()
}