package pw.binom.properties.ini

import pw.binom.strong.properties.StrongProperties

fun StrongProperties.addIni(ini: String) {
  ini.lineSequence()
    .map { it.trimStart() }
    .filter {
      !it.startsWith("#") && !it.startsWith(";")
    }
    .map {
      val items = it.split('=', limit = 2)
      items[0] to items.getOrNull(1)
    }.toMap()
    .let { add(it) }
}
