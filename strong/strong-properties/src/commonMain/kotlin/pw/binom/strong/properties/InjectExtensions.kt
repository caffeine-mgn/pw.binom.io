package pw.binom.strong.properties

import pw.binom.strong.Strong
import pw.binom.strong.inject
import pw.binom.strong.map

inline fun <reified T : Any> injectProperty() =
  inject<StrongProperties>().map { it.parse<T>() }

inline fun <reified T : Any> Strong.injectProperty() =
  inject<StrongProperties>().map { it.parse<T>() }
