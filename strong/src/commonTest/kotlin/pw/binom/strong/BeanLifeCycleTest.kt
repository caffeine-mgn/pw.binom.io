package pw.binom.strong

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals

class BeanLifeCycleTest {
  class BeanA(val list: MutableList<Int>) {
    val ff by inject<BeanB>()

    init {
      list += 3
      BeanLifeCycle.postConstruct {
        list += 4
      }
      BeanLifeCycle.preDestroy {
        list += 5
      }
    }
  }

  class BeanB(val list: MutableList<Int>) {
    init {
      list += 0
      BeanLifeCycle.postConstruct {
        list += 1
      }
      BeanLifeCycle.preDestroy {
        list += 2
      }
    }
  }

  @Test
  fun test() =
    runTest {
      val list = ArrayList<Int>()
      val strong =
        Strong.create(
          Strong.config {
            it.bean { BeanA(list) }
            it.bean { BeanB(list) }
          },
        )
      strong.destroy()
      strong.awaitDestroy()
      assertContentEquals(listOf(3, 0, 1, 4, 2, 5), list)
    }
}
