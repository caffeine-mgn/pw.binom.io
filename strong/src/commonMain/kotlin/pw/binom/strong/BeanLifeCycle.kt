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
}
