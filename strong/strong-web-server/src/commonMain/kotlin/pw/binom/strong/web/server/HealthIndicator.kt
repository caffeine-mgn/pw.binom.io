package pw.binom.strong.web.server

interface HealthIndicator {
  suspend fun isHealthy(): Boolean
}
