package pw.binom.url

import kotlin.jvm.JvmInline

@JvmInline
value class PathMask(val raw: String) {

  inline fun splitOnElements(crossinline const: (String) -> Unit, crossinline variable: (String) -> Unit) {
    raw.parsePathMask(
      variable = { text, position -> variable(text) },
      wildcard = { text, position -> variable(text) },
      text = { text, position -> const(text) },
    )
  }

  fun toPath(): Path = toPath {
    throw IllegalArgumentException("Can't convert mask \"$raw\" to path: can't replace variable \"$it\"")
  }

  fun toPath(map: Map<String, String>) = toPath { name ->
    map[name] ?: throw IllegalArgumentException("Can't find value for variable \"$name\"")
  }

  fun toPath(vararg values: Pair<String, String>) = toPath(values.toMap())

  fun getVariables(): Set<String> {
    val variables = HashSet<String>()
    raw.parsePathMask(
      variable = { name, _ -> variables += name },
      wildcard = null,
      text = null,
    )
    return variables
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

  override fun toString(): String = raw
}
