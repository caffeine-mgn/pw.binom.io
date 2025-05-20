package pw.binom.strong

interface HealthIndicator {
  val componentName: String?
    get() = null

  suspend fun isHealthy(): Boolean
  suspend fun isReady(): Boolean = isHealthy()
}
