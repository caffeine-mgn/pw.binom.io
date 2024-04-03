package pw.binom.strong

object BeanLifeCycle {
  private fun currentStrong() =
    STRONG_LOCAL as StrongWithDependenciesSpy? ?: throw IllegalStateException("Should call inside bean constructor")

  fun postConstruct(func: suspend () -> Unit) {
    currentStrong().postConstruct += func
  }

  fun preDestroy(func: suspend () -> Unit) {
    currentStrong().preDestroy += func
  }

  /**
   * Special method for init field after init.
   *
   * ```kotlin
   * class MyBean {
   *   val userRepository:UserRepository by inject()
   *
   *   // field will be init in post-construct phase
   *   val userCount by BeanLifeCycle.afterInit {
   *     userRepository.getUserCount()
   *   }
   *
   *   init {
   *     // Try access to userCount before init phase.
   *     println(userCount)
   *     BeanLifeCycle.postConstruct {
   *       // Access to userCount after init phase. All is alright.
   *       println(userCount)
   *     }
   *   }
   * }
   * ```
   */
  fun <T> afterInit(func: suspend () -> T): Lazy<T> {
    val property = LazyProperty<T>()
    postConstruct {
      property.update(func())
    }
    return property
  }
}

private class LazyProperty<T> : Lazy<T> {
  private var inited = false
  private var fieldValue: T? = null

  fun update(value: T) {
    check(!inited) { "Field already inited" }
    inited = true
    fieldValue = value
  }

  @Suppress("UNCHECKED_CAST")
  override val value: T
    get() = fieldValue as T

  override fun isInitialized(): Boolean = inited
}
