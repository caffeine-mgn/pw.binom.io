package pw.binom.url

import kotlin.jvm.JvmInline

@JvmInline
value class PathMask(val raw: String) {
    fun toPath(): Path = toPath {
        throw IllegalArgumentException("Can't convert mask \"$raw\" to path: can't replace variable \"$it\"")
    }

    fun toPath(map: Map<String, String>) = toPath { name ->
        map[name] ?: throw IllegalArgumentException("Can't find value for variable \"$name\"")
    }

    fun toPath(vararg values: Pair<String, String>) = toPath(values.toMap())

    fun toPath(variable: (String) -> String): Path {
        val sb = StringBuilder()
        raw.parsePathMask(
            variable = { text, position -> sb.append(variable(text)) },
            wildcard = { text, position -> sb.append(variable(text)) },
            text = { text, position -> sb.append(text) },
        )
        return sb.toString().toPath
    }

    override fun toString(): String = raw
}
