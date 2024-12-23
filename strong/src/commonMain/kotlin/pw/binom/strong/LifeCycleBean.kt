package pw.binom.strong

interface LifeCycleBean {
  val order: Int
  suspend fun beforeInit()
  suspend fun afterInit()
  suspend fun preDestroy()
  suspend fun afterDestroy()
}
