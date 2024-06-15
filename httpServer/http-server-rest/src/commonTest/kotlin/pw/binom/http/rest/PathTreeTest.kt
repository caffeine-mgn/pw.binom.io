package pw.binom.http.rest

import pw.binom.testing.shouldEquals
import pw.binom.testing.shouldNotNull
import pw.binom.testing.shouldNull
import pw.binom.url.toPath
import pw.binom.url.toPathMask
import kotlin.math.absoluteValue
import kotlin.test.Test

class PathTreeTest {
  class Holder {
    var value = "[empty ${hashCode().absoluteValue}]"
    fun value(value: String) {
      this.value = value
    }

    override fun toString(): String = value
  }

  @Test
  fun getMaskTest() {
    fun valid(tree: PathTree<*>) {
      tree.root.apply {
        key shouldEquals ""
        wildcard.shouldNull()
        nodes.size shouldEquals 1
        nodes[0].apply {
          wildcard.shouldNull()
          key shouldEquals "/"
          nodes.size shouldEquals 1
          nodes[0].apply {
            wildcard.shouldNull()
            key shouldEquals "use"
            nodes.size shouldEquals 2
            nodes.find { it.key == "rs" }.shouldNotNull().apply {
              wildcard.shouldNull()
              nodes.size shouldEquals 0
            }
            nodes.find { it.key == "/" }.shouldNotNull().apply {
              nodes.size shouldEquals 0
              wildcard.shouldNotNull().apply {
                wildcard.shouldNull()
                nodes.size shouldEquals 1
                nodes[0].apply {
                  key shouldEquals "/id"
                  wildcard.shouldNull()
                }
              }
            }
          }
        }
      }
    }
    run {
      val tree = PathTree { }
      tree.getMask("/users".toPathMask())
      tree.getMask("/use/*".toPathMask())
      tree.getMask("/use/*/id".toPathMask())
      valid(tree)
//      tree.root.print(0)
    }
    run {
      val tree = PathTree { Holder() }
      tree.getMask("/use/*".toPathMask()).value("[/use/*]")
      tree.getMask("/use/*/id".toPathMask()).value("[/use/*/id]")
      tree.getMask("/users".toPathMask()).value("[/users]")
      valid(tree)
    }
  }

  @Test
  fun findByPathTest(){
    val tree = PathTree { Holder() }
    tree.getMask("/use/*".toPathMask()).value("[/use/*]")
    tree.getMask("/use/*/id".toPathMask()).value("[/use/*/id]")
    tree.getMask("/users".toPathMask()).value("[/users]")
    tree.findByPath("/use/123/id".toPath).shouldNotNull().value shouldEquals "[/use/*/id]"
    tree.findByPath("/use/123/id2".toPath).shouldNotNull().value shouldEquals "[/use/*]"
  }

  private fun PathTree.Node<*>.print(level: Int) {
    this.nodes.forEach { subNode ->
      print(level.toString().padEnd(3, ' '))
      repeat(level) {
        print("    ")
      }
      print(subNode.key)
      print("->")
      println()
      subNode.print(level + 1)
    }
    val wildcard = wildcard
    if (wildcard != null) {
      print(level.toString().padEnd(3, ' '))
      repeat(level) {
        print("    ")
      }
      print("*")
      print("->")
      println()
      wildcard.print(level + 1)
    }
  }
}
