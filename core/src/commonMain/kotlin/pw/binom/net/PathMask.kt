package pw.binom.net

import pw.binom.parsePathMask
import kotlin.jvm.JvmInline

@JvmInline
value class PathMask(val raw: String) {
    fun toPath(): Path = toPath {
        throw IllegalArgumentException("Can't convert mask \"$raw\" to path: can't replace variable \"$it\"")
    }

    fun toPath(variable: (String) -> String): Path {
        val sb = StringBuilder()
        raw.parsePathMask(
            variable = { text, position -> sb.append(variable(text)) },
            wildcard = { text, position -> sb.append(variable(text)) },
            text = { text, position -> sb.append(text) },
        )
        return sb.toString().toPath
    }
}

fun String.toPathMask() = PathMask(this)
