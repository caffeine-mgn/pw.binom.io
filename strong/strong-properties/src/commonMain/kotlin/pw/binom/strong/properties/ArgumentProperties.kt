package pw.binom.strong.properties

import pw.binom.Environment
import pw.binom.getEnvs
import pw.binom.properties.IniParser
import pw.binom.properties.PropertyValue

object ArgumentProperties {
  fun parseEnvironment(
    prefix: String = "strong_",
    caseSensitive: Boolean = false,
  ): PropertyValue.Object {
    val map =
      Environment.getEnvs().asSequence()
        .map {
          (if (caseSensitive) it.key else it.key.lowercase()) to it.value
        }
        .filter {
          it.first.startsWith(prefix)
        }
        .map {
          it.first.removePrefix(prefix) to it.second
        }
        .map {
          it.first.replace('_', '.') to it.second
        }
        .toMap()
    return IniParser.parseMap(map)
  }

  fun parseArguments(
    args: Array<String>,
    prefix: String = "-D",
    caseSensitive: Boolean = false,
  ): PropertyValue.Object {
    val parsedArgs =
      args.asSequence().mapNotNull {
        it.takeIf { it.startsWith(prefix) }
          ?.removePrefix(prefix)
      }.map {
        val separatorIndex = it.indexOf('=')
        if (separatorIndex >= 0) {
          if (caseSensitive) {
            it
          } else {
            it.substring(0, separatorIndex).lowercase() + "=" + it.substring(separatorIndex + 1)
          }
        } else {
          if (caseSensitive) {
            "$it=true"
          } else {
            "${it.lowercase()}=true"
          }
        }
      }.toList()
    return IniParser.parseLines(parsedArgs)
  }
}
