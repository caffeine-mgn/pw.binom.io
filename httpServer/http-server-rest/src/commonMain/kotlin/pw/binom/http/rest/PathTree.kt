package pw.binom.http.rest

import pw.binom.url.Path
import pw.binom.url.PathMask

class PathTree<T>(init: () -> T) {
  internal val root = Node("", init)

  fun findByPath(path: Path): T? {
    if (path.raw.isEmpty()) {
      return root.getOrInitValue()
    }
    return sequenceFind(
      node = root,
      path = path.raw,
      index = 0,
    )
  }

  fun getMask(path: PathMask): T {
    val sb = StringBuilder()
    path.splitOnElements(const = { sb.append(it) }, variable = { sb.append("*") })
    val fullPath = sb.toString()
    var cursor = 0
    var start = 0
    var node = root

    while (cursor < fullPath.length) {
      val char = fullPath[cursor]
      if (char == '/' || char == '{' || char == '?' || char == '*') {
        if (cursor == start) {
          val line = fullPath.substring(startIndex = start, endIndex = cursor + 1)
          node = node.push(line)
          start = cursor + 1
        } else {
          val line = fullPath.substring(startIndex = start, endIndex = cursor)
          node = node.push(line)
          start = cursor
        }
      }
      cursor++
    }
    if (start != cursor) {
      val line = fullPath.substring(startIndex = start, endIndex = cursor)
      node = node.push(line)
    }
    return node.getOrInitValue()
  }

  @Suppress("UNCHECKED_CAST")
  internal class Node<T>(var key: String, val valueProvider: () -> T) {
    private var value: T? = null
    private var inited = false
    fun getOrInitValue(): T {
      if (!inited) {
        inited = true
        value = valueProvider()
      }
      return value as T
    }

    val nodes = ArrayList<Node<T>>()
    var wildcard: Node<T>? = null

    fun push(item: String): Node<T> {
      if (item == "*") {
        if (wildcard == null) {
          val n = Node("*", valueProvider)
          wildcard = n
          return n
        } else {
          return wildcard!!
        }
      }
      var exist = nodes.find { it.key == item }
      if (exist != null) {
        return exist
      }
      val forSeparate = nodes.find { it.key.startsWith(item) }
      if (forSeparate != null) {
        nodes.remove(forSeparate)
        exist = Node(item, valueProvider)
        nodes -= forSeparate
        nodes += exist
        forSeparate.key = forSeparate.key.substring(item.length)
        exist.nodes += forSeparate
        return exist
      }
      val forSeparate2 = nodes.findWithoutIterator { item.startsWith(it.key) }
      if (forSeparate2 != null) {
        val ee = item.substring(forSeparate2.key.length)
        exist = Node(ee, valueProvider)
        forSeparate2.nodes += exist
        return exist
      }
      exist = Node(item, valueProvider)
      nodes += exist
      return exist
    }
  }

  private fun wildcardFind(node: Node<T>, path: String, index: Int): T? {
    node.nodes.forEachWithoutIterator { subNode ->
      val substringIndex = path.indexOf(string = subNode.key, startIndex = index)
      if (substringIndex != -1) {
        val value = sequenceFind(
          node = subNode,
          path = path,
          index = substringIndex + subNode.key.length,
        )
        if (value != null) {
          return value
        }
      }
    }
    val wildcard = node.wildcard
    return if (wildcard == null) {
      node.getOrInitValue()
    } else {
      wildcardFind(
        node = wildcard,
        path = path,
        index = index
      )
    }
  }

  private fun sequenceFind(node: Node<T>, path: String, index: Int): T? {
    node.nodes.forEachWithoutIterator { subNode ->
      if (path.startsWith(prefix = subNode.key, startIndex = index)) {
        val result = sequenceFind(
          node = subNode,
          path = path,
          index = index + subNode.key.length
        )
        if (result != null) {
          return result
        }
      }
    }
    val wildcard = node.wildcard
    return if (wildcard != null) {
      wildcardFind(
        node = wildcard,
        path = path,
        index = index
      )
    } else {
      if (index == path.length) {
        node.getOrInitValue()
      } else {
        null
      }
    }
  }
}

private inline fun <T> ArrayList<T>.findWithoutIterator(func: (T) -> Boolean): T? {
  var i = 0
  while (i < size) {
    val value = get(i)
    if (func(value)) {
      return value
    }
    i++
  }
  return null
}

private inline fun <T> ArrayList<T>.forEachWithoutIterator(func: (T) -> Unit) {
  var i = 0
  while (i < size) {
    func(get(i))
    i++
  }
}
