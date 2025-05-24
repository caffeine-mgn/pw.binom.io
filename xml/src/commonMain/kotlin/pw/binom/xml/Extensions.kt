package pw.binom.xml

import pw.binom.collections.LinkedList
import pw.binom.xml.dom.XElement

fun XElement.Tag.tags() = child.asSequence().filterIsInstance<XElement.Tag>()
fun XElement.Tag.text() = child.asSequence().filterIsInstance<XElement.Text>().joinToString("")
fun Sequence<XElement.Tag>.withName(name: String) =
  filter { it.name == name }

fun XElement.Tag.flattenXml() = sequence<XElement> {
  yield(this@flattenXml)
  yieldAll(child.asSequence().flattenXml())
}

fun Sequence<XElement>.flattenXml() = sequence<XElement> {
  val tags = LinkedList<XElement>()
  tags += this@flattenXml
  while (!tags.isEmpty()) {
    val current = tags.removeFirst()
    yield(current)
    if (current is XElement.Tag) {
      current.child.forEach {
        if (it is XElement.Tag) {
          tags.addLast(it)
        } else {
          yield(it)
        }
      }
    }
  }
}

val XElement.asTagOrNull
  get() = this as? XElement.Tag
