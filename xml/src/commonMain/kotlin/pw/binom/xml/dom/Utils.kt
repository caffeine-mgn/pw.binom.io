package pw.binom.xml.dom

import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine

fun Map<Attribute, String?>.find(name: String, nameSpace: String?): String? {
  val attr = keys.find { it.name == name && it.nameSpace == nameSpace } ?: return null
  return this[attr]
}

internal fun <T> a(f: suspend () -> T): T {
  var result2: Result<T>? = null
  f.startCoroutine(object : Continuation<T> {
    override val context: CoroutineContext = EmptyCoroutineContext

    override fun resumeWith(result: Result<T>) {
      result2 = result
    }
  })
  return result2!!.getOrThrow()
}
